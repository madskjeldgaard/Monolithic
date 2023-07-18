/*

TIDAL inspired patterns

*/

Pspeed : Pattern {
    *new { |pat, speed=1|
		^Pchain(pat, (stretch: speed.reciprocal))
    }
}

// Basic weave function. Can be used directly but probably shouldn't
Pweave : Pattern {
	*new { |eventPatterns, weaveSpeed=1, overWrite=true ... params|

		// For every event pattern in the list...
		var weavePats = eventPatterns.collect { |eventPat, eventIndex|

			// ... and every parameter chosen
			var newParams = params.collect { |param, paramIndex|

				// ... Use this function to create a new version of the pattern(s)
				var paramFunc = this.weaveFunc(
					eventIndex,
					paramIndex,
					weaveSpeed,
					eventPatterns.size
				);

				// Possibly overwriting original parameter values, or scaling them against the function
				var paramValue = overWrite.if(
					{ paramFunc },
					{ Pkey(param.asSymbol) * paramFunc} // Scale original value using func
				);

				[param.asSymbol, paramValue] 
			}.flatten;

			// Modified version of event pattern collected
			Pbindf(eventPat, *newParams)
		};

		// All modified versions of the event patterns stacked on top of eachother again
		^Ppar(weavePats)
	}

	*weaveFunc{ |eventIndex, paramIndex, weaveSpeed, numEventPatterns|
		"Do not use the Pweave function directly".error;
		^nil
	}
}

// Weave patterns using sine, fixed phase
Ptops : Pweave {
	*weaveFunc{ |eventIndex, paramIndex, weaveSpeed, numEventPatterns|
		eventIndex.odd.if({
			^Psinen(( 1+paramIndex )*( 1+eventIndex )*weaveSpeed)
		}, {
			^Pcosinen(( 1+paramIndex )*( 1+eventIndex )*weaveSpeed)
		})
	}
}

// Weave patterns using sine, distributed phase
Pwaves : Pweave {
	*weaveFunc{ |eventIndex, paramIndex, weaveSpeed, numEventPatterns|
			var phase = eventIndex.linlin(0, numEventPatterns, -2pi, 2pi);
			^Psinen(( 1+paramIndex )*( 1+eventIndex )*weaveSpeed, phase);
	}
}

// Weave using envelopes
// Psegways : Pweave {
// 	*weaveFunc{ |eventIndex, paramIndex, weaveSpeed, numEventPatterns|

// 	}
// }
