// Generates all midi notes in scale
MK_MidiScale{
    *new{|scale|
        var notes = (0..127).collect{|midiNote| midiNote.nearestInScale(scale) };

        // Remove duplicates
        notes = notes.asSet.asArray;

        ^notes;
    }
}

+Scale{
    asMidiNotes{
        ^MK_MidiScale.new(this)
    }
}

// Convert a number to a midi note in a scale
+SimpleNumber{
    snapToMidiScale{|scale|
        var midiScale = scale.asMidiNotes();
        var midiNote = this.asInteger;
        var indexOfNote = midiScale.indexOfEqual(midiNote);

        while({indexOfNote.isNil},{
            indexOfNote = midiScale.indexOfEqual(midiNote + 1);
            midiNote = midiNote + 1;
        });

        if(midiNote != this, {
            "Snapped % to midinote %".format(this, midiNote).postln;
        });

        ^midiNote
    }
}
