POutFX {
    *new{|sourcePattern, fxDefName, fxArgPairs, releaseTime=10|
        var fxPairs = [fxDefName, \addAction, \addToTail] ++ fxArgPairs;

        if(sourcePattern.isNil, { "%: No source pattern supplied".format(this.class.name).error});
        if(fxDefName.isNil, { "%: No fx SynthDef name supplied".format(this.class.name).error});

        ^this.prmakePattern(sourcePattern, fxPairs, releaseTime)
    }

    *prmakePattern{|sourcePattern, fxPairs, releaseTime|
        ^this.prGroup(
            Ppar([
                this.prWrapSource(sourcePattern),
                // FX synth
                Pmono(
                    *fxPairs
                )
            ])
        ) <> (groupReleaseTime: releaseTime)
    }

    *prWrapSource{|source|
        ^source
    }

    // This makes it easier to inherit and change this to ParGroup for supernova versions
    *prGroup{|inpat|
        ^Pgroup(inpat)
    }
}

// Same as above but wrapped in a Plambda so Plet and Pget can be used to share data between source and fx pattern.
POutFXLambda : POutFX {
    *prGroup{|inpat|
        ^Pgroup(Plambda(inpat))
    }
}

// SuperNova version
PparOutFXLambda : POutFX {
    *prGroup{|inpat|
        ^Pgroup(Plambda(inpat))
    }

    *prWrapSource{|source|
        ^PparGroup(source)
    }
}

// SuperNova version
PparOutFX : POutFX {
    *prGroup{|inpat|
        ^Pgroup(inpat)
    }

    *prWrapSource{|source|
        ^PparGroup(source)
    }
}
