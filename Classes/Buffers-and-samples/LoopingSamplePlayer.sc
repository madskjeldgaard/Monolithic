// A looping sample player. This will keep playing the sample in a smoothly crossfaded loop until it is stopped
// A simple convenience for playing a sample in loop
LoopingSamplePlayer{
    var buffer, fadeTime, out, amp, playrate, lowcutFreq;
    var <routine, <synth, action;

    *new{|buffer, fadeTime=8, out=16, amp=0.125, playrate=1, lowcutFreq=40|
        // fadetime duration is clipped to avoid infinite spawners
        ^super.newCopyArgs(buffer, fadeTime.clip(0.0, buffer.duration/2.0), out, amp, playrate, lowcutFreq).init();
    }

    init{
        routine = this.prMakeRoutine()
    }

    synthFunc{
        ^{|gate=1|
            var numChannels = buffer.numChannels;
            var doneAction = \doneAction.kr(0, spec: [0,2,\lin,1,0]);
            var fadeTime = \crossfadeTime.kr(1, spec: [0.0,1.0,\lin]);
            var playrate = \playrate.kr(1.0, spec: [-4.0,4.0,\lin]);
            var loop = \loop.kr(0, spec: [0,2,\lin,1,1]); // 0 = no loop, 1 = loop, 2 = pingpong-loop
            var loopStart = \loopStart.kr(0.0, spec: [0.0,1.0,\lin]);
            var loopEnd = \loopEnd.kr(1.0, spec: [0.0,1.0,\lin]);
            var env = Env.gatefade(fadeTime: fadeTime).ar(gate: gate, doneAction: doneAction);
            var end = buffer.numFrames-1;
            var phasor;

            playrate = playrate * BufRateScale.kr(buffer);

            if(\RedPhasor.asClass.isNil, {
                "RedPhasor not installed, using regular Phasor".warn;
                phasor = Phasor.ar(0, playrate, 0, end, loop)
            }, {
                phasor = \RedPhasor.asClass.ar(0, playrate, 0, end, loop, loopStart*end, loopEnd*end)
            });

            env * BufRd.ar(numChannels, buffer, phase: phasor, loop: 0, interpolation: 4);
        }
    }

    prMakeRoutine{
        ^Routine({
            var args = [\buffer, buffer, \fadegate, 1, \crossfadeTime, fadeTime, \out, out, \amp, amp, \playrate, playrate, \lowcutFreq, lowcutFreq];
            var bufDuration = (buffer.duration / playrate);

            synth = this.synthFunc().play(args:args);

            loop{

                if(synth.notNil, {
                    (bufDuration - fadeTime).wait;
                });

                synth.release;
                synth = this.synthFunc().play(args:args);

            }
        });
    }

    play{|actionOnFree|
        action = actionOnFree;
        routine.reset();
        routine.play;
    }

    stop{
        synth.onFree(action);
        routine.stop;
        synth.set(\doneAction, 2);
        synth.set(\gate, 0);
        synth = nil;
    }

    set{|...keysValues|
        synth.set(*keysValues)
    }

    asCueInfo{
        var fileName = PathName(buffer.path).fileNameWithoutExtension;
        var title = "LoopingSamplePlayer: %".format(fileName);

        ^CueInfo.new(title, title, {|cue|

            // Register cleanup functions
            cue.hook = {
                this.stop()
            };

            this.play();
        })
    }

    addToCuePlayer{|cuePlayer|
        cuePlayer.add(this.asCueInfo());
    }

}
