/*

Interpolate between two arrays using patterns

// EXAMPLE with stream:
(
var repeats = 10;
// Basic usage with static arrays
p = PdiffInterp(
    [1, 0, 0, 0],
    [0, 1, 1, 1],
    interpolationAmount: Pseq([0.0, 0.25, 0.5, 0.75, 1.0], inf),
    strategy: Pseq([\fromTop, \fromBottom], inf),
    repeats: repeats
);
x = p.asStream;
repeats.do { x.next.postln };
)

*/
PdiffInterp : Pattern {
    var <>mainArray, <>contrastArray, <>interpolationAmount, <>strategy, <>repeats;

    *new { |mainArray, contrastArray, interpolationAmount = 0.0, strategy = \fromTop, repeats = inf|
        ^super.newCopyArgs(mainArray, contrastArray, interpolationAmount, strategy, repeats);
    }

    storeArgs { ^[mainArray, contrastArray, interpolationAmount, strategy, repeats] }

    embedInStream { |inval|
        var mainStream, contrastStream, amountStream, strategyStream;
        var mainArr, contrastArr, amount, strat;
        var count = 0;

        mainStream = mainArray.asStream;
        contrastStream = contrastArray.asStream;
        amountStream = interpolationAmount.asStream;
        strategyStream = strategy.asStream;

        while { count < repeats } {
            mainArr = mainStream.next(inval);
            contrastArr = contrastStream.next(inval);
            amount = amountStream.next(inval);
            strat = strategyStream.next(inval);

            if (mainArr.isNil or: { contrastArr.isNil }) { ^inval };

            // Handle scalar amount if stream ends
            if (amount.isNil) {
                amount = interpolationAmount;
                if (amount.isSequenceableCollection) { amount = amount[0] };
            };

            // Handle scalar strategy if stream ends
            if (strat.isNil) {
                strat = strategy;
                if (strat.isSequenceableCollection) { strat = strat[0] };
            };

            // Call the array method
            inval = mainArr.diffInterp(contrastArr, amount, strat).yield;
            count = count + 1;
        };

        ^inval;
    }
}

// Same as above, but plays the interpolated array in a sequence
// Here repeats works the same as in Pseq: It plays through the whole sequence, and that's a repeat.
PdiffInterpSeq : Pattern {
    var <>mainArray, <>contrastArray, <>interpolationAmount, <>strategy, <>repeats;

    *new { |mainArray, contrastArray, interpolationAmount = 0.0, strategy = \fromTop, repeats = 1|
        ^super.newCopyArgs(mainArray, contrastArray, interpolationAmount, strategy, repeats);
    }

    storeArgs { ^[mainArray, contrastArray, interpolationAmount, strategy, repeats] }

    embedInStream { |inval|
        var mainStream, contrastStream, amountStream, strategyStream;
        var mainArr, contrastArr, amount, strat, resultArray;
        var count = 0;

        mainStream = mainArray.asStream;
        contrastStream = contrastArray.asStream;
        amountStream = interpolationAmount.asStream;
        strategyStream = strategy.asStream;

        while { count < repeats } {
            mainArr = mainStream.next(inval);
            contrastArr = contrastStream.next(inval);
            amount = amountStream.next(inval);
            strat = strategyStream.next(inval);

            // Check if any essential streams ended
            if (mainArr.isNil or: { contrastArr.isNil }) { ^inval };

            // Handle default values for optional streams
            amount = amount ? 0.0;
            strat = strat ? \fromTop;

            // Get the interpolated array
            resultArray = mainArr.diffInterp(contrastArr, amount, strat);

            // Yield each element of the result array
            resultArray.do { |item|
                inval = item.yield;
            };

            count = count + 1;
        };

        "done".postln;

        ^inval;
    }
}
