// This function interpolates the differences between two arrays, and does so either from the top, bottom or middle of the difference
/*
(
var a = [1, \r, \r, \r, ];
var b = [\r, 1, 1, 1];
var strategy = \fromTop; // or fromBottom

(0,0.125..1.0).do{|interp|
    var interpolated = a.diffInterp(contrastArray: b, interpolationAmount: interp, strategy: strategy);
    "interp value %: % (a: %, b: %, strategy: %)".format(interp, interpolated, a, b, strategy).postln;
}
)
*/
+ Array{
    diffInterp { |contrastArray, interpolationAmount = 0.0, strategy = \fromTop|
        var mainArray = this;
        var result, differences, indices, numToReplace;

        // Validate inputs
        if (mainArray.size != contrastArray.size, {
            "Arrays must be the same size".error;
            ^mainArray;
        });

        if (interpolationAmount < 0.0 or: { interpolationAmount > 1.0 }, {
            "InterpolationAmount must be between 0.0 and 1.0".error;
            ^mainArray;
        });

        // Find indices where arrays differ
        differences = [];
        mainArray.do { |item, i|
            if (item != contrastArray[i], {
                differences = differences.add(i);
            });
        };

        // If no differences, return arrayA
        if (differences.isEmpty, { ^mainArray });

        // Determine which indices to replace based on strategy
        case { strategy == \fromTop } {
            indices = differences.sort;
        }
        { strategy == \fromBottom } {
            indices = differences.sort.reverse;
        }
        // { strategy == \fromMiddle } {
        //     indices = differences.sortBy { |i|
        //         (i - (mainArray.size / 2)).abs
        //     };
        // }
        {
            "Strategy must be \\fromTop, or \\fromBottom".error;
            ^mainArray;
        };

        // Calculate how many differences to replace
        numToReplace = (interpolationAmount * differences.size).round(1).asInteger;
        numToReplace = numToReplace.clip(0, differences.size);

        // Create result by replacing values
        result = mainArray.copy;
        indices.keep(numToReplace).do { |index|
            result[index] = contrastArray[index];
        };

        ^result
    }
}
