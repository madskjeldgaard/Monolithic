// Convert a string to a root note, for use with Scale and patterns
+ String{
    asRoot{
        var notes = ["C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"];
        var index = notes.indexOfEqual(this.toUpper);
        ^index
    }
}

+ Symbol{
    asRoot{
        ^this.asString.asRoot
    }
}

+ Integer {
    asRoot{
        ^this
    }
}
