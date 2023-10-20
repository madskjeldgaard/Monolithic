+ Collection{

    notesOn{|midiTarget, channel=0, velocity=100|
        this.asArray.do{|note|
            midiTarget.noteOn(channel, note, velocity)
        }
    }

    notesOff{|midiTarget, channel=0, velocity=0|
        this.asArray.do{|note|
            midiTarget.noteOff(channel, note, velocity)
        }
    }

}

+ MIDIOut{

    notesOn{|notesCollection, channel=0, velocity=100|
        notesCollection.do{|note|
            this.noteOn(channel, note, velocity)
        }
    }

    notesOff{|notesCollection, channel=0, velocity=100|
        notesCollection.do{|note|
            this.noteOff(channel, note, velocity)
        }
    }

}

+ MIDIEndPoint{

    notesOn{|notesCollection, channel=0, velocity=100|
        notesCollection.do{|note|
            this.noteOn(channel, note, velocity)
        }
    }

    notesOff{|notesCollection, channel=0, velocity=100|
        notesCollection.do{|note|
            this.noteOff(channel, note, velocity)
        }
    }
}
