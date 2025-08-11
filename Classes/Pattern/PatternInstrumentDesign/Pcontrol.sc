// A namespace of pparams
// Encapsulates an event pattern and a range of parameters that are then made accessible to that pattern
// All is livecodeable
Pcontrol [] {
    var <func;

    var <>params;
    var <>patternProxy, <patternProxyPlayer;
    var <>presets; // Dictionary to store presets
    var <>currentPresetName; // Name of current preset
    var <>interpolationTime = 1.0; // Default interpolation time

    var toggleState = true;

    *new{ arg wrapFunc;
        ^super.new().init(wrapFunc);
    }

    quant_{|newQuant|
        this.patternProxy.quant = newQuant;
        params.keysValuesDo{|k,v| v.quant = newQuant };
    }

    init{|wrapFunc|
        params = IdentityDictionary.new;
        // patternProxy = patternProxy ? EventPatternProxy.new;
        presets = IdentityDictionary.new; // Initialize presets dictionary
        currentPresetName = nil;

        if(wrapFunc.notNil, {
            this.source_(wrapFunc)
        });
    }

    at{|key|
        var res = params[key];
        if(res.isNil, {
            res = Pparam.new(0, [0.0,1.0,\lin].asSpec);
            params.put(key, res);
        });

        ^params[key];
    }

    setRaw{|...keyValuePairs|
        keyValuePairs.arePairs.if({
            keyValuePairs.pairsDo{|key, value|
                this.setRawOne(key, value)
            }
        }, {
            "setRaw expects pairs".warn;
        })
    }

    // Set a raw value of a param
    setRawOne{|key, value|
        if(params[key].notNil, {
            params[key].source = value;
        }, {
            "%: param % not found".format(this.class.name, key).warn;
        })
    }


    incrementRawOne{|key, value|
        if(params[key].notNil, {
            params[key].source = params[key].source + value;
        }, {
            "%: param % not found".format(this.class.name, key).warn;
        })
    }

    // Add a value to a param's existing value
    increment{|...keyValuePairs|
        keyValuePairs.arePairs.if({
            keyValuePairs.pairsDo{|key, value|
                this.incrementRawOne(key, value)
            }
        }, {
            "incrementRaw expects pairs".warn;
        })
    }

    decrement{|...keyValuePairs|
        keyValuePairs.arePairs.if({
            keyValuePairs.pairsDo{|key, value|
                this.increment(key, -1 * value)
            }
        }, {
            "decrement expects pairs".warn;
        })
    }

    map{|...keyValuePairs|
        keyValuePairs.arePairs.if({
            keyValuePairs.pairsDo{|key, value|
                this.mapOne(key, value)
            }
        }, {
            "map expects pairs".warn;
        });
    }

    // Map using a control spec
    mapOne{|key, value|
        if(params[key].notNil, {
            params[key].map(value);
            this.changed(key, [key, value]);
        }, {
            "%: param % not found".format(this.class.name, key).warn;
        })
    }

    source{
        ^patternProxy.source;
    }

    source_{|wrapFunc|
        if(wrapFunc.isKindOf(Function), {
            var result = wrapFunc.value(this);
            func = wrapFunc;

            if(result.isKindOf(Pattern).not, {
                "%: wrapFunc must return a pattern".format(this.class.name).error;
            }, {
                patternProxy.isNil.if({
                    patternProxy = EventPatternProxy.new;
                });

                patternProxy.source = result;
            });

            this.changed(\source, [\source, result]);
        }, {
            "%: wrapFunc must be a function".format(this.class.name).error;
        })
    }

    // If fadeInTime is above 0, it will wrap the proxy source in Pfadein2 and fade in the pattern
    // The fadeChannels argument should correspond to the number of channels in the pattern you are playing.
    play{|clock, quant, fadeInTime = 0, fadeOutTime = 0, fadeChannels, doReset=false|
        fadeChannels = fadeChannels ? Server.local.options.numOutputBusChannels;


        if(patternProxy.isNil, {
            "%: no pattern to play".format(this.class.name).warn;
        }, {

            // Wrap the pattern proxy in a fader if fade in/out times are specified
            (fadeInTime > 0.0 or: { fadeOutTime > 0.0 }).if({
                patternProxy.source = Pfadeinout2.new(patternProxy.source, fadeInTime: fadeInTime, fadeOutTime: fadeOutTime, numChannels: fadeChannels)
            });

            patternProxyPlayer = patternProxy.play(argClock: clock, quant: quant, doReset: doReset);
        });

    }

    stop{
        patternProxyPlayer.isNil.not.if({
            patternProxyPlayer.stop;
        }, {
            "%: no pattern to stop".format(this.class.name).warn;
        })
    }

    toggle{
        if(this.isPlaying, {
            this.stop;
        }, {
            this.play;
        })
    }

    isPlaying{
        ^patternProxyPlayer.isPlaying;
    }

    // Change a pattern key in a Pbind
    // Example:
    /*

    // Change dur of Pbind
    Pctrldef(\yoyoy).change(\dur, 2)

    // Change using past value
    Pctrldef(\yoyoy).change(\dur, Pkey(\dur)*0.5)

    */

    change{|...keyValuePairs|
        keyValuePairs.arePairs.if({
            keyValuePairs.pairsDo{|key, value|
                this.changeOne(key, value)
            }
        }, {
            "change expects pairs".warn;
        })
    }


    changeOne{|patternKey, newValue|
        patternProxy.isNil.not.if({
            patternProxy.source = Pbindf(patternProxy.source, patternKey, newValue)
        })
    }

    addParam{|...keysSourcesSpecs|
        keysSourcesSpecs.areTriplets.if({
            var clumpedArgs = keysSourcesSpecs.clump(3);

            clumpedArgs.do{|args|
                var key = args[0];
                var source = args[1];
                var spec = args[2];

                spec.isKindOf(Array).if({
                    spec = ArrayedSpec.new(array:spec, default:0)
                });

                this.addOneParam(key, source, spec);
            }
        }, {
            "addParam expects triplets: [key, source, spec]".warn;
        })
    }

    addOneParam{|key, source, spec|
        if(params[key].notNil, {
            params[key].source = source;
            params[key].spec = spec ? Spec.specs[key] ? params[key].spec ? [0.0 ,1.0, \lin].asSpec;
        }, {
            var newParam = Pparam.new(source, spec);
            params.put(key, newParam)
        })
    }

    // A convenience for creating a modality callback that will toggle this pattern on/off
    mktlToggleAction{|verbose=true, isMomentary=false|
        ^{|elem|
            verbose.if({
                "%: %".format(this.class.name, elem.value).postln;
            });

            isMomentary.if({
                // Uses a toggle state to fake a latch
                if(elem.value == 1 && toggleState, {
                    "Toggling pattern".postln;
                    this.toggle();

                    // Flip the state
                    toggleState = toggleState.not;
                }, {
                    // Reset the state
                    toggleState = toggleState.not;
                })
            }, {
                // Latching
                if(elem.value == 1, {

                    if(this.isPlaying.not, {
                        verbose.if({
                            "playing".postln
                        });
                        this.play;
                    }, {
                        verbose.if({
                            "already playing.stopping".postln
                        });
                        this.stop;
                    });
                })
            })
        }
    }

    gui{
        ^PcontrolGui.new(this)
    }

    // PRESETS

    // Save current parameter values as a preset
    savePreset {|name|
        var preset = IdentityDictionary.new;

        params.keysDo {|key|
            preset[key] = (
                value: params[key].source,
                spec: params[key].spec
            );
        };

        presets[name] = preset;
        currentPresetName = name;
        ^"Preset '%' saved".format(name).postln;
    }

    // Recall a preset immediately
    recallPreset {|name|
        var preset = presets[name];

        if (preset.notNil) {
            preset.keysDo {|key|
                if (params[key].notNil) {
                    params[key].source = preset[key][\value];
                    params[key].spec = preset[key][\spec];
                    this.changed(key, [key, preset[key][\value]]);
                };
            };
            currentPresetName = name;
            ^"Preset '%' recalled".format(name).postln;
        } {
            ^"Preset '%' not found".format(name).warn;
        };
    }

    // Interpolate between current values and a preset
    interpolateToPreset {|name, time|
        var preset = presets[name];
        var interpolationTime = time ? this.interpolationTime;

        if (preset.notNil) {
            preset.keysDo {|key|
                if (params[key].notNil) {
                    var targetVal = preset[key][\value];
                    var currentVal = params[key].source;

                    // Only interpolate if types match
                    if (targetVal.class == currentVal.class) {
                        params[key].source = currentVal.blend(targetVal, interpolationTime);
                    } {
                        // If types don't match, just set immediately
                        params[key].source = targetVal;
                    };

                    // Always update spec
                    params[key].spec = preset[key][\spec];
                };
            };
            currentPresetName = name;
            ^"Interpolating to preset '%' over % seconds".format(name, interpolationTime).postln;
        } {
            ^"Preset '%' not found".format(name).warn;
        };
    }

    // Get list of preset names
    getPresetNames {
        ^presets.keys.asArray;
    }

    // Delete a preset
    deletePreset {|name|
        if (presets[name].notNil) {
            presets.removeAt(name);
            if (currentPresetName == name) { currentPresetName = nil };
            ^"Preset '%' deleted".format(name).postln;
        } {
            ^"Preset '%' not found".format(name).warn;
        };
    }

    // Save all presets to disk
    savePresetsToFile {|path|
        var file = File(path, "w");
        var data = (
            presets: presets,
            interpolationTime: interpolationTime
        );

        file.write(data.asCompileString);
        file.close;
        ^"Presets saved to %".format(path).postln;
    }

    // Load presets from disk
    loadPresetsFromFile {|path|
        var file = File(path, "r");
        var data, newPresets;

        if (file.isOpen) {
            data = file.readAllString.interpret;
            file.close;

            if (data[\presets].notNil) {
                presets = data[\presets];
                interpolationTime = data[\interpolationTime] ? 1.0;
                ^"Presets loaded from %".format(path).postln;
            } {
                ^"No presets found in file".warn;
            };
        } {
            ^"Could not open file %".format(path).warn;
        };
    }

    // MIDI
    exportAsMidi{|file, dur, tempoBPM=120, maxEvents=10000000|
        var pattern = this.patternProxy.source;
        var midiFile;

        if(dur.notNil, {
            pattern = Pfindur.new(dur, pattern);
        });

        midiFile = SimpleMIDIFile.fromPattern(pattern: pattern, inTempo: tempoBPM,  maxEvents: maxEvents);

        midiFile.write(file.asAbsolutePath);

    }

    randomize{|...paramKeys|
        paramKeys.asArray.do{|key|
            if(params[key].notNil, {
                params[key].randomize();
                this.changed(key, [key, params[key].source]);

            }, {
                "%: param % not found".format(this.class.name, key).warn;
            })
        }
    }

    randomizeAll{
        this.randomize(*params.keys.asArray);
    }


}
