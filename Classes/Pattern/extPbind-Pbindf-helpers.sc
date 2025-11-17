+ Pattern {
    deltaParam_ {|paramName, deltaValue| 

        var val = if(deltaValue.isKindOf(Function), {
            deltaValue.value(Pkey(paramName))
        }, {
            Pkey(paramName) + deltaValue
        });

        ^Pbindf(this, paramName, val)
    }

    param_ {|paramName, newValue|
        ^Pbindf(this, paramName, newValue)
    }

    octave_ {|newOctave|
        ^this.param_(\octave, newOctave)
    }

    octaveOff_ {|deltaOctave|
        ^this.deltaParam_(\octave, deltaOctave)
    }

    mtranspose_ {|deltaVal|
        ^this.deltaParam_(\mtranspose, deltaVal)
    }

    mtransposeOff_ {|deltaVal|
        ^this.deltaParam_(\mtranspose, deltaVal)
    }

    root_ {|newRoot|
        ^this.param_(\root, newRoot)
    }

    rootOff_ {|deltaRoot|
        ^this.deltaParam_(\root, deltaRoot)
    }

    gtranspose_ {|deltaVal|
        ^this.deltaParam_(\gtranspose, deltaVal)
    }

    gtransposeOff_ {|deltaVal|
        ^this.deltaParam_(\gtranspose, deltaVal)
    }

    ctranspose_ {|deltaVal|
        ^this.deltaParam_(\ctranspose, deltaVal)
    }

    ctransposeOff_ {|deltaVal|
        ^this.deltaParam_(\ctranspose, deltaVal)
    }

}
