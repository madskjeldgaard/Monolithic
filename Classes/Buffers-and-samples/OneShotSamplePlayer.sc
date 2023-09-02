// Plays a buffer once, then frees itself and calls an action language side when it's done playing
// Example
/*
(
var path = "~/Desktop/kreuzberg-singleshots/kreuzberg-oneshot-gated-verb-001.wav".asAbsolutePath;

s.waitForBoot{
    var buffer = Buffer.read(server:s, path:path);
    s.sync;
    o = OneShotSamplePlayer.new(buffer);
    s.sync;
    o.play(action: {|msg|
        "Sample done!!".postln;
    });
}
)

// Example with CuePlayer
c = CuePlayer.new()
c.gui;
(
var path = "~/Desktop/kreuzberg-singleshots/kreuzberg-oneshot-gated-verb-001.wav".asAbsolutePath;
var path2 = "~/Desktop/kreuzberg-singleshots/gatedverb/kreuzberg-oneshot-gated-verb-003.wav".asAbsolutePath;

s.waitForBoot{
    var buffer = Buffer.read(server:s, path:path);
    var buffer2 = Buffer.read(server:s, path:path2);
    s.sync;
    o = OneShotSamplePlayer.new(buffer, playrate: 1.1, amp: 2);
    y = OneShotSamplePlayer.new(buffer2, playrate: 1.1, amp: 2);
    s.sync;
    o.addToCuePlayer(c, true);
    y.addToCuePlayer(c, true);
    // o.play(action: {|msg|
    //     "Sample done!!".postln;
    // });
}
)
*/
OneShotSamplePlayer{
    classvar <numPlayers = 0;

    var buffer,  fadeTime, out, amp, playrate, lowcutFreq;
    var <synth, <oscfunc, <oscpath, synthdef, synthfunc;

    *new{|buffer, fadeTime=0.0, out=0, amp=0.5, playrate=1|
        ^super.newCopyArgs(buffer, fadeTime, out, amp, playrate).init()
    }

    init{
        numPlayers = numPlayers + 1;
        oscpath = "/oneshotsample/%".format(numPlayers).asSymbol;
        synthfunc = {|playrate=1, amp|
            var end = buffer.numFrames;
            // var phase = Phasor.ar(0, playrate * BufRateScale.ir(buffer), 0, end);
            var phase = Line.ar(start:0, end:end, dur:playrate.reciprocal * BufRateScale.ir(buffer), doneAction:2);
            var hasEnded = phase > (end-1);
            SendReply.ar(trig:hasEnded, cmdName:oscpath, values:[phase, end, hasEnded], replyID:-1);
            amp * BufRd.ar(numChannels:buffer.numChannels, bufnum:buffer, phase:phase, loop:0.0, interpolation:4);
        }
    }

    play{|action|
        oscfunc = OSCFunc({|msg, time, addr, recvPort|
            action.value(msg);
            // synth.free;
            oscfunc.free;

        }, oscpath);

        synth = synthfunc.play(outbus: out, args: [
            \buffer, buffer, \fadeTime, fadeTime, \out, out, \amp, amp, \playrate, playrate
        ]);
    }

    asCueInfo{|triggerNextWhenDone=false|
        var fileName = PathName(buffer.path).fileNameWithoutExtension;
        var title = "Oneshot: %".format(fileName);

        ^CueInfo.new(title, title, {|cue|

            // Register cleanup functions
            cue.hook = {};

            this.play(action: {
                defer{
                    if(triggerNextWhenDone, {
                        cue.next();
                    })

                }
            });
        })
    }

    addToCuePlayer{|cuePlayer, triggerNextWhenDone=false|
        cuePlayer.add(this.asCueInfo(triggerNextWhenDone));
    }

}
