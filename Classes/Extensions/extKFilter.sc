/*

This adds a nodeproxy role for \kfilter which works exactly like the normal \filter role except for a crucial difference: It adds numbers to the node proxy role's synth arguments to make it possible to add several node proxy role layers containing the same function while still being able to control them individually.

Thanks to Eirik Blekesaune (https://github.com/blacksound) for putting together this beautiful hack <3

*/

KFilter {

	*initClass{
		StartUp.add {
			AbstractPlayControl.proxyControlClasses.put(\kfilter, SynthDefControl);

			AbstractPlayControl.buildMethods.put(\kfilter,
				#{ | func, proxy, channelOffset = 0, index |
					var ok, ugen;
					if(proxy.isNeutral) {
						ugen = func.value(Silent.ar);
						ok = proxy.initBus(ugen.rate, ugen.numChannels + channelOffset);
						if(ok.not) { Error("NodeProxy input: wrong rate/numChannels").throw }
					};

					{ | out |
						var e = EnvGate.new * Control.names(["wet"++(index ? 0)]).kr(1.0);
						if(proxy.rate === 'audio') {
							var innerArgNames = func.def.argNames.reject({arg it; it == \in});
							var controlSigs;

							innerArgNames = innerArgNames.collect({arg innerArgName;
								innerArgName.asString ++ index ? 0;
							});
							controlSigs = innerArgNames.collect({arg innerArgName;
								Control.names([innerArgName]).kr(1.0);
							});

							/*		"MY ARGS: %".format(innerArgNames).postln;
							"Controlnames: %".format(proxy.controlNames).postln;*/

							XOut.ar(out, e, SynthDef.wrap(func, nil, [In.ar(out, proxy.numChannels)] ++ controlSigs ))
						} {
							XOut.kr(out, e, SynthDef.wrap(func, nil, [In.kr(out, proxy.numChannels)]))
						};
					}.buildForProxy( proxy, channelOffset, index )

			})
		}
	}
}