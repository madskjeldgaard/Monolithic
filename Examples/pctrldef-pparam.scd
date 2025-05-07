(
Pctrldef(\yo, {|ctrl|

    // The pattern to be returned/played
    Pbind(
        \dur, Pwhite(1, 5, inf) * 0.125,
        \scale, ctrl[\scale].trace,
        \degree,ctrl[\degree] + Pn(Pseries(rrand(0, 10), #[-1, 1].choose, rrand(4, 10))),
        \amp, ctrl[\amp],
    )

})
.addParam(
    // Name, default val, spec
    \amp, 0.25, \amp,
    \degree, 0, [0,10].asSpec,
    // Name of param, default val, array of choices
    \scale, Scale.minor, [Scale.minor, Scale.major, Scale.whole, Scale.majorPentatonic, Scale.minorPentatonic]
);

s.waitForBoot{
    s.sync;
    Pctrldef(\yo).gui;
};
)


// change scale
Pctrldef(\yo).map(\scale, 0.0);
Pctrldef(\yo).map(\scale, 0.5);
Pctrldef(\yo).map(\scale, 1.0);

// Set amp
Pctrldef(\yo).map(\amp, 0.35);

// Set degree
Pctrldef(\yo).map(\degree, rrand(0.0, 1.0));
