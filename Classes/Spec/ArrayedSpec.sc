// Similar to ControlSpec but bases itself on an array, and then the mapping function returns an indexed value from that array
ArrayedSpec{
    var <indexSpec;
    var <values;

    *new{|array, default=0|
        ^super.new.init(array, default)
    }

    init{|array, default|
        values = array;
        indexSpec = ControlSpec.new(minval:0, maxval:array.size-1, warp:\lin, step:1, default:default);
    }

    // Take a normalized input 0.0-1.0 and return a value from the array
    map{|value|
        var index = indexSpec.map(value).asInteger;

        ^values[index]
    }

    // Take a value from an array and return a normalized value 0.0-1.0 depending on it's index in the array
    unmap{|value|
        var index = values.indexOfEqual(value);

        ^indexSpec.unmap(index)
    }

    asSpec{
        ^this
    }

    array{
        ^values
    }

    default{
        ^indexSpec.default
    }

    minval{
        ^indexSpec.minval
    }

    maxval{
        ^indexSpec.maxval
    }

    warp{
        ^indexSpec.warp
    }

    step{
        ^indexSpec.step
    }

    step_{|newStep|
        indexSpec.step_(newStep);
    }

    isArrayedSpec{
        ^true
    }
}

/*
// A special kind of array that maps functions to certain values
// Example usage:

f = ArrayedFuncSpec.new([{"i'm func 1".postln}, {"i'm func 2".postln}, {"i'm func 3".postln}]);
f.map(rrand(0.0,1.0));
*/
ArrayedFuncSpec : ArrayedSpec {
    var <lastIndex;

    map{|value|
        var newIndex = indexSpec.map(value).asInteger;

        ^if(newIndex != lastIndex, {
            lastIndex = newIndex;
            values[newIndex].value();
        }, {
            nil
        })
    }

    isArrayedSpec{
        ^false
    }
}
