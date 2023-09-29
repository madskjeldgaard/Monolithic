+ SimpleNumber {

    // Convert a midi note to playrate
    midinote2Rate{|referenceNote=60|
        var noteIn = this;
        var distanceFromReferenceNote = noteIn - referenceNote;
        var rate = distanceFromReferenceNote.midiratio;

        ^rate
    }

    // Convert a scale degree, octave and scale to a midi note using the scale object and the .nearestInScale method
    scaleDegree2Midinote{|scale, root = 0, degree = 0, octave = 4, stepsPerOctave=12|
        var noteIn = this;
        var octaveNotes = octave * stepsPerOctave;
        var noteOut = scale.at(degree) + octaveNotes + root;

        ^noteOut
    }

    scaleDegree2PlayRate{|scale, root = 0, degree = 0, octave = 4, stepsPerOctave=12, referenceNote=60|
        var noteIn = this;
        var noteOut = noteIn.scaleDegree2Midinote(scale, root, degree, octave, stepsPerOctave);
        var rate = noteOut.midinote2Rate(referenceNote);

        ^rate
    }

}
