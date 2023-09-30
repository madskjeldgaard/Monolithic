/*

Nice way of fading sound based patterns,
it wraps the patterns in a group with an fx synth at the end that plays the pattern through a fadein/out envelope.

This way, you can play patterns with really long notes and they will fade in and out nicely according to the fade time.

*/
Pfadein2{

    *new{|inPattern, numChannels=2, fadeTime=10|
        ^super.new.init(inPattern, numChannels, fadeTime);
    }

    *initClass{
        Class.initClassTree(SynthDescLib);
        Class.initClassTree(Server);

        StartUp.add({

            (1..64).do{|numChannels|
                var name = "fadeinsynth%".format(numChannels).asSymbol;

                name.synthdefBinaryExists.not.if{
                    "Pfadein synth % doesn't exist, creating it now.".format(name).postln;

                    SynthDef(name, {
                        var out = \out.kr(0);
                        var sig = In.ar(bus:out, numChannels:numChannels);
                        var gate = \gate.ar(1);
                        var fadetime = \xfadetime.kr(4.0);
                        var env = Env.fadein(fadeTime:fadetime, sustainLevel:1);
                        env = env.ar(gate: gate, doneAction: Done.freeGroup);

                        ReplaceOut.ar(bus:out, channelsArray:sig * env);

                    }).store;
                }
            }

        })

    }

    init{|inPattern, numChannels, fadeTime|
        ^Pgroup(Pfx(inPattern, "fadeinsynth%".format(numChannels).asSymbol, \xfadetime, fadeTime));
    }

}

Pfadeinout2{
    *new{|inPattern, numChannels=2, fadeTime=10|
        ^super.new.init(inPattern, numChannels, fadeTime);
    }

    *initClass{

        Class.initClassTree(SynthDescLib);
        Class.initClassTree(Server);

        StartUp.add({
            (1..64).do{|numChannels|
                var name = "fadeinoutsynth%".format(numChannels).asSymbol;

                name.synthdefBinaryExists.not.if({
                    "Pfadeinout synth % doesn't exist, creating it now.".format(name).postln;
                    SynthDef(name, {
                        var out = \out.kr(0);
                        var sig = In.ar(bus:out, numChannels:numChannels);
                        var gate = \gate.ar(1);
                        var fadetime = \xfadetime.kr(4.0);
                        var env = Env.gatefade(fadeTime:fadetime, sustainLevel:1);
                        env = env.ar(gate: gate, doneAction: Done.freeGroup);
                        ReplaceOut.ar(bus:out, channelsArray:sig * env);

                    }).store;
                })
            }
        })


    }

    init{|inPattern, numChannels, fadeTime|
        ^Pgroup(Pfx(inPattern, "fadeinoutsynth%".format(numChannels).asSymbol, \xfadetime, fadeTime));
    }
}
