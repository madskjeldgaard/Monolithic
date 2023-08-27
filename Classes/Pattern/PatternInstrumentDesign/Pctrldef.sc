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

    solo{
        all.select({|pdefctrl| pdefctrl != this}).keysValuesDo{|k,v| v.stop};
        this.play;
    }
}
