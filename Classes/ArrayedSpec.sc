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
