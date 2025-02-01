/*

A simple way to map MIDI controls to functions in SuperCollider.

It includes MIDI Learn functionality to easily map MIDI controls to functions, as well as the ability to save and load mappings to a file.

// Example:
(

// Initialize MIDI
MIDIClient.init;
MIDIIn.connectAll;

// Initialize the SimpleMIDIMapper
~midiController = SimpleMIDIMapper.new("midiMappings.scd");

// Load existing MIDI mappings (if any)
~midiController.loadMappings;

"Mapping cc 0 to volume".postln;
// Example: Map a function to a MIDI control
~midiController.map(\cc, 0, 1, { |val|
    var volume = val.linlin(0, 127, 0.0, 1.0);
    ("Volume set to " ++ volume).postln;
});

"Turn a knob on your MIDI controller to see map it to the rate parameter".postln;
// Example: Enable MIDI Learn to map a new control
~midiController.enableMIDILearn({ |val|
    var rate = val.linexp(0, 127, 0.01, 4.0);
    ("Rate set to " ++ rate).postln;
});

// Save MIDI mappings to a file
// ~midiController.saveMappings;
)

*/
SimpleMIDIMapper {
    var <midiActions; // Dictionary to store MIDI mappings
    var <midiLearnEnabled = false; // MIDI Learn mode flag
    var <midiLearnCallback; // Callback for MIDI Learn
    var <midiMappingsPath; // Path to save/load MIDI mappings

    *new { |midiMappingsPath|
        ^super.new.init(midiMappingsPath);
    }

    init { |path|
        midiActions = Dictionary.new;
        midiMappingsPath = path ? "midiMappings.scd"; // Default path for saving/loading mappings

        // Set up MIDI handlers
        this.setupMIDIHandlers;
    }

    // Setup MIDI handlers for noteOn, noteOff, and CC events
    setupMIDIHandlers {
        MIDIdef.noteOn(\midiNoteOnHandler, { |val, num, chan|
            var key = [\noteOn, chan, num];
            this.handleMIDIEvent(key, val);
        });

        MIDIdef.noteOff(\midiNoteOffHandler, { |val, num, chan|
            var key = [\noteOff, chan, num];
            this.handleMIDIEvent(key, val);
        });

        MIDIdef.cc(\midiCCHandler, { |val, num, chan|
            var key = [\cc, chan, num];
            this.handleMIDIEvent(key, val);
        });

        // MIDI input handler for ccOn and ccOff events, ie cc buttons
        MIDIdef.cc(\midiCCOnHandler, { |val, num, chan|
            var keyOn = [\ccOn, chan, num];
            var keyOff = [\ccOff, chan, num];
            if (val > 63) {
                this.handleMIDIEvent(keyOn, 1);
            } {
                this.handleMIDIEvent(keyOff, 0);
            }
        });

        // Program change handler
        MIDIdef.programChange(\midiProgramChangeHandler, { |val, chan|
            var key = [\programChange, chan, val];
            this.handleMIDIEvent(key, 1);
        });

        // Pitch bend handler
        MIDIdef.pitchBend(\midiPitchBendHandler, { |val, chan|
            var key = [\pitchBend, chan, val];
            this.handleMIDIEvent(key, val);
        });

        // Aftertouch handler
        MIDIdef.aftertouch(\midiAftertouchHandler, { |val, chan|
            var key = [\aftertouch, chan, val];
            this.handleMIDIEvent(key, val);
        });
    }

    // Handle MIDI events
    handleMIDIEvent { |key, val|
        if (midiLearnEnabled && midiLearnCallback.notNil) {
            // If MIDI Learn is enabled, assign the callback to the key
            midiActions[key] = midiLearnCallback;
            midiLearnEnabled = false; // Disable MIDI Learn after mapping
            ("MIDI mapping learned for " ++ key).postln;
        } {
            // Otherwise, execute the mapped action
            if (midiActions[key].notNil) {
                midiActions[key].value(val);
            } {
                ("No action mapped for " ++ key).postln;
            };
        };
    }

    // Enable MIDI Learn mode
    enableMIDILearn { |callback|
        midiLearnEnabled = true;
        midiLearnCallback = callback;
        "MIDI Learn enabled. Press a MIDI control to map it.".postln;
    }

    // Map a function to a MIDI control
    map { |type, channel, controlNumber, action|
        var key = [type, channel, controlNumber];
        midiActions[key] = action;
        ("MIDI control mapped: " ++ key).postln;
    }

    // Unmap a MIDI control
    unmap { |type, channel, controlNumber|
        var key = [type, channel, controlNumber];
        midiActions.removeAt(key);
        ("MIDI control unmapped: " ++ key).postln;
    }

    // Save MIDI mappings to a file
    saveMappings {
        var file = File(midiMappingsPath, "w");
        file.write(midiActions.asCompileString);
        file.close;
        ("MIDI mappings saved to " ++ midiMappingsPath).postln;
    }

    // Load MIDI mappings from a file
    loadMappings {
        var file = File(midiMappingsPath, "r");
        if (file.isOpen) {
            midiActions = file.readAllString.interpret;
            file.close;
            ("MIDI mappings loaded from " ++ midiMappingsPath).postln;
        } {
            ("Failed to load MIDI mappings from " ++ midiMappingsPath).postln;
        };
    }
}
