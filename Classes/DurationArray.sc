+ Array{
    // return the total duration of an array of numbers and Rests
    totalDuration{
         ^this.sanitizeRests.sum;
    }

    sanitizeRests{
        ^this.collect{|x| x.isRest.if({x.dur}, x) };
    }

    // FIXME: WORK IN PROGRESS
    // FIXME: How to make it round properly and not return zeroes?
    // Take an array of durations / Rests and normalize their total duration (for Pseq etc)
    // Not 100% accurate but mostly works
    normalizeDurations{|sumDurations=8.0, roundTo|
        var returnArray = this;

        // Get the indices of all Rests
        var areRests = this.collect{|x| x.isRest};

        // Convert to durations
        returnArray = returnArray.sanitizeRests();

        // Normalize and scale
        returnArray = returnArray.normalizeSum().collect{|x| x * sumDurations };

        // Round
        roundTo.notNil.if({
            returnArray = returnArray.collect{|x|
                // The rounding algos expect x to to not be 0 (and so does \dur in Pbind)
                if(x == 0.0, {
                    x = 0.00001
                });

                x.roundUp(roundTo).postln;
            };
        });

        // Convert rests back to rests
        returnArray = returnArray.collect{|x, index| areRests[index].if({Rest(x)}, x)};

        ^returnArray;
    }
}
