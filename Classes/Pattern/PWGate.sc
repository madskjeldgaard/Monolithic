/*

A simple Bernoulli gate that is able to take a stream of values in (as opposed to Pwrand) for both values and weights

(

p = Pbind(
    \dur, 0.125 * PWGate(
        onWeight: Pseg([0.01,1.0],16,\lin,inf),
        onVal: 1,
        offVal: Rest(1)
    ).trace,
    \degree, Pwhite(0,7)
);

p.play;
)

*/
PWGate{

    *new{|onWeight=0.25, onVal=1, offVal=0, repeats=inf|
        ^super.new.init(onWeight, onVal, offVal, repeats)
    }

    init{|onWeight, onVal, offVal, repeats|
        var pat = Prout({
            var weightStream = onWeight.asStream;

            onVal = onVal.asStream;
            offVal = offVal.asStream;

            repeats.do{|repeatnum|
                var weight = weightStream.next();
                var outval = [
                    onVal.next(),
                    offVal.next()
                ].wchoose([
                    weight,
                    1.0 - weight
                ]);

                outval.yield;
            }

        });

        ^pat
    }
}