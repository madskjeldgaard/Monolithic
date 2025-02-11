/*


Convert an event pattern to a list of Fosc objects,
allowing to create lilypond scores from SuperCollider patterns.

// Example usage
(
var eventPattern = Pbind(
    \degree, Pseq([0, 2, 4, 5, 7], inf),
    \dur, Pseq([0.5, 0.5, 1, 1.5, 2], inf)
);

var foscList = EventPattern2Fosc.new(eventPattern, 10);

v = FoscVoice.new(music:foscList, lilypondType:'PianoStaff', name: "Voice 1");

v.show;

)

TODO:
- support modal transposition (mtranspose)
- This class should really have some automatic tests to ensure it works as expected.

*/

EventPattern2Fosc {
    classvar <instance;

    // Constructor
    *new {|eventPattern, numEvents=32|
        ^super.new.init(eventPattern, numEvents);
    }

    // Initialize the instance
    init { |eventPattern, numEvents|
        instance = this;

        ^this.convert(eventPattern, numEvents);
    }

    // Convert an event pattern to a list of Fosc objects
    convert {|eventPattern, numEvents=32|
        var stream = eventPattern.asStream;
        var outList = [];

        numEvents.do {
            var event = stream.next(());

            var pitch = instance.resolveEventPitch(event);
            var duration = instance.resolveEventDuration(event);

            // If there is more than one pitch or duration, it's a chord
            var isChord = duration.isKindOf(Collection) or: { pitch.isKindOf(Collection) };

            isChord.if({
                // A chord
                var chord = nil;
                var isRest = duration.isRest or: { pitch.isRest };

                isRest.if({
                    // If the duration is a list, get the first element
                    var restDuration = if(duration.isKindOf(Collection), {
                        duration.first;
                    }, {
                        duration;
                    });

                    // Make the fosc chord
                    chord = FoscRest.new(writtenDuration: duration.value);
                }, {
                    // Resolve the duration for the pitches
                    // Make the fosc chord
                    chord = FoscChord.new(writtenPitches: pitch, writtenDuration: duration);
                });

                outList = outList.add(chord);
            }, {
                var isRest = duration.isRest or: { pitch.isRest };

                // Just a single note
                isRest.not.if({
                    var foscNote = FoscNote.new(writtenPitch: pitch, writtenDuration: duration);
                    outList = outList.add(foscNote);
                }, {
                    // Rest
                    var foscRest;
                    var restDuration = duration.isRest.if({ duration.value }, { duration });

                    foscRest = FoscRest.new(writtenDuration: restDuration);
                    outList = outList.add(foscRest);
                })
            })
        };

        ^outList
    }

    // Resolve the pitch of an event
    resolveEventPitch {|event|
        var degree = event['degree'] ? 0;
        var root = event['root'] ? 0;
        var scale = event['scale'];
        var ctranspose = event['ctranspose'] ? 0;
        var mtranspose = event['mtranspose'] ? 0;
        var hasChromaticTransposition = ctranspose.notNil and: { ctranspose != 0 };
        var hasModalTransposition = mtranspose.notNil and: { mtranspose != 0 };
        var hasScale = scale.notNil;
        var resolvedPitch = 0;

        resolvedPitch = degree.notNil.if({
            var scale = event['scale'];
            var octave = 5;
            var scaledPitch = 0;

            if(event['octave'].notNil, {
                octave = event['octave'];
            });

            // Resolve the scale
            if(scale.notNil, {
                scale = scale;
            }, {
                scale = Scale.major;
            });

            // Resolve the octave
            if(scale.notNil, {
                var numNotesInOctave = scale.pitchesPerOctave;
                octave = octave * numNotesInOctave;
            }, {
                // Default to 12 notes in an octave
                octave = octave * 12;
            });

            // If the degree is outside the octave, add octave offset
            octave = octave + (degree / scale.pitchesPerOctave).asInteger;

            // Resolve the pitch from the degree + octave
            scaledPitch = scale.degrees.wrapAt(degree) + octave + root;

            // Prioritize modal transposition (mtranspose)
            if(hasModalTransposition, {
                var scaleDegrees = mtranspose.isArray.if({
                    mtranspose.collect { |mt| scale.at(mt) }
                }, {
                    [scale.at(mtranspose)]
                });

                var numNotesInOctave = scale.size;

                // If the modal transposition goes beyond the octave, add octave offset
                scaleDegrees = scaleDegrees.collect { |scaleDegree|
                    var octaveOverFlow  = (scaleDegree / numNotesInOctave).asInteger;

                    // as semitones
                    octaveOverFlow = octaveOverFlow * 12;

                    // Correct scale degree for the octave overflow
                    scaleDegree + octaveOverFlow
                };

                scaledPitch = scaledPitch + scaleDegrees;

            }, {
                // Chromatic transposition (semitones)
                if(hasChromaticTransposition, {
                    scaledPitch = scaledPitch + ctranspose;
                });
            });

            scaledPitch

        }, {
            "Could not resolve pitch".warn;
            60
        });

        ^resolvedPitch;
    }

    // Resolve the duration of an event
    resolveEventDuration {|event|
        var dur = event['dur'] ? event['delta'];
        var isRest = dur.isRest;

        ^dur.notNil.if({
            dur = dur;
        }, {
            // Default to 1
            dur = 1;
        });
    }

}
