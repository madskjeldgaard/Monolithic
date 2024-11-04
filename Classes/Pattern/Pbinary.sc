// Make a list pattern with binary 1's and 0's. 0's are converted to Rests. Useful for creating rhythms.
/*

// Example

Pbind(
    \degree, Pseq([0,5], inf),
    \dur, 0.125 * Pbinary([1,1,1,0,1,1,0,1,0,1,1,0], inf),
).play;


*/
Pbinary{

    *new{|list, repeats|
        ^super.new.init(list, repeats)
    }

    init{|list, repeats|

        // Convert all 0 to Rest
        list = list.collect{|val| if(val == 0, {Rest()}, {val})}

        ^Pseq(list, repeats);
    }

}
