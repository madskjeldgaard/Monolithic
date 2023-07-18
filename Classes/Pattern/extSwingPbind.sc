// Swing
// More info: https://doc.sccode.org/Tutorials/A-Practical-Guide/PG_Cookbook08_Swing.html
+ Pbind{
    withSwing{|swingBase=0.25, swingAmount=0.5|
        var swingify = Prout({ |ev|
            var now, nextTime = 0, thisShouldSwing, nextShouldSwing = false, adjust;
            while { ev.notNil } {
                // current time is what was "next" last time
                now = nextTime;
                nextTime = now + ev.delta;
                thisShouldSwing = nextShouldSwing;
                nextShouldSwing = ((nextTime absdif: nextTime.round(ev[\swingBase])) <= (ev[\swingThreshold] ? 0)) and: {
                    (nextTime / ev[\swingBase]).round.asInteger.odd
                };
                adjust = ev[\swingBase] * ev[\swingAmount];
                // an odd number here means we're on an off-beat
                if(thisShouldSwing) {

                    ev[\lag] = (ev[\lag] ? 0) + adjust;

                    // "This should swing. Adjustment: %. Timing offset: %".format(adjust,ev[\lag] ).postln;

                    // if next note will not swing, this note needs to be shortened
                    if(nextShouldSwing.not) {
                        ev[\sustain] = ev.use { ~sustain.value } - adjust;
                    };
                } {
                    // if next note will swing, this note needs to be lengthened
                    if(nextShouldSwing) {
                        ev[\sustain] = ev.use { ~sustain.value } + adjust;
                    };
                };
                ev = ev.yield;
            };
        });

        var swingPat = Pchain(
            swingify,
            this,
            (
                swingBase: swingBase,
                swingAmount: swingAmount,
            )
        );

        ^swingPat
    }
}
