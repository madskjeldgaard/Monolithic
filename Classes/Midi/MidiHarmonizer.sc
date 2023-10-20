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
    // Drop is 0-3: 0 is no drop, 1 is drop 2, 2 is drop 3, 3 is drop 2 and 3
    harmonizeMidiNote{|midiNote, density=3, extend=0, invert=0, drop|

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

      // Inversion only works i f there are atlesast 3 notes in the chord
      if(chordNotes.size < 3, {
          invert = 0;
      });

      // Invert chord if inversion is 1 or 2
      invert.switch(
          1, {
              var invertedNote = chordNotes[0] + 12;
              // Invert second note

              // If this note is negative, simply skip it
              if(invertedNote >= 0, {
                  chordNotes[0] = invertedNote.clip(0,127);
              });

          },
          2, {
              var invertedNote = chordNotes[0] + 12;
              var invertedNote2 = chordNotes[1] + 12;

              if(invertedNote >= 0, {
                  chordNotes[0] = invertedNote.clip(0,127);
              });

              if(invertedNote2 >= 0, {
                  chordNotes[1] = invertedNote2.clip(0,127);
              })

          }
      );

      // Chord extensions
      if(extend.isKindOf(SimpleNumber), {

          extend.switch(
              1, {
                  chordNotes = this.addSixth(chordNotes, rootNote);
              },
              2, {
                  chordNotes = this.addSeventh(chordNotes, rootNote);
              },
              3, {
                  chordNotes = this.addNinth(chordNotes, rootNote);
              },
              4, {
                  chordNotes = this.addEleventh(chordNotes, rootNote);
              },
              5, {
                  chordNotes = this.addSixth(chordNotes, rootNote);
                  chordNotes = this.addSeventh(chordNotes, rootNote);
              },
              6, {
                  chordNotes = this.addSixth(chordNotes, rootNote);
                  chordNotes = this.addSeventh(chordNotes, rootNote);
                  chordNotes = this.addNinth(chordNotes, rootNote);
              },
              7, {
                  chordNotes = this.addSixth(chordNotes, rootNote);
                  chordNotes = this.addSeventh(chordNotes, rootNote);
                  chordNotes = this.addNinth(chordNotes, rootNote);
                  chordNotes = this.addEleventh(chordNotes, rootNote);
              }
          );

      });

      if(extend.isKindOf(Array), {
          extend.do{|extension|
              chordNotes = this.prAddExtension(chordNotes, rootNote, extension);
          };
      });

      // Drop 2 chords (or drop 2 voicings) refer to taking a closed-position chord and dropping the second-highest note down an octave
      // Drop 3 chords (or drop 3 voicings) refer to taking a closed-position chord and dropping the third-highest note down an octave
      // Drop 2 and 4 chords (or drop 2 and 4 voicings) refer to taking a closed-position chord and dropping the second- and fourth-highest notes down an octave

      // Sanitize drop parameter
      // If we have less than 4 notes, we can't do drop 2, 3 or 2 and 3
      if(chordNotes.size < 4, {
          drop = 0;
      });

      drop.switch(
          1, {
              // Drop 2
              chordNotes = chordNotes.sort;
              chordNotes = chordNotes[0..1] ++ [chordNotes[2] - 12] ++ chordNotes[3..-1];
          },
          2, {
              // Drop 3
              chordNotes = chordNotes.sort;
              chordNotes = chordNotes[0..2] ++ [chordNotes[3] - 12];
          },
          3, {
              // Drop 2 and 3
              chordNotes = chordNotes.sort;
              chordNotes = chordNotes[0..1] ++ [chordNotes[2] - 12] ++ [chordNotes[3] - 12];
          }
      );

      // Add root note to the end
      chordNotes = chordNotes.collect{|nnn| nnn + rootNoteOffset };

      // Do a final sanitize just in case: Remove duplicates and convert all to integer
      chordNotes = chordNotes.withoutDuplicates.sort.collect{|nnn| nnn.asInteger.clip(0,127)};


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

    prAddExtension{|chordNotes, rootNote, extension|
        var extensionNote;

        extensionNote = this.prGetNoteXDegreesFromTriadStart(rootNote, extension);

        ^(chordNotes ++ [extensionNote]).sort
    }


    octaved{|midiNoteIn, above=true, numOctaves = 1|
        numOctaves = numOctaves.max(1).round(1);
        ^above.if(
            { midiNoteIn + (numOctaves * 12) },
            { midiNoteIn - (numOctaves * 12) }
        )
    }
}
