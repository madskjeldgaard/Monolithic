/*
* The main point of these classes is to be used as bridges between sensor inputs and a midi target, they allow soloing midi streams (to allow mapping certain cc values in a DAW for example) and rate limiting the output, as to avoid spamming the target.

// TODO:
- Randomize

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
    var <name, <midiout, <midiChannel, <ccnum, <mute, <solo, <timeOfLast, <timeLimitThreshold, <lastVal=0, <>onlySendNewValues=true, <minVal=0, <maxVal=127, dependant, soloFunc, muteFunc;

    *new{ arg name, midiout, midiChannel, ccnum, timeLimitSeconds = 0.01;
        ^super.newCopyArgs(name, midiout, midiChannel, ccnum, false, false, Date.getDate.rawSeconds, timeLimitSeconds);
    }

    send{ arg val;
        if(mute.not, {
            var timeSinceLast = Date.getDate.rawSeconds - timeOfLast;

            if(timeSinceLast > timeLimitThreshold, {

                if(onlySendNewValues.not or: (onlySendNewValues and: val != lastVal), {
                    midiout.control(midiChannel, ccnum, val);
                    timeOfLast = Date.getDate.rawSeconds;
                    lastVal = val;
                    this.changed('value', val);
                })
            })
        })
    }

    mute_{|enabled=true|
        mute = enabled;
        // muteFunc.value(mute);
        this.changed('mute', mute);
    }

    solo_{|enabled=true|
        solo = enabled;
        // soloFunc.value(solo);
        this.changed('solo', solo);
    }

    // doOnSolo{ arg func;
    //     soloFunc = func;
    // }

    // doOnMute{ arg func;
    //     muteFunc = func;


    map{|normalizedVal|
        ^normalizedVal.linlin(0.0,1.0,minVal,maxVal)
    }

    unmap{|val|
        ^val.linlin(minVal,maxVal,0.0,1.0)
    }

    asGUILayout{|window|
        var nameLabel = StaticText.new(window).string_(name);
        var midiInfoLabel = StaticText.new(window).string_("chan: %, cc: %".format(midiChannel+1, ccnum+1));

        var muteButton = Button.new(window)
        .states_([
            ["Mute", Color.black],
            ["Muted", Color.red],
        ])
        .value_(
            if(mute, {1}, {0})
        )
        .action_({|obj|
            var val = obj.value;

            if(val == 1, {
                this.mute = true;
            }, {
                this.mute = false;
            })
        });

        // var soloButton = Button.new(window)
        // .states_([
        //     ["Solo", Color.black],
        //     ["Soloed", Color.blue]
        // ]).action_({|obj|
        //     var val = obj.value;

        //     if(val == 1, {
        //         this.solo = true;
        //     }, {
        //         this.solo = false;
        //     })
        // });

        var valueSlider = Slider.new(window)
        .orientation_(\horizontal)
        .value_(0)
        .action_({|obj|
            var val = this.map(obj.value);
            this.send(val);
        });

        var newLayout = HLayout.new(
            [nameLabel, a: \left, s:2],
            [midiInfoLabel, a: \left, s:2],
            [valueSlider, s: 8],
            [muteButton, s: 1],
            // soloButton,
        );

        dependant = {|changer, whatChanged|

            whatChanged.switch(
                \solo, {
                    // soloButton.value = solo.if({1},{0});
                },
                \mute, {
                    muteButton.value = mute.if({1},{0});
                },
                \value, {
                    valueSlider.value = this.unmap(lastVal);
                }
            )
        };

        this.addDependant(dependant);
        ^newLayout
    }

    gui{
        var window = Window.new("MidiCCTrack: %".format(name), bounds: Rect(0,0,Window.availableBounds.width.asInteger*0.75,100), scroll: false);
        window.layout = this.asGUILayout(window);
        window.front;
        window.onClose_({
            this.removeDependant(dependant);
        })
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
        // track.doOnSolo({|val| this.solo(name)});
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
    solo{ arg name, enabled=true;
        var track = this.at(name);

        if(track.notNil, {

            if(enabled, {
                // Set all tracks to mute, except for this one
                var muteTracks = tracks.values.reject{|t| t == track};

                muteTracks.do{|t|
                    if(t.solo.not, {
                        t.mute = true;
                    })
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

    asGUILayout{|window|
        var trackGUIs = tracks.values.collect{|t|
            var soloButton = Button.new(window)
            .states_([
                ["Solo", Color.black],
                ["Soloed", Color.blue]
            ])
            .value_(
                if(t.solo, {1}, {0})
            )
            .action_({|obj|
                var val = obj.value;

                if(val == 1, {
                    // Solo this track
                    this.solo(t.name);

                    // unsolo all other tracks
                    tracks.values.reject{|track| track == t}.do{|track|
                        this.solo(false);
                    };

                }, {
                    this.solo(t.name, false);
                })
            });

            var trackLayout = t.asGUILayout(window);

            HLayout(*[soloButton, trackLayout]);
        };

        var newLayout = VLayout(*trackGUIs);

        ^newLayout
    }

    gui{
        var window = Window.new("MidiStreamer: %".format(name), Rect(0,0,Window.availableBounds.width.asInteger*0.66, 150), scroll: true);
        var layout = this.asGUILayout(window);
        // var soloDependant = {|changer, whatChanged|
        //     if(whatChanged == \solo, {
        //     })
        // };

        // tracks.keysValuesDo{|name, track|
        //     track.addDependant(soloDependant);
        // };

        window.layout = layout;
        window.front;

        window.onClose_({
            tracks.keysValuesDo{|name, track|
                track.removeDependant(track.dependant);
                // track.removeDependant(soloDependant);
            }
        })
    }

}
