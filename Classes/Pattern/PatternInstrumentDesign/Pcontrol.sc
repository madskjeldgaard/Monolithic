// A namespace of pparams
// Encapsulates an event pattern and a range of parameters that are then made accessible to that pattern
// All is livecodeable
Pcontrol [] {
    var <func;

    var <>params;
    var <>patternProxy, <patternProxyPlayer;

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
            })
        }, {
            "%: wrapFunc must be a function".format(this.class.name).error;
        })
    }

    play{|clock, quant, doReset=false|
        if(patternProxy.isNil, {
            "%: no pattern to play".format(this.class.name).warn;
        });

        patternProxyPlayer = patternProxy.play(argClock: clock, quant: quant, doReset: doReset);
    }

    stop{
        patternProxyPlayer.isNil.not.if({
            patternProxyPlayer.stop;
        }, {
            "%: no pattern to stop".format(this.class.name).warn;
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
    mktlToggleAction{|verbose=true|
        ^{|elem|
            verbose.if({
                "%: %".format(this.class.name, elem.value).postln;
            });

            if(elem.value == 1, {
                verbose.if({
                    "playing".postln
                });

                this.play;
            }, {
                verbose.if({
                    "stopping".postln
                });
                this.stop;
            })
        }
    }
}
