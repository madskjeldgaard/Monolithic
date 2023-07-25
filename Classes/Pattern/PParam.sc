// The same as a pattern proxy (pdefn) but with a spec and mapping capabilities;
/*
(
p = Pparam.new(500, Spec.specs[\freq]);
p.map(0.2);
p.source.postln
)

// Using arrayed spec !
(
Pctrldef(\yo)
.addParam(
    \myArrayParam, \hey, [\hey, \yo, \ho]
);

Pctrldef(\yo)[\myArrayParam].source.postln;
Pctrldef(\yo).map(\myArrayParam, 0.5);
Pctrldef(\yo)[\myArrayParam].source.postln;
)

*/
Pparam : PatternProxy{
    var <spec;

    *new{|source, controlspec|
        ^super.new().source_(source).spec_(controlspec ? [ 0.0,1.0,\lin])
    }

    copy{
        ^this.class.new(this.source, this.spec).envir_(this.envir.copy).spec_(this.spec)
    }

	// copy {
	// 	^super.copy.copyState(this)
	// }

	copyState { |proxy|
		envir = proxy.envir.copy;
		this.source = proxy.source;
	}

    spec_{|newSpec|
        spec = newSpec.asSpec;
    }

    // Uses a spec to map it's values (yes, I know, it overwrites original map)
    map{|value|
        if(spec.notNil, {

            var mapped = spec.map(value);
            var step = spec.step;

            this.source = mapped;

        }, {
            "No spec found for %. Using unipolar".format(this.class.name).warn;

            this.source = \uni.asSpec.map(value);
        })
    }

    // Convenience method to make it super easy to map a MKtl / modality toolkit element to control this parameter
    mktlAction{|verbose=true|
        ^{|elem|
            var value = elem.value;

            verbose.if({
                "%: %".format(this.class.name, value).postln;
            });

            this.map(value);
        }
    }

}

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
                "Setting new pattern as source for %".format(this.class.name).postln;
                patternProxy = EventPatternProxy.new(result);
            })
        }, {
            "%: wrapFunc must be a function".format(this.class.name).error;
        })
    }

    play{
        if(patternProxy.isNil, {
            "%: no pattern to play".format(this.class.name).warn;
        });

        patternProxyPlayer = patternProxy.play;
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

// Same as above but pdef style
//
/*
(
Pctrldef(\yoyoy, {|ctrl|
    Pbind(\instrument, \default, \dur, 0.25, \degree, ctrl[\degree].trace)
});

Pctrldef(\yoyoy).addParam(\degree, 0, ControlSpec.new(minval:0, maxval:7, warp:\lin, step:1, default:4));
Pctrldef(\yoyoy).play;

r{
    loop{
        1.wait;
        Pctrldef(\yoyoy).map(\degree, rrand(0.0,1.0))
    }
}.play;
)
*/
Pctrldef : Pcontrol{

    var <key;

    classvar <>all;

    *new{ arg key, item;
		var res = this.at(key);
		if(res.isNil) {
			res = super.new(item).prAdd(key);
		} {
			if(item.notNil) { res.source = item }
		}
		^res

	}

    *at{|key|
        ^all[key]
    }

    prAdd { arg argKey;
        key = argKey;
        all.put(argKey, this);
    }

    copy { |toKey|
        if(toKey.isNil or: { key == toKey }) { Error("can only copy to new key (key is %)".format(toKey)).throw };
        "Copying from key % to %".format(key, toKey).postln;
        ^this.class.new(toKey).copyState(this)
    }

    copyState { |otherPctrldef|
        if(otherPctrldef.patternProxy.source.isNil, {
            "%: no pattern to copy".format(this.class.name).warn;
        });

        // this.patternProxy.isNil.if({
        //     this.patternProxy = EventPatternProxy.new(otherPctrldef.patternProxy.source.copy());
        // }, {
        //     this.patternProxy.source = otherPctrldef.patternProxy.source;
        // });


        // this.patternProxy.envir = otherPctrldef.patternProxy.envir.copy;

        this.params = otherPctrldef.params.collect{|param| param.copy()};
        this.source_(otherPctrldef.func);
    }

    // Convenience – copy and immediately change bits of the pattern
    copyChange{ |toKey ... changeKeyValues|
        var newPctrlDef = this.copy(toKey);

        if(changeKeyValues.arePairs, {
            newPctrlDef.change(*changeKeyValues)
        }, {
            "can't set changekeyvalues if not pairs".error;
        });

        ^newPctrlDef
    }

    dup { |n = 2| ^{ this }.dup(n) } // avoid copy in Object::dup

    *hasGlobalDictionary { ^true }

    *initClass {
        all = IdentityDictionary.new;
        Class.initClassTree(Pdef);
    }

    clear{
        patternProxy.clear();
        params.keysValuesDo{|k,v| v.clear()}
    }
}
