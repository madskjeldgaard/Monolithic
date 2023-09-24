SceneDef : PatchDef{
    classvar <>all;
    var <data;

    *new{ arg key, configFunc, playFunc, stopFunc, cleanupFunc;
        var res = this.at(key);
        if(res.isNil) {
            "Creating new %: %".format(this.name, key).postln;
            res = super.new().prAdd(key).configFunc_(configFunc).playFunc_(playFunc).stopFunc_(stopFunc).cleanupFunc_(cleanupFunc);
            res.data = IdentityDictionary.new;
        } {
            "Not creating new patch def at % but modifying it.".format(key).postln;
            if(configFunc.notNil) { res.configFunc_(configFunc) };
            if(playFunc.notNil) { res.playFunc_(playFunc) };
            if(stopFunc.notNil) { res.stopFunc_(stopFunc) };
            if(cleanupFunc.notNil) { res.cleanupFunc_(cleanupFunc) };
        };

        ^res
    }
    *initClass {
        all = IdentityDictionary.new;
    }

    *at{|key|
        ^all[key]
    }
    *hasGlobalDictionary { ^true }

    prAdd { arg argKey;
        key = argKey;
        all.put(argKey, this);
    }
}
