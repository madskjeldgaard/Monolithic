+ SimpleNumber {
    midinote2Rate{|referenceNote=60|
        var noteIn = this;
        var distanceFromReferenceNote = noteIn - referenceNote;
        var rate = distanceFromReferenceNote.midiratio;

        ^rate
    }
}
