// Classes for making persistent tempoclocks with Def-style
/*

// EXAMPLE:

// Def-style access - always returns the same instance for a given key
TempoClockDef(\myDefClock).bpm_(100);

// Play a pattern with the clock
s.waitForBoot{ Pbind().play(clock:TempoClockDef(\myDefClock)) }

// Change bpm
TempoClockDef(\myDefClock).bpm_(333);

// Run cmdPeriod
CmdPeriod.run();

// Play pattern again, the clock is still there and has the same tempo
Pbind().play(clock:TempoClockDef(\myDefClock));

*/

/*

TODO:
- The GUI jumps back to old value when running CmdPeriod

*/

TempoClockProx : TempoClock {
    classvar <all;
    var <bpm, <>gui, <persist = true;
    var <>savedTempo, <>savedBeats, <>savedSeconds, <>savedQueueSize;

    *initClass {
        all = IdentitySet.new;
        CmdPeriod.add({ this.reinstantiateAll });
    }

    *new { |name, tempo, beats, seconds, queueSize = 256|
        var clock = super.new(name, tempo, beats, seconds, queueSize);
        all = all.add(clock);
        clock.savedTempo = tempo ? 1.0;
        clock.savedBeats = beats;
        clock.savedSeconds = seconds;
        clock.savedQueueSize = queueSize;
        ^clock;
    }

    *reinstantiateAll {
        all.do { |clock|
            if (clock.persist and: { clock.isStopped }) {
                clock.reinstantiate;
            }
        };
    }

    reinstantiate {
        var newClock = this.class.new(
            this.name,
            this.savedTempo,
            this.savedBeats,
            this.savedSeconds,
            this.savedQueueSize
        );

        // Copy other properties
        newClock.bpm = this.bpm ? (this.savedTempo * 60);
        newClock.persist = this.persist;

        // Transfer GUI to new instance
        if (this.gui.notNil) {
            var oldGUI = this.gui;
            newClock.gui = oldGUI;

            // Update the slider action to control the new clock instance
            oldGUI.slider.action_({ |sl|
                var newBpm = oldGUI.spec.map(sl.value);
                newClock.bpm_(newBpm);
                oldGUI.label.string_(newBpm.round(0.1).asString ++ " BPM");
            });

            // Update the label with current BPM
            newClock.updateGUI;
        }

        ^newClock;
    }

    bpm_ { |newBpm|
        bpm = newBpm;
        this.savedTempo = newBpm / 60;
        if (this.isRunning) {
            this.tempo = this.savedTempo;
        };
        this.updateGUI;
    }

    tempo_ { |newTempo|
        if (this.isRunning) {
            super.tempo_(newTempo);
        };
        bpm = newTempo * 60;
        this.savedTempo = newTempo;
        this.updateGUI;
    }

    isRunning {
        ^this.ptr.notNil;
    }

    isStopped {
        ^this.ptr.isNil;
    }

    makeGui { |parent, bounds, minBpm = 40, maxBpm = 240, curve = \lin, sliderOrientation = \vertical|
        var view, slider, label;
        var spec, sliderBounds, labelBounds;

        view = View(parent, bounds ?? {
            Rect(
                0,
                0,
                (sliderOrientation == \vertical).if({ 60 }, { 200 }),
                (sliderOrientation == \vertical).if({ 200 }, { 60 })
            )
        });

        // Create mapping spec
        spec = if (curve == \lin) {
            ControlSpec(minBpm, maxBpm, \lin, 1, bpm ? (this.savedTempo * 60))
        } {
            ControlSpec(minBpm, maxBpm, \exp, 0.1, bpm ? (this.savedTempo * 60))
        };

        // Calculate bounds based on orientation
        if (sliderOrientation == \vertical) {
            sliderBounds = Rect(10, 10, 40, view.bounds.height - 50);
            labelBounds = Rect(0, view.bounds.height - 30, view.bounds.width, 20);
        } {
            sliderBounds = Rect(10, 10, view.bounds.width - 20, 30);
            labelBounds = Rect(0, 40, view.bounds.width, 20);
        };

        slider = Slider(view, sliderBounds)
            .value_(spec.unmap(bpm ? (this.savedTempo * 60)))
            .orientation_(sliderOrientation)
            .action_({ |sl|
                var newBpm = spec.map(sl.value);
                this.bpm_(newBpm);
                label.string_(newBpm.round(0.1).asString ++ " BPM");
            });

        label = StaticText(view, labelBounds)
            .string_((bpm ? (this.savedTempo * 60)).round(0.1).asString ++ " BPM")
            .align_(\center);

        gui = (
            view: view,
            slider: slider,
            label: label,
            spec: spec
        );

        ^view;
    }

    updateGUI {
        if (gui.notNil) {
            {
                var currentBpm = bpm ? (this.savedTempo * 60);
                gui.slider.value = gui.spec.unmap(currentBpm);
                gui.label.string = currentBpm.round(0.1).asString ++ " BPM";
            }.defer;
        }
    }

    free {
        all.remove(this);
        if (gui.notNil) {
            gui.view.close;
            gui = nil;
        };
        super.free;
    }

    persist_ { |bool|
        persist = bool;
        if (bool) {
            all.add(this);
        } {
            all.remove(this);
        }
    }
}

TempoClockDef : TempoClockProx {
    classvar <all;
    var <>key;

    *initClass {
        all = IdentityDictionary.new;
        CmdPeriod.add({ this.reinstantiateAll });
    }

    *new { |key, tempo, beats, seconds, queueSize = 256|
        var instance;

        if (all[key].notNil and: { all[key].isStopped.not }) {
            ^all[key];
        };

        instance = super.new(key, tempo, beats, seconds, queueSize);
        instance.key = key;
        all[key] = instance;

        ^instance;
    }

    *at { |key|
        ^all[key];
    }

    *keys {
        ^all.keys;
    }

    *free { |key|
        if (key.isNil) {
            all.do(_.free);
            all.clear;
        } {
            all[key].free;
            all.removeAt(key);
        }
    }

    *reinstantiateAll {
        all.keys.do { |k|
            var clock = all[k];
            if (clock.persist and: { clock.isStopped }) {
                fork {
                    0.1.wait; // Small delay to ensure clean state
                    clock.reinstantiate;
                };
            }
        };
    }

    reinstantiate {
        var newClock = this.class.new(
            this.key,
            this.savedTempo,
            this.savedBeats,
            this.savedSeconds,
            this.savedQueueSize
        );

        // Copy properties
        newClock.bpm = this.bpm ? (this.savedTempo * 60);
        newClock.persist = this.persist;

        // Transfer GUI to new instance
        if (this.gui.notNil) {
            var oldGUI = this.gui;
            newClock.gui = oldGUI;

            // Update the slider action to control the new clock instance
            oldGUI.slider.action_({ |sl|
                var newBpm = oldGUI.spec.map(sl.value);
                newClock.bpm_(newBpm);
                oldGUI.label.string_(newBpm.round(0.1).asString ++ " BPM");
            });

            // Update the label with current BPM
            newClock.updateGUI;
        }

        ^newClock;
    }

    free {
        all.removeAt(this.key);
        super.free;
    }

    // Make it accessible like Def-style
    *default {
        ^this.new(\default);
    }

    *prox { |key|
        ^this.at(key) ?? { this.new(key) };
    }
