// Convert midi notes to playrate (of a sample player)
// Use it inside of a Pbind
PMidiNote2Rate{
    *new{|baseMidiNote|
        ^Pfunc({|ev| ev.use{
            var base = baseMidiNote ? ~rateBaseNote ? 60;
            ~midinote.value().asArray.collect{|note|
                note.asInteger.midinote2Rate(base)
            }}
        })
    }
}

// Short hand for getting the size of a chord
PChordSize{
    *new{
        ^Pfunc({|ev| ev.use{ ~freq.value().asArray.size}})
    }
}

// Amplitude compensation based on a sounds frequency
PAmpComp{
    *new{
        ^Pfunc({|ev|
            ev.use{
                var root = 40.0;
                var freq = ~freq.value();
                var xa = root / freq;
                var xb = 0.3333;

                xa.pow(xb)
            }
        })
    }
}

// Get the full duration of the currently playing buffer, scaled correctly by the rate
// If you pop it in a Pbind in the \dur key, it will play the whole buffer's content, no matter the rate
// Example:
// Pbind(\instrument, \playbuf_1i_2o_gate, \buffer, g[1], \playrate, Pwhite(0.125, 1.0), \dur, PBufferDur()).play;
PBufferDur{
    *new{
        ^Pfunc({|ev|
            ev.use{
                var rate = ~rate ? ~playrate ? ~playRate ? ~playbackrate ? ~playbackRate ? 1;
                var rateArg = rate.value();
                var buffer = (~buffer ? ~buf ? ~bufNum ? ~sampleBuffer).value();
                var bufferDur = buffer.duration;
                bufferDur / rateArg;
            }
        })
    }
}

// Just an alias
PBufDur : PBufferDur{}

// Format a string with the number of channels in the \buffer key
// Return as a symbol (for synthdef names)
PBufChanFormatSym{
    *new{|string, bufferKeyName=\buffer|
        ^Pfunc({|ev|
                var buffer = ev[ bufferKeyName ];
                var channels = buffer.numChannels;
                string.format(channels).asSymbol;
        })
    }
}

// Expand a pattern depending on the number of notes in the chord being played
+ Pattern{
    clumpByChordSize{
        ^this.clump(PChordSize());
    }
}
