/*

This class is a simple disk player that may be used to play a folder of sound files from disk, either one at a time or
as a routine, allowing to play infinitely in shuffle or linear mode, with a given rate and fade time for each.

// Example
(
s.waitForBoot{
    d = DiskPlayer(diskPath: "/Volumes/REDRUM/sounds/real/HANDLING NOISE/\*", numChannels: 2);
    s.sync;
    1.wait;
    r = d.asRoutine(playMode: \shuffle, fadeTime: 2, rate: { rrand(0.25, 2) }, overlapTime: -1);
    s.sync;
    r.play;
}
)
*/

DiskPlayer{
    var <path="";
    var <filesInFolder;
    var <buffer;
    var <numChans;
    var <server;
    var <synthDescName;

    var playingSynth;
    var playingSoundfile;
    var index = 0;

    var bufferSize = 65536;

    *new{|diskPath, numChannels=2|
        ^super.new.init(diskPath, numChannels);
    }

    init{|diskPath, numChannels|
        var pathIsFolder;

        numChans = numChannels;
        path = diskPath;

        server = Server.default;

        filesInFolder = SoundFile.collect(path: path);

        synthDescName = "monolithic_diskplayer%".format(numChannels).asSymbol;

        SynthDef(synthDescName,{|out=0, duration=1, amp=0.85, buffer, loop=0, rate=1, fadeDuration=0.01|
            var rateScalar = BufRateScale.kr(buffer);
            var fadeTime = fadeDuration;

            var envelope = Env.new(
                levels: [0,1,1,0],
                times: [fadeTime, duration - fadeTime, fadeTime],
                curve: [\sine, 0, \sine],
                // releaseNode: 2 // Use this to make it gated
            ).ar(gate: 1, doneAction: Done.freeSelf);

            var sig = VDiskIn.ar(numChannels: numChannels, bufnum: buffer, rate: rate * rateScalar, loop: loop);

            sig = sig * envelope * amp;

            Out.ar(out, sig)
        }).add;

        buffer = Buffer.alloc(server: server, numFrames: bufferSize, numChannels: numChannels);
    }

    play{|fileIndex=0, rate=1, fadeTime=0.01|
        var synthArgs;
        var dur;

        if(fileIndex >= filesInFolder.size || fileIndex < 0){
            fileIndex = 0;
            "fileIndex out of bounds, resetting to 0".warn;
        };

        this.stop();

        // Get the sound file name
        playingSoundfile = filesInFolder[fileIndex];

        dur = playingSoundfile.duration / rate;

        "Playing file %: Duration % (rate %)".format(playingSoundfile.path, dur, rate).postln;

        playingSynth = Synth.basicNew(synthDescName);

        // Cue in the buffer
        buffer = Buffer.cueSoundFile(
            server: server,
            path: playingSoundfile.path,
            startFrame: 0,
            numChannels: numChans,
            bufferSize: bufferSize,
            completionMessage: {|thisBuffer|
                "Buffer loaded".postln;

                synthArgs = [
                    \buffer, thisBuffer,
                    \rate, rate,
                    \duration, playingSoundfile.duration / rate,
                    \fadeDuration, fadeTime,
                ];

                playingSynth.newMsg(server, synthArgs, \addToTail);
            }
        );


        ^dur
    }

    stop{
        playingSynth.free;
    }

    next{
        index = index + 1;
        ^this.play(index);
    }

    prev{
        index = index - 1;
        ^this.play(index);
    }

    randomNext{
        index = index + 1.rand(filesInFolder.size);
        ^this.play(index);
    }

    // Rate can be a number or a function returning a number (e.g. { rrand(0.5, 2) })
    // Negative overlaptime becomes time between the end of the previous sound and the start of the next
    // FIXME: Overlap still not implemented. Need to allow parallel synths to overlap.
    asRoutine{|playMode=\shuffle, rate=1, fadeTime=0.25, overlapTime=(-1)|
        var thisRate = rate;

        ^Routine.new({
            loop{
                var dur;

                switch(playMode,
                    \shuffle, {
                        index = rrand(0, filesInFolder.size-1);
                    },
                    \linear, {
                        index = index + 1;
                    },
                    // \oneshot, {
                    //     index = 0;
                    // },
                );

                if(rate.isFunction){
                    thisRate = rate.value(index);
                } {
                    thisRate = rate.value();
                };


                "Playing index %".format(index).postln;

                dur = this.play(index, thisRate, fadeTime);

                (dur - overlapTime).wait; // Wait for the duration minus the overlap time

            }
        })
    }

}
