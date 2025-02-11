// Make a list pattern with binary 1's and 0's. 0's are converted to Rests. Useful for creating rhythms.
/*

// Example

Pbind(
    \degree, Pseq([0,5], inf),
    \dur, 0.125 * Pbinary([1,1,1,0,1,1,0,1,0,1,1,0], inf),
).play;


*/
Pbinary{

    *new{|list, repeats=inf|
        ^super.new.init(list, repeats)
    }

    init{|list, repeats|

        // Convert all 0 to Rest
        list = list.collect{|val| if(val.asInteger == 0, {Rest()}, {val})}

        ^Pseq(list, repeats);
    }

}

PbinaryGate : Pn {
    *new { arg pattern, repeats=inf, key;
        ^super.new(pattern).repeats_(repeats).key_(key)
    }

    storeArgs { ^[pattern, repeats, key] }

    embedInStream { | event |
        var stream, output;
        repeats.do {
            stream = pattern.asStream;
            output = nil;  // force new value for every repeat
            while {
                if (event[key] == true or: { output.isNil }) { output = stream.next(event) };
                output.notNil;
            } {
                event = output.copy.embedInStream(event)
            }
        };
        ^event;
    }
}

+Array{
    asPbinary{|repeats=inf|
        ^Pbinary.new(this, repeats)
    }
}
