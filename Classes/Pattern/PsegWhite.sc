// A shortcut to creating a Pwhite with a Pseg to interpolate between values
PsegWhite{
    *new{|lo=0.0, hi=1.0, durMin=1, durMax=10,curve=\lin,repeats=inf|
        ^Pseg(Pwhite(lo,hi,inf),Pwhite(durMin,durMax,inf),curve,repeats)
    }
}
