/*

Contains parameters as a dictionary, and calls a callback function when params are changed, to allow setting a synth or something else.

This is useful to simplify maintaining state in between synths spawning and dying.

EXAMPLE:

(
s.waitForBoot{
    SynthDef(\sine, {|freq=440, amp=0.5| Out.ar(0, SinOsc.ar(freq)*amp)}).add;
    s.sync;

    z = Synth(\sine, [\freq, 444, \amp, 0.5]);

    p = Params.new(
        defaultParams: [\amp, 0.5, \freq, 444],
        callback: {| obj, params|
            // Post info
            "PARAMS CHANGED:\nparamsdict: %, node: %, paramObj: %".format(params, paramObj).postln;

            // Set synth
            obj.apply(z);
        }
    );
}
)

// Change parameters
p.set(\freq, 500)
p.set(\freq, 100)
p.set(\freq, 150, \amp, 0.1)


*/
Params {
    var <params;
    var <callbackFunction;

    *new{|defaultParams, callback|
        ^super.new.init(callback, defaultParams);
    }

    init{|callback, keyValuePairs|
        callbackFunction = callback;
        params = Dictionary.new;
        keyValuePairs.postln;
        this.setParams(*keyValuePairs.flatten);
    }

    set{|...params|
        this.setParams(*params);
    }

    setParams{|...keyValuePairs|
        var dict = keyValuePairs.asDict;
        params.putAll(dict);
        callbackFunction.value(this, params.asKeyValuePairs);
    }

    setCallback{|callback|
        callbackFunction = callback;
    }

    at{|key|
        ^params[key];
    }

    clear{|key|
        params.remove(key);
    }

    clearAll{
        params.clear;
    }

    // Unfolds and sets a synth or object that responds to .set
    // Exclude is an optional list of keys to exclude from the set
    apply{|target, exclude|
        if(target.respondsTo(\set)){
            if(exclude.notNil){
                var dict = params.copy;

                exclude.do{|key|
                    dict.remove(key);
                };

                target.set(*dict.asKeyValuePairs);
            }{
                target.set(*params.asKeyValuePairs);
            }
        } {
            "%: Target does not respond to .set()".format(this.class.name).error
        }
    }

}
