/*

An event recorder

Example:

(
MPK.init();
e = MEventRecorder.new();
~mididevice = IAC.new("Bus 1");
)

(
// Start recording
e.record();

// Play some notes
"Play some notes".postln;
)

// Stop recording
e.stop();

// Replay events
r = e.eventCollection.asRoutine(~mididevice.midiout);

r.play

*/

EventRecorderCollection {
    var <events;

    *new {
        ^super.new.init;
    }

    init {
        events = [];
    }

    addEvent { |event|
        events = events.add(event);
    }

    processEvents {
        var processedEvents = [];
        var noteOnEvents = Dictionary.new;
        "All events:".postln;
        events.postln;

        events.do { |event|
            var time = event[0];
            var eventData = event[1];
            var type = eventData[\type];

            switch (type,
                \noteOn, {
                    var midinote = eventData[\midinote];
                    if(midinote.isNil, {
                        "Note on event without midinote detected:".warn;
                        event.postln;
                    }, {
                        if(event.isNil, {
                            "Event is nil".warn;
                        }, {
                            noteOnEvents.put(midinote.asSymbol, event).postln;
                        });
                    })
                },
                \noteOff, {
                    var midiNote = eventData[\midinote];
                    var noteOnEvent = noteOnEvents.at(midiNote.asSymbol);

                    if(noteOnEvent.isNil, {
                        "Note off event without corresponding note on detected:".warn;
                        event.postln;
                    }, {
                        var noteOnTime = noteOnEvent[0];
                        var duration = time - noteOnTime;
                        var collapsedEvent = (
                            type: \note,
                            timeBeats: noteOnTime,
                            duration: duration,
                            amp: noteOnEvent[1][\amp],
                            midinote: noteOnEvent[1][\midinote],
                            chan: noteOnEvent[1][\chan]
                        );

                        processedEvents = processedEvents ++ [[noteOnTime, collapsedEvent]];
                    })
                },
                { // Default case
                    "Unknown event type detected:".postln;
                    event.postln;
                }
            );
        };

        events = processedEvents;
        "Processed events".postln;
    }

    // Reposition all events so that the first event starts at time 0
    removeOffsetBeforeFirstEvent{
        var firstEventTime = events.first[0];
        var offset = firstEventTime;

        events = events.collect { |event|
            var time = event[0];
            var eventData = event[1];
            var type = eventData[\type];

            var newTime = time - offset;
            [newTime, eventData];
        };
    }

    quantizeToScale{|inScale|
        // Quantize all notes to a scale
        // Use the inScale.nearestInScale() method to quantize a note to the nearest note in the scale
        events = events.collect { |event|
            var time = event[0];
            var eventData = event[1];
            var type = eventData[\type];

            switch (type,
                \note, {
                    var midinote = eventData[\midinote];
                    var quantizedNote = midinote.nearestInScale(inScale);
                    eventData[\midinote] = quantizedNote;
                    [time, eventData];
                },
                { // Default case
                    event;
                }
            );
        };
    }


    asRoutine{|midiOutput|
        ^Routine.new({
            var clock = TempoClock.new;
            var events = this.events;
            var timeAtStart = clock.beats;

            "Playing back events".postln;

            events.do { |event|
                var time = event[0];
                var eventData = event[1];
                var type = eventData[\type];

                switch (type,
                    \note, {
                        var duration = eventData[\duration];
                        var amp = eventData[\amp];
                        var midinote = eventData[\midinote];
                        var chan = eventData[\chan];

                        clock.schedAbs(time - timeAtStart, {
                            "Playing note on: % % %".format(midinote, amp, duration).postln;
                            midiOutput.noteOn(chan, midinote, amp * 127);
                        });

                        clock.schedAbs(time - timeAtStart + duration, {
                            "Playing note off: % % %".format(midinote, amp, duration).postln;
                            midiOutput.noteOff(chan, midinote, amp * 127);

                        });
                    },
                    { // Default case
                        "Unknown event type detected:".postln;
                        event.postln;
                    }
                );
            };
        });
    }

    // All events sorted into an array with all events of the same midinote grouped together
    asPolyphonicArray{
        var polyphonic = Array.newClear(128);

        events.do { |event|
            var eventData = event[1];
            var midinote = eventData[\midinote];

            if(polyphonic[midinote].isNil, {
                polyphonic[midinote] = [];
            });

            polyphonic[midinote] = polyphonic[midinote].add(event);
        };

        ^polyphonic;
    }

    // TODO:
    // There is some confusion about the delta here
    asPbind{|midiOutput|

        // Iterate over the polyphonic array and create a Pbind for each midinote, if it is not empty
        var polyphonic = this.asPolyphonicArray;
        var polyPatterns = polyphonic.collect { |events, midinote|
            if(events.notNil, {
                var pbind = Pbind(
                    \type, \midi,
                    \midiout, midiOutput,

                    \delta, Pseq(events.collect { |event| event[0] }),
                    \dur, Pseq(events.collect { |event| event[1][\duration] }),
                    \amp, Pseq(events.collect { |event| event[1][\amp]  }),
                    \chan, Pseq(events.collect { |event| event[1][\chan] }),

                    \midinote, Pseq(events.collect { |event| event[1][\midinote] }),
                );

                pbind;
            });
        };


        ^Ppar(polyPatterns);
    }

}

// This class records incoming events and stores them according to a time, allowing to play them back later
MEventRecorder {
    var <routine;
    var <clock;
    var <eventCollection;
    var <noteOnMidiFunc, <noteOffMidiFunc;
    var <timeAtRecordStart = -1;
    var <>tempo = 120.0;

    *new{|clock|
        ^super.new.init(clock)
    }

    init{|clockIn|
        eventCollection = EventRecorderCollection.new;
        this.prSetupMidiFuncs();
    }

    record {
        clock = TempoClock.new(tempo: tempo / 60.0);
        eventCollection.events.clear();
        noteOnMidiFunc.enable;
        noteOffMidiFunc.enable;
        "Recording started".postln;
    }

    stop {
        noteOnMidiFunc.disable;
        noteOffMidiFunc.disable;

        // If you waited a bit to play the first notes, that wait time is removed.
        eventCollection.removeOffsetBeforeFirstEvent();

        // Turn seperate note on/off events into single note events
        ^eventCollection.processEvents();
    }

    events{
        ^eventCollection.events
    }

    prSetupMidiFuncs {
        noteOnMidiFunc = noteOnMidiFunc ?? {
            MIDIFunc.noteOn({ |...args|
                var time = clock.beats;
                var velocity = args[0];
                var note = args[1];
                var channel = args[2];

                var asEvent = (
                    type: \noteOn,
                    timeBeats: time,
                    amp: velocity.asFloat / 127.0,
                    midinote: note,
                    chan: channel
                );

                ("Adding note on event at time: " ++ time).postln;
                eventCollection.addEvent([time, asEvent].postln);
            })
        };

        noteOffMidiFunc = MIDIFunc.noteOff({ |...args|
            var time = clock.beats;
            var velocity = args[0];
            var note = args[1];
            var channel = args[2];

            var asEvent = (
                type: \noteOff,
                timeBeats: time,
                midinote: note,
                chan: channel
            );

            ("Adding note off event at time: " ++ time).postln;
            eventCollection.addEvent([time, asEvent].postln);
        });

        noteOnMidiFunc.disable;
        noteOffMidiFunc.disable;
    }
}
