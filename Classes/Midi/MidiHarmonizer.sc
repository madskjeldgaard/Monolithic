/*


TODO:
- voicing – drop 2, 3, drop 2 and 3, drop 2 and 4

*/

MidiHarmonizer2 {
    var <chordNotes;
    var thisScale, midiNotesInScale;
    var rootNoteOffset;

    *new{|root=0, scale|
        ^super.new.init(root, scale);
    }

    init{|root, scale|
        rootNoteOffset = root;
        thisScale = scale;
        midiNotesInScale = thisScale.asMidiNotes();
    }

    // Density is amount of notes in chord: 3 is a triad, anything above that will add octaves to the top/bottom
    // Chord extension is 0-4: 0 is triad, 1 is 6th, 2 is 6th and 7th, 3 is 6th, 7th and 9th, 4 is 6th, 7th, 9th and 11th
    // Inversion is 0-2: 0 is root position, 1 is first inversion, 2 is second inversion
    harmonizeMidiNote{|midiNote, density=3, extend=0, invert=0|

      var chordNotes, rootNote;
      var midiNoteIndex;

      midiNote = rootNote = midiNote.snapToMidiScale(midiNotesInScale);
      midiNoteIndex = midiNotesInScale.indexOf(midiNote);

      // Sanitize density parameter
      density = density.asInteger.max(1);

      // Generate initial triad and add octaves if density is above 3
      chordNotes = density.collect{|chordNoteIndex|
          var chordNote = midiNote;

          // Get next note in chord
          if(chordNoteIndex > 0, {
              // Find the note that is 2 scale degrees from the previous note
              chordNote = this.prGetNoteXDegreesFromTriadStart(rootNote, 2 * chordNoteIndex);
          });

          // If we are above the triad, it's time to add some octaves
          if(chordNoteIndex > 2, {
              var stepsFromTriad = (chordNoteIndex-2).abs;
              var numOctaves =(stepsFromTriad / 2).max(1).round(1); // FIXME: Not sure why this round and max is needed? But it is.. trust me, it is....
              chordNote = this.octaved(rootNote, above: stepsFromTriad.even, numOctaves: numOctaves);
          });

          chordNote
      };

      // Invert chord if inversion is 1 or 2
      invert.switch(
          1, {
              // Invert second note
              chordNotes[0] = (chordNotes[0] + 12).clip(0,127);
          },
          2, {
              // Invert third note
              chordNotes[0] = (chordNotes[0] + 12).clip(0,127);
              chordNotes[1] = (chordNotes[1] + 12).clip(0,127);
          }
      );

      // Chord extensions
      extend.switch(
          1, {
              chordNotes = this.addSixth(chordNotes, rootNote);
          },
          2, {
              chordNotes = this.addSixth(chordNotes, rootNote);
              chordNotes = this.addSeventh(chordNotes, rootNote);
          },
          3, {
              chordNotes = this.addSixth(chordNotes, rootNote);
              chordNotes = this.addSeventh(chordNotes, rootNote);
              chordNotes = this.addNinth(chordNotes, rootNote);
          },
          4, {
              chordNotes = this.addSixth(chordNotes, rootNote);
              chordNotes = this.addSeventh(chordNotes, rootNote);
              chordNotes = this.addNinth(chordNotes, rootNote);
              chordNotes = this.addEleventh(chordNotes, rootNote);
          }
      );

      // Add root note to the end
      chordNotes = chordNotes.collect{|nnn| nnn + rootNoteOffset };

      // Do a final sanitize just in case: Remove duplicates and convert all to integer
      chordNotes = chordNotes.asSet.asArray.sort.collect{|nnn| nnn.asInteger.clip(0,127)};

      ^chordNotes;
    }

    //------------------------------------------------------------------//
    //                         Internal methods                         //
    //------------------------------------------------------------------//

    addSixth{|chordNotes, rootNote|
        var sixthNote = this.prGetNoteXDegreesFromTriadStart(rootNote, 5);
        ^(chordNotes ++ [sixthNote]).sort
    }

    addSeventh{|chordNotes, rootNote|
        var seventhNote = this.prGetNoteXDegreesFromTriadStart(rootNote, 6);

        ^(chordNotes ++ [seventhNote]).sort
    }

    addNinth{|chordNotes, rootNote|
        var ninthNote = this.prGetNoteXDegreesFromTriadStart(rootNote, 8);

        ^(chordNotes ++ [ninthNote]).sort
    }

    addEleventh{|chordNotes, rootNote|
        var eleventhNote = this.prGetNoteXDegreesFromTriadStart(rootNote, 10);

        ^(chordNotes ++ [eleventhNote]).sort
    }

    prGetNoteXDegreesFromTriadStart{|chordRootNote, degreesFromRoot|
        var notes = midiNotesInScale;
        var indexOfRootNote = notes.indexOf(chordRootNote);
        var note = notes.clipAt(indexOfRootNote + degreesFromRoot);

        ^note
    }

    octaved{|midiNoteIn, above=true, numOctaves = 1|
        numOctaves = numOctaves.max(1).round(1).postln;
        ^above.if(
            { midiNoteIn + (numOctaves * 12) },
            { midiNoteIn - (numOctaves * 12) }
        )
    }
}
