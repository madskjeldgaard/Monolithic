/*

Sine and cosine functions

*/

// Tak til Eirik og Niklas for at fixe dette!
Psine : Pattern {
    *new { |freq=1, phase=0|

		//phase arg in radians e.g. 1pi, 1.5pi etc.
		//add 2pi the get correct freq and phase values
		//Don't use .abs here. But rather do scaling outside the pattern
		//   e.g. Psine(...).linlin(-1.0, 1.0, 0.0, 1.0)

		^Pn(sin(2pi * freq * Ptime() + phase))
    }
}

// Normalized
Psinen : Pattern {
    *new { |freq=1, phase=0|
		^Pn(sin(2pi * freq * Ptime() + phase)).linlin(-1,1,0.0,1.0)
    }
}

Pcosine : Pattern {
    *new { |freq=1, phase=0|
		^Pn(cos(2pi * freq * Ptime() + phase))
    }
}

// Normalized
Pcosinen : Pattern {
    *new { |freq=1, phase=0|
		^Pn(cos(2pi * freq * Ptime() + phase)).linlin(-1,1,0.0,1.0)
    }
}
