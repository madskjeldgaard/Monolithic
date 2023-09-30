+ SimpleNumber {

    // Convert a midi note to playrate
    midinote2Rate{|referenceNote=48|
        var noteIn = this;
        var distanceFromReferenceNote = noteIn - referenceNote;
        var rate = distanceFromReferenceNote.midiratio;

        ^rate
    }

    // Convert a scale degree, octave and scale to a midi note using the scale object and the .nearestInScale method
    scaleDegree2Midinote{|scale, root = 0, degree = 0, octave = 4, stepsPerOctave=12|
        var noteIn = this;
        var scaleSize = scale.size;
        var octaveNotes = octave * stepsPerOctave;
        var noteOut;

        // Add/subtract octaves if the scale degree is out of range
        if(degree < 0) {
            // Negative scale degrees
            var numOctavesOff = (degree.abs / scaleSize).roundUp(1);
            octaveNotes = octaveNotes - (numOctavesOff * stepsPerOctave);
        }{
            // Positive scale degrees above the scale size
            if(degree >= scaleSize) {
                var numOctavesOff = (degree / scaleSize).roundUp(1) - 1;
                octaveNotes = octaveNotes + (numOctavesOff * stepsPerOctave);
            };
        };

        noteOut = scale.at(degree) + octaveNotes + root;

        ^noteOut
    }

    scaleDegree2PlayRate{|scale, root = 0, degree = 0, octave = 4, stepsPerOctave=12, referenceNote=48|
        var noteIn = this;
        var noteOut = noteIn.scaleDegree2Midinote(scale, root, degree, octave, stepsPerOctave);
        var rate = noteOut.midinote2Rate(referenceNote);

        ^rate
    }

}
