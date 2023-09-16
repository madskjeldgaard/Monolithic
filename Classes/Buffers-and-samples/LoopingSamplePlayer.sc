// A looping sample player. This will keep playing the sample in a smoothly crossfaded loop until it is stopped
// A simple convenience for playing a sample in loop
LoopingSamplePlayer{
    var buffer, fadeTime, out, amp, playrate, lowcutFreq, numChannels, pingpong;
    var <routine, <synth, action, <synthArgs;

    var lastDirection = 1;

    *new{|numChannels, buffer, fadeTime=8, out=16, amp=0.125, playrate=1, lowcutFreq=40, pingpong=true|
        // fadetime duration is clipped to avoid infinite spawners
        fadeTime = fadeTime.clip(0.0, buffer.duration/2.0);

        ^super.newCopyArgs(buffer, fadeTime, out, amp, playrate, lowcutFreq, numChannels ? buffer.numChannels, pingpong).init();
    }

    *initClass{
        Class.initClassTree(Pbind);
        Class.initClassTree(Event);

        /*

        (
            Pdef(
                \fbweave,
                Pbind(
                    \type, \loopingsampler,
                    \buffer, b['glassy'].asPxrand(inf),
                    \dur, 128,
                    \fadeTime, 8,
                    \playrate, Pwhite(0.5,1.0)
                )
            ).play;
        )

        */
        Event.addEventType(type:\loopingsampler, func:{|ev|
            var duration = ~dur ? 1;
            var buffer = ~buffer;
            var fadeTime = ~fadeTime ? 4;
            var playrate = ~playrate ? 1;
            var lagTime = ~lagTime ? 0;
            var numChannels = ~numChannels ?? { buffer.numChannels };
            var out = ~out ? 0;
            var pan = ~pan ? 0;
            var amp = ~amp ? 0.5;
            var pingpong = ~pingpong ? false;

            var player = LoopingSamplePlayer(numChannels: numChannels, buffer: buffer, fadeTime: fadeTime, playrate: playrate, pingpong: pingpong);

            // If fade is longer than event duration
            if(fadeTime > duration, {
                "fadetime duration % is longer than event duration %".format(fadeTime, duration).warn;
            });

            player.play();

            fork{
                duration.wait;
                "Stopping LoopingSamplePlayer".postln;
                player.stop;
            };

        }, parentEvent:nil)
    }
    init{
        synthArgs = [\buffer, buffer, \fadegate, 1, \fadeTime, fadeTime, \out, out, \amp, amp, \playrate, playrate, \lowcutFreq, lowcutFreq].asDict;
        routine = this.prMakeRoutine()
    }

    synthFunc{
        ^{|gate=1|
            var lagTime = \lagTime.kr(1, spec: [0.0,10.0]);
            var doneAction = \doneAction.kr(0, spec: [0,2,\lin,1,0]);
            var fadeTime = \fadeTime.kr(1, spec: [0.0,1.0,\lin]);
            var playrate = \playrate.kr(1.0, lag: lagTime, spec: [-4.0,4.0,\lin]);
            var loop = \loop.kr(2, spec: [0,2,\lin,1,1]); // 0 = no loop, 1 = loop, 2 = pingpong-loop
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
            var args = synthArgs;
            var bufDuration = (buffer.duration / playrate);

            synth = this.synthFunc().play(args:args.asKeyValuePairs);

            loop{

                if(synth.notNil, {
                    (bufDuration - fadeTime).wait;
                });

                "Releasing synth".postln;
                synth.release;

                pingpong.if({
                    lastDirection = lastDirection * -1;
                    args[\playrate] = args[\playrate] * lastDirection;
                });

                synth = this.synthFunc().play(args:args.asKeyValuePairs);

            }
        });
    }

    play{|actionOnFree|
        action = actionOnFree;
        routine.reset();
        routine.play;
    }

    stop{
        if(synth.notNil, {
            synth.onFree(action);
        });
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
