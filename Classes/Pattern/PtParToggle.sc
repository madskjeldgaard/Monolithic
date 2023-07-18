/*

(
var list = [
    // Start time, pattern, Toggle on/off
    0, Pbind(\dur, 0.125, \degree, Pseq([1,2,3],inf)), Pstep([1,0],4,inf),

    2, Pbind(\octave, 6, \degree, Pseq([3,2,5],inf)), Pstep([1,0],2,inf),
];

PtparToggle(list).play;

)

*/
PtparToggle {
    *new{|list|
        var parallel;

        parallel = list.clump(3).collect{|items|
            var startTime = items[0];
            var pattern = items[1];
            var toggle = items[2]; // 1 = on, 0 = off

            [
                startTime,
                // Wrapping the Rest in a Pbind is necessary for some reason when using a Rest in this context. Otherwise it will lead to errors that Rest doesn't respond to .asEvent
                Pswitch1([ Pbind(\dur, Rest()), pattern], toggle)
            ]
        }.flatten;

        ^Ptpar(parallel)
    }
}
