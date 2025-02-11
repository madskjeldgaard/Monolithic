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
        ^Pgroup(Pfxb(inPattern, "fadeinsynth%".format(numChannels).asSymbol, \xfadetime, fadeTime));
    }

}

/*
(
p = Pfadeinout2(Pbind(\degree, Pwhite(0,10), \dur, 4.0, \legato, 2), fadeInTime: 10, fadeOutTime: 2);
)
p = p.play;
p.stop;

*/
Pfadeinout2{
    *new{|inPattern, numChannels=2, fadeInTime=10, fadeOutTime=1|
        ^super.new.init(inPattern, numChannels, fadeInTime, fadeOutTime);
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
                        var fadeInTime = \fadeInTime.kr(4.0);
                        var fadeOutTime = \fadeOutTime.kr(4.0);
                        var env = Env.fadeinout(fadeInTime:fadeInTime, fadeOutTime: fadeOutTime, sustainLevel:1);
                        env = env.ar(gate: gate, doneAction: Done.freeGroup);
                        ReplaceOut.ar(bus:out, channelsArray:sig * env);

                    }).store;
                })
            }
        })

    }

    init{|inPattern, numChannels, fadeInTime, fadeOutTime|
        var fxed = Pfx(inPattern, "fadeinoutsynth%".format(numChannels).asSymbol, \fadeInTime, fadeInTime, \fadeOutTime, fadeOutTime);
        ^Pbus(fxed, dur: fadeOutTime + 0.1);
    }
}
