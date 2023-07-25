// Similar to ControlSpec but bases itself on an array, and then the mapping function returns an indexed value from that array
ArrayedSpec{
    var <indexSpec;
    var <thisArray;

    *new{|array, default=0|
        ^super.new.init(array, default)
    }

    init{|array, default|
        thisArray = array;
        indexSpec = ControlSpec.new(minval:0, maxval:array.size-1, warp:\lin, step:1, default:default);
    }

    map{|value|
        var index = indexSpec.map(value).asInteger;

        ^thisArray[index]
    }

    asSpec{
        ^this
    }

    array{
        ^thisArray
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
            thisArray[newIndex].value();
        }, {
            nil
        })
    }
}
