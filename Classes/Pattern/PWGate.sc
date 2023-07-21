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
PwGate{

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

// Same as above but convenience class that uses 1 for onVal and Rest(1) for offVal
PwRest{
    *new{|restWeight=0.25, restDur=1, repeats=inf|
        ^super.new.init(restWeight, restDur, repeats)
    }

    init{|onWeight, restDur, repeats|
        ^PwGate.new(onWeight: onWeight, onVal: Rest(restDur), offVal: 1, repeats: repeats)
    }
}
