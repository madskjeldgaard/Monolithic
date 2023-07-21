// The same as a pattern proxy (pdefn) but with a spec and mapping capabilities
/*
(
p = Pparam.new(500, Spec.specs[\freq]);
p.map(0.2);
p.source.postln
)
*/
Pparam : PatternProxy{
    var <>spec;

    *new{|source, controlspec|
        ^super.new().source_(source).spec_(controlspec ? [0.0,1.0,\lin].asSpec)
    }

    // Uses a spec to map it's values (yes, I know, it overwrites original map)
    map{|value|
        this.source = spec.map(value);
    }

}

// A namespace of pparams
// Encapsulates an event pattern and a range of parameters that are then made accessible to that pattern
// All is livecodeable
Pcontrol [] {
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
        this.source_(wrapFunc);
    }

    at{|key|
        var res = params[key];
        if(res.isNil, {
            res = Pparam.new(0, [0.0,1.0,\lin].asSpec);
            params.put(key, res);
        });

        ^params[key];
    }

    // Set a raw value of a param
    setRaw{|key, value|
        if(params[key].notNil, {
            params[key].source = value;
        }, {
            "%: param % not found".format(this.class.name, key).warn;
        })
    }

    // Map using a control spec
    map{|key, value|
        if(params[key].notNil, {
            params[key].map(value);
        }, {
            "%: param % not found".format(this.class.name, key).warn;
        })
    }

    source_{|wrapFunc|
        if(wrapFunc.isKindOf(Function), {
            var result = wrapFunc.value(this);
            if(result.isKindOf(Pattern).not, {
                "%: wrapFunc must return a pattern".format(this.class.name).error;
            }, {
                // result = wrapFunc.value(this);

                if(result.isKindOf(EventPatternProxy).not, {
                    patternProxy = EventPatternProxy.new(result);
                }, {
                    patternProxy = result;
                })

            })
        }, {
            "%: wrapFunc must be a function".format(this.class.name).error;
        })
    }

    play{
        patternProxyPlayer = patternProxy.play;
    }

    stop{
        patternProxyPlayer.isNil.not.if({
            patternProxyPlayer.stop;
        })
    }

    // Change a pattern key in a Pbind
    // Example:
    /*

    // Change dur of Pbind
    Pctrldef(\yoyoy).change(\dur, 2)

    // Change using past value
    Pctrldef(\yoyoy).change(\dur, Pkey(\dur)*0.5)

     */
    change{|patternKey, newValue|
        patternProxy.isNil.not.if({
            patternProxy.source = Pbindf(patternProxy.source, patternKey, newValue)
        })
    }

    addParam{|key, source, spec|
        if(params[key].notNil, {
            params[key].source = source;
            params[key].spec = spec ? Spec.specs[key] ? params[key].spec ? [0.0 ,1.0, \lin].asSpec;
        }, {
            var newParam = Pparam.new(source, spec);
            params.put(key, newParam)
        })
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
        ^this.class.new(toKey).copyState(this)
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
