/*
* The main point of these classes is to be used as bridges between sensor inputs and a midi target, they allow soloing midi streams (to allow mapping certain cc values in a DAW for example) and rate limiting the output, as to avoid spamming the target.
*
*/

/*

// Example:
(
// Setup
~iac = ~iac ?? {IAC.new("Bus 1")};

m = MidiStreamer.new("BitWig1", tracks: [
    MidiCCTrack.new("filterCutoff", midiout: ~iac.midiout, midiChannel: 11, ccnum: 1, timeLimitSeconds: 0.01),
    MidiCCTrack.new("filterResonance", midiout: ~iac.midiout, midiChannel: 11, ccnum: 2, timeLimitSeconds: 0.01),
    MidiCCTrack.new("oscFreq", midiout: ~iac.midiout, midiChannel: 11, ccnum: 3, timeLimitSeconds: 0.01),
]);

)

// Send random values to all tracks
r = Routine.new({loop{m.tracks.do{|track| track.send(rrand(0,127))}; 0.1.wait}}).play;

// Solo one of them to map it or something
m.solo('filterResonance');
m.at('filterResonance').send(20);

// Solo another one and do the same
m.solo('oscFreq');
m.at('oscFreq').send(20);

*/


// A data class that holds a value and a timestamp for when it was last updated (used for rate limiting)
// A midi track takes a value and sends it to a midiout as a midi cc value, it can be muted and soloed and has a name.
MidiCCTrack{
    var <name, <midiout, <midiChannel, <ccnum, <>mute, <>solo, <timeOfLast, <timeLimitThreshold;

    *new{ arg name, midiout, midiChannel, ccnum, timeLimitSeconds = 0.01;
        ^super.newCopyArgs(name, midiout, midiChannel, ccnum, false, false, Date.getDate.rawSeconds, timeLimitSeconds);
    }

    send{ arg val;
        if(mute.not, {
            var timeSinceLast = Date.getDate.rawSeconds - timeOfLast;

            if(timeSinceLast > timeLimitThreshold, {
                midiout.control(midiChannel, ccnum, val);
                timeOfLast = Date.getDate.rawSeconds;
            })
        });
    }
}

// MidiNoteOnOffTrack{
//     var <name, <midiout, <midiChannel, <noteNum, <>mute, <>solo, <timeOfLast, <timeLimitThreshold;

//     *new{ arg name, midiout, midiChannel, noteNum, timeLimitSeconds = 0.01;
//         ^super.newCopyArgs(name, midiout, midiChannel, noteNum, false, false, Date.getDate.rawSeconds, timeLimit);
//     }

//     send{ arg val;
//         if(mute.not, {
//             var timeSinceLast = Date.getDate.rawSeconds - timeOfLast;

//             if(timeSinceLast > timeLimitThreshold, {
//                 midiout.noteOn(midiChannel, noteNum, val);
//                 timeOfLast = Date.getDate.rawSeconds;
//             })
//         });
//     }
// }

// Contains several MidiCCTracks
MidiStreamer {
    var  <name, <tracks;

    *new{ arg name, tracks;
        ^super.newCopyArgs(name).init(tracks);
    }

    init{ arg inTracks;
        tracks = IdentityDictionary.new();

        inTracks.do{ arg track;
            this.addTrack(track)
        };
    }

    at{ arg name;
        ^tracks[name.asSymbol];
    }

    addTrack{ arg track;
        var name = track.name.asSymbol;
        tracks.put(name, track);
    }

    send{ arg name, val;
        var track = this.at(name);
        if(track.notNil, {
            track.send(val);
        },{
            "MidiStreamer: track not found".warn;
        })
    }

    // This is a toggle that will mute all tracks except for the one specified by name
    // If the track is already soloed, it will unmute all tracks
    solo{ arg name;
        var track = this.at(name);

        if(track.notNil, {

            if(track.solo.not, {
                // Set all tracks to mute, except for this one
                var muteTracks = tracks.values.reject{|t| t == track};

                muteTracks.do{|t|
                    t.mute = true;
                    t.solo = false;
                };

                "Soloing track %".format(track.name).postln;
                track.mute = false;
                track.solo = true;

            }, {
                "Unsoloing track %".format(track.name).postln;
                // Unmute all tracks
                tracks.values.do{|t|
                    t.mute = false;
                    t.solo = false;
                };
            })
        },{
            "MidiStreamer: track not found".warn;
        })
    }

}
