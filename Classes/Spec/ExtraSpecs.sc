// Adds extra specs
ExtraSpecs{
    *initClass{
        Class.initClassTree(Spec);
        Class.initClassTree(ControlSpec);

        Spec.add(\out, [0,64,\lin,1,0]);

        Spec.add(\time, [0.000000001,100000.0,\lin,0,1]);

        // Envelope stuff
        Spec.add(\attackTime, [0.0000001, 100.0, \exp]);
        Spec.add(\decayTime, [0.0000001, 100.0, \exp]);
        Spec.add(\sustainLevel, [0.0, 1.0, \lin]);
        Spec.add(\releaseTime, [0.0000001, 100.0, \exp]);
        Spec.add(\curve, [-10.0, 10.0, \lin]);

        Spec.add(\root, [0,12,\lin,1,0]);
        Spec.add(\octave, [0,10,\lin,1,0]);
        Spec.add(\degree, [-12,12,\lin,1,0]);

        Spec.add(\midinote, [0,127,\lin,1,0]);
        Spec.add(\midivel, [0,127,\lin,1,0]);
        Spec.add(\midicc, [0,127,\lin,1,0]);
        Spec.add(\midipgm, [0,127,\lin,1,0]);

        Spec.add(\cutoff, [20.0,20000.0,\exp]);
        Spec.add(\lfo, [0.0000001,100.0,\exp]);

        Spec.add(\uni, [0.0,1.0,\lin]);
        Spec.add(\bi, [-1.0,1.0,\lin]);

        Spec.add(\uniplusone, [1.0,2.0,\lin]);

        Spec.add(\modAmount, [0.0,1.0,\lin]);

        ["pan", "amp", "filter", "fm", "freq", "rate", "playrate", "delay", "feedback", "wet", "dry", "gain"].do{|x|
            var keyString = "%ModAmount".format(x);
            Spec.add(keyString.asSymbol, [0.0,1.0,\lin]);
            Spec.add(keyString.toLower.asSymbol, [0.0,1.0,\lin]);
        };

        ServerBoot.add({
            Spec.add(\buffer, [0,Server.default.options.numBuffers - 1,\lin, 1, 0]);
        }, \default)
    }
}
