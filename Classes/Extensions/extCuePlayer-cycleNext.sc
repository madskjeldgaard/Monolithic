+ CuePlayer {

    // Next cue, and if we're at the end, wrap around to the beginning
    nextWrap{
        var nextIndex = (this.current + 1).wrap(1, this.cueList.size) ;
        this.trigger(nextIndex);
    }

    // Jump to a random cue, but not this one
    nextRandom{
        var possibleCues = (1..this.cueList.size).reject{|i| i == current};
        var nextIndex = possibleCues.choose;
        this.trigger(nextIndex);
    }

    printCues{
        "Cueplayer contains the following cues:".postln;

        cueList.do{|cue, cueNum|
            ((cueNum + 1).asString ++ ": " ++ cue.cueTitle).postln;
        };
    }

    currentCueTitle{
        var cueIdx = current - 1;
        var title = cueList[cueIdx].cueTitle;

        ^title;
    }
}
