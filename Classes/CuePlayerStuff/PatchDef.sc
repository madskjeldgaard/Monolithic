/*


TODO:
- Add specs and spec-mapping

A simple def-style way to organize a scene or a patch for a performance. I find I often need this to prepare resources, then when a cue hits, .play a scene, and when it is done .stop it and at the end of a performance clean up

(
PatchDef(\scene1,
    configFunc: {|scene ... moreArgs|
        scene.data[\somedata] = moreArgs[0];
        // Do controller setup, sample loading, etc here
        "hi from config".postln;
    },
    playFunc: {|scene ... moreArgs|
        // Do the actual playing here
        "hi from play. Here is some data: %".format(scene.data[\somedata]).postln;
    },
    stopFunc: {|scene ... moreArgs|
        // Do the stop playing here
        "hi from stop".postln;
    },
    cleanupFunc: {|scene ... moreArgs|
        // Do the cleanup here, e.g. freeing synths and buffers
        "hi from cleanup".postln;
    },
);

PatchDef(\scene1).configure(30)
PatchDef(\scene1).play;
)

A bigger example with cue player
(
~cueplayer = CuePlayer.new();

PatchDef(\scene1,
    configFunc: {|scene ... moreArgs|
        scene.data[\somedata] = 30;
        // Do controller setup, sample loading, etc here
        "hi from scene1 config".postln;
    },
    playFunc: {|scene ... moreArgs|
        // Do the actual playing here
        "hi from scene1 play. Here is some data: %".format(scene.data[\somedata]).postln;
    },
    stopFunc: {|scene ... moreArgs|
        // Do the stop playing here
        "hi from scene1 stop".postln;
    },
    cleanupFunc: {|scene ... moreArgs|
        // Do the cleanup here, e.g. freeing synths and buffers
        "hi from scene1 cleanup".postln;
    },
)
.configure()
.addToCuePlayer(~cueplayer);

PatchDef(\scene2,
    configFunc: {|scene ... moreArgs|
        scene.data[\somedata] = 30;
        // Do controller setup, sample loading, etc here
        "hi from scene2 config".postln;
    },
    playFunc: {|scene ... moreArgs|
        // Do the actual playing here
        "hi from scene2 play. Here is some data: %".format(scene.data[\somedata]).postln;
    },
    stopFunc: {|scene ... moreArgs|
        // Do the stop playing here
        "hi from scene2 stop".postln;
    },
    cleanupFunc: {|scene ... moreArgs|
        // Do the cleanup here, e.g. freeing synths and buffers
        "hi from scene2 cleanup".postln;
    },
)
.configure()
.addToCuePlayer(~cueplayer);

~cueplayer.gui;
)

*/
PatchDef{
    var <key, <>configFunc, <>playFunc, <>stopFunc, <>cleanupFunc;
    var <>data;

    var <hasBeenConfigured = false;

    classvar <>all;

    /* Standard *def-style stuff START */
    *new{ arg key, configFunc, playFunc, stopFunc, cleanupFunc;
        var res = this.at(key);
        if(res.isNil) {
            "Creating new PatchDef: %".format(key).postln;
            res = super.new().prAdd(key).configFunc_(configFunc).playFunc_(playFunc).stopFunc_(stopFunc).cleanupFunc_(cleanupFunc);
            res.data = IdentityDictionary.new;
        } {
            if(configFunc.notNil) { res.configFunc_(configFunc) };
            if(playFunc.notNil) { res.playFunc_(playFunc) };
            if(stopFunc.notNil) { res.stopFunc_(stopFunc) };
            if(cleanupFunc.notNil) { res.cleanupFunc_(cleanupFunc) };
        };

        ^res
    }

    *at{|key|
        ^all[key]
    }

    at{|key|
        ^data[key]
    }

    put{|...keysValues|
        keysValues.asPairs.pairsDo{|key, value|
            data.put(key, value);
        };
    }

    set{|...keysValues|
        keysValues.asPairs.pairsDo{|key, value|
            data.put(key, value);
        };
    }

    *initClass {
        all = IdentityDictionary.new;
    }

    dup { |n = 2| ^{ this }.dup(n) } // avoid copy in Object::dup

    *hasGlobalDictionary { ^true }

    prAdd { arg argKey;
        key = argKey;
        all.put(argKey, this);
    }

    /* Standard *def-style stuff END */

    configure{ arg ...args;
        configFunc.value(this, *args);
        hasBeenConfigured = true;
    }

    play{ arg ...args;
        hasBeenConfigured.not.if({
            "%: playing without having called .configure .. resources may not have been allocated".format(this.class.name).warn;
        });

        playFunc.value(this, *args);
    }

    stop{ arg ...args;
        stopFunc.value(this, *args);
    }

    cleanup{ arg ...args;
        cleanupFunc.value(this, *args);
        hasBeenConfigured = false;
    }

    clear{
        configFunc = { |scene ... extraArgs| };
        playFunc = { |scene ... extraArgs| };
        stopFunc = { |scene ... extraArgs| };
        cleanupFunc = { |scene ... extraArgs| };
        data = IdentityDictionary.new;
        hasBeenConfigured = false;
    }

    // CuePlayer integration
    // This will automatically call .play when the cue is triggered and .stop when the cue is stopped
    asCueInfo{
        ^CueInfo.new(key, key, {|cue|

            // Register cleanup functions
            cue.hook = {
                this.stop;
            };

            this.play();
        })
    }

    addToCuePlayer{ arg cuePlayer;
        cuePlayer.add(this.asCueInfo);
    }

    putInCuePlayer{ arg cuePlayer, index;
        cuePlayer.put(index, this.asCueInfo);
    }

    // FIXME: doesnt work
    // copy{|toKey|
    //     if(toKey.isNil or: { key == toKey }) { Error("can only copy to new key (key is %)".format(toKey)).throw };
    //     ^this.class.new(toKey, configFunc: configFunc, playFunc: playFunc, stopFunc: stopFunc, cleanupFunc: cleanupFunc).data_(data);
    // }
}

// SceneDef : PatchDef{
//     classvar <all;
//     *initClass {
//         this.all = IdentityDictionary.new;
//     }
// }

// PerformanceDef : PatchDef{
//     classvar <all;
// *initClass {
//                    all = IdentityDictionary.new;
//                }
// }
