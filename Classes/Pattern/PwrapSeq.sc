// Same as Pseq but with a max index at which point it wraps
PwrapSeq{
    *new{
        arg array, maxIndex, startIndex=0, repeats=inf;

        var indexPat = Pseries.new(startIndex, 1, length: inf) % (maxIndex+1);

        ^Pindex.new(listPat: array, indexPat: indexPat, repeats: repeats)
    }

}

// Change the max index over time
/*
(
var array = (0..4);
Pbind(\degree, PwrapSeqStep.new(array, maxIndices: Pseq([0,1,2],inf), times: 1, repeats: inf).trace, \dur, 0.125).play
)

*/
PwrapSeqStep{
    *new{
        arg array, maxIndices, times, repeats=inf;

        ^PwrapSeq.new(array, maxIndex: Pstep(levels: maxIndices, durs: times, repeats: 1), startIndex:0, repeats: repeats)

    }
}
