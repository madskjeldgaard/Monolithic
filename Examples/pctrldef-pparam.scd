(
s.waitForBoot{
    Pctrldef(\yo, {|ctrl|

        // The pattern to be returned/played
        Pspawn(
            Pbind(
                \method,ctrl[\method].trace,
                \delta, Pwhite(1, 5, inf) * 0.125,
                \pattern, Pfunc {|ev|
                    var delta = ev[\delta] ? 1;
                    Pbind(
                        \degree,ctrl[\degree] + Pseries(rrand(0, 10), #[-1, 1].choose, rrand(4, 10)),
                        \amp, ctrl[\amp],
                        \dur,delta/10
                    )},
                )
            )

        })
        .addParam(
            // Name, default val, spec
            \amp, 0.25, \amp,
            \degree, 0, [0,10].asSpec,
            // Name of param, default val, array of choices
            \method, \seq, [\wait, \seq, \par]
        );

        s.sync;
        Pctrldef(\yo).play;

    };

)


// change method
Pctrldef(\yo).map(\method, 0.0);
Pctrldef(\yo).map(\method, 0.5);
Pctrldef(\yo).map(\method, 1.0);

// Set amp
Pctrldef(\yo).map(\amp, 0.35);

// Set degree
Pctrldef(\yo).map(\degree, rrand(0.0, 1.0));
