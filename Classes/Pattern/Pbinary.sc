// Make a list pattern with binary 1's and 0's. 0's are converted to Rests. Useful for creating rhythms.
/*

// Example

Pbind(
    \degree, Pseq([0,5], inf),
    \dur, 0.125 * Pbinary([1,1,1,0,1,1,0,1,0,1,1,0], inf),
).play;

*/

Pbinary : Pattern {
    var list, repeats, offset;

    *new { |list, repeats=1, offset=0|
        ^super.newCopyArgs(list, repeats, offset)
    }

    embedInStream { |inval|
        var item, offsetValue = offset.value(inval);
        var remainingRepeats = repeats.value(inval);
        var isKindOfArray = list.isKindOf(Array);
        var isKindOfPattern = list.isKindOf(Pattern);

        if(isKindOfArray, {
            remainingRepeats.do { |j|
                list.size.do { |i|
                    item = list.wrapAt(i + offsetValue);

                    item = this.prConvert(item);

                    if(item.notNil) {
                        inval = item.yield;
                    }
                }
            };
        });

        ^inval;
    }

    storeArgs { ^[list, repeats, offset] }

    prConvert{|in|
        if(in.isKindOf(Number), {
            if(in <= 0, {
                ^Rest()
            }, {
                ^in
            });
        }, {
            ^("Pbinary: Expected a Number, got " ++ in.class.name ++ ".").error;
        })
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
