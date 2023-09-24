// Fades in a pattern by wrapping it in an fx synth
// The advantage of this over PfadeIn is it doesn't require an \amp argument in the synth  playing in the pattern
// The disadvantage is that it needs to know the number of channels
Pfadein2{

    *new{|inPattern, numChannels=2, fadeTime=10|
        ^super.new.init(inPattern, numChannels, fadeTime);
    }

    *initClass{
        (1..64).do{|numChannels|
            SynthDef("fadesynth%".format(numChannels).asSymbol, {
                var out = \out.kr(0);
                var sig = In.ar(bus:out, numChannels:numChannels);
                var gate = \gate.ar(1);
                var fadetime = \xfadetime.kr(4.0);
                var env = Env.fadein(fadeTime:fadetime, sustainLevel:1);
                env = env.ar(gate: gate, doneAction: 2);

                ReplaceOut.ar(bus:out, channelsArray:sig * env);

            }).add;
        }

    }

    init{|inPattern, numChannels, fadeTime|
        ^Pfx(inPattern, "fadesynth%".format(numChannels).asSymbol, \xfadetime, fadeTime);
    }

}
