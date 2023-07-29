// Generates a Pbind for a given synth def using all of the synth def's controls
PbindGenerator{
    *new{|synthDefName=\default, wrapInPdef=true, randomize=false|
        this.synthDefExists(synthDefName).if({
            this.postPatFor(synthDefName, wrapInPdef, randomize)
        })
    }

    *synthDefExists{|synthDefName|
        ^SynthDescLib.global.synthDescs.at(synthDefName).isNil.not;
    }

    *postPatFor {|synthDef=\default, wrapInPdef=true, randomize=true|
        var controls = SynthDescLib.global.synthDescs.at(synthDef).controls;

        if(wrapInPdef, {"Pdef('%', ".format(Date.getDate.stamp).postln});
        if(wrapInPdef, "\t".post);
        "Pbind(".postln;
            if(wrapInPdef, "\t".post);
            "\t%instrument, %%,".format("\\", "\\", synthDef.asSymbol).postln;
            controls.do{|control|
                var name = control.name;
                var val = control.defaultValue;

                // Check that synth doesn't have a duration of 0 by default (making sc explode)
                val = if(name == \dur && val == 0.0, { 1.0 }, { val });
                val = if(randomize && val.isKindOf(Number), { val * rrand(0.9,1.1) }, { val });

                if(wrapInPdef, "\t".post);
                "\t%%, %,".format("\\", name, val).postln
            };
            if(wrapInPdef, {"\t)\n)".postln}, {")".postln});
        }
    }
