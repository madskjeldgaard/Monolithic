// An alternative to FoscMusicMaker that takes patterns for the durations and pitches and generates music from that
/*

// Example
(
    var durs = Pseq([1,2,Rest(1),3],inf) / 32;
    var pitches = Pseq([56,50, [56,54,61]],inf);
    var music = FoscPatMusicList.value(durs, pitches, numNotes: 32, scale: Scale.major);
    var voice = FoscVoice.new(music:music, lilypondType:'GrandStaff', name: "Voice 1");
    voice.play;
    voice.show;
)

*/
FoscPatMusicList{
    *new{|durationPattern, pitchPattern, numNotes, scale|
        ^super.new.value(durationPattern, pitchPattern, numNotes, scale)
    }

    // Returns a list of FoscNote, FoscRest and FoscChord
    *value {|durationPattern, pitchPattern, numNotes(8), scale(Scale.major), multiplier|
        var durationStream = durationPattern.asStream;
        var pitchStream = pitchPattern.asStream;
        var outList = [];


        numNotes.do{
            var duration = durationStream.next;
            var pitch = pitchStream.next;
            var isChord = duration.isKindOf(Collection) or: { pitch.isKindOf(Collection) };
            var scaled = scale.notNil;

            var snapToScale = {|midinote, inScale|
                var returnNote = midinote.nearestInScale(inScale);
                // if(returnNote != midinote, {
                //     "Snapping % to scale %, returning %".format(midinote, inScale, returnNote).postln;
                // }, {
                //     "Not snapping % to scale %, returning %".format(midinote, inScale, returnNote).postln;
                // });

                returnNote
            };


            isChord.if({
                // A chord
                var chord;
                var isRest = duration.asArray.any({ arg dur; dur.isRest }) or: { pitch.asArray.any({ arg ppp; ppp.isRest }) };

                isRest.if({
                    // "Detected rest in chord, adding rest in stead".postln;
                    duration = duration.asArray.select({ arg dur; dur.isRest }).first.value;
                    chord = FoscRest.new(writtenDuration:duration, multiplier: multiplier);
                }, {

                    var scaledPitch = scaled.if({
                        pitch.collect{|ppp| snapToScale.(ppp, scale)}
                    }, {
                        pitch
                    });

                    // Convert any rests to FoscRest
                    // scaledPitch = scaledPitch.collect{|ppp| ppp.isRest.if({ ppp.value }, { ppp }) };

                    // "Trying to make a chord consisting of % with durations %".format(scaledPitch, duration).postln;
                    chord = FoscChord.new(writtenPitches:scaledPitch, writtenDuration:duration, multiplier: multiplier);
                    outList = outList.add(chord);

                })
            }, {
                var isRest = duration.isRest or: { pitch.isRest };

                // Just a single note
                isRest.not
                .if({
                    var foscNote;
                    var scaledPitch = scaled.if({
                        snapToScale.(pitch, scale)
                    }, {
                        pitch
                    });

                    foscNote = FoscNote.new(writtenPitch:scaledPitch, writtenDuration:duration, multiplier: multiplier);
                    outList = outList.add(foscNote);
                }, {
                    // Rest
                    var foscRest;
                    var restDuration = duration.isRest.if({ duration.value }, { duration });

                    foscRest = FoscRest.new(writtenDuration:restDuration, multiplier: multiplier);
                    outList = outList.add(foscRest);
                })
            })
        };

        ^outList
    }
}
