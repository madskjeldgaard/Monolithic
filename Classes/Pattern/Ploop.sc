/*

This class allows playing an event pattern with a sort of looping functionality.

It records the last X number of events from the input pattern when recording is on (1), and plays them when playing is on (1). Note that you can both record and play at the same time, which will continually fill up the input buffer while playing. Most often you want to toggle recording off when playing and vice-versa.

// Example usage:
(
(
// When this is 1, it plays the events in the buffer, when it is 0, it records them
var playing = Pstep([0, 1],4,inf);
var maxNumEvents = 4; // The number of events to loop/record

// The pattern to record/play. Just a melody with the first 10 degrees of the default scale.
p = Pbind(\degree, Pseq((0..9), inf), \dur, 0.125);

// Pass the Pbind through the looper
p = Ploop.new(p, maxEvents: maxNumEvents, recording: 1 - playing, playing: playing);

// play
p.play;
)

*/
Ploop : Pattern {
    var <>pattern, <>maxEvents, <>recording, <>playing;
    var eventBuffer;

    *new { |pattern, maxEvents = 4, recording(Pseq([1], inf)), playing(Pseq([0], inf))|
        ^super.newCopyArgs(pattern, maxEvents, recording, playing)
    }

    storeArgs { ^[pattern, maxEvents, recording, playing] }

    embedInStream { |inval|
        var stream = pattern.asStream;
        var recordingStream = recording.asStream;
        var playingStream = playing.asStream;
        var event;
        eventBuffer = List.newClear(maxEvents);

        loop {
            var isRecording = recordingStream.next(inval);
            var isPlaying = playingStream.next(inval);

            if (isRecording == 1) {
                event = stream.next(inval);
                if (event.notNil) {
                    if (eventBuffer.size < maxEvents, {
                        eventBuffer = eventBuffer.add(event);
                    },{
                        var bufferTail = eventBuffer[1..];

                        // Buffer is full, remove the oldest event and add the new one
                        eventBuffer = [bufferTail, event].flatten;
                    })
                }
            };

            if (isPlaying == 1) {
                eventBuffer.do { |e|
                    inval = e.yield;
                };
            } {
                inval = event.yield;
            }
        }
    }
}
