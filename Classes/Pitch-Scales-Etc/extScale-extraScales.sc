+ Scale{
    *minorHarmonicPentatonic{
        ^Scale.new(degrees: Scale.harmonicMinor.degrees[0..4], pitchesPerOctave: 12, tuning: Tuning.et12, name: 'minorHarmonicPentatonic')
    }

    *minorMelodicPentatonic{
        ^Scale.new(
            degrees: Scale.melodicMinor.degrees[0..4],
            pitchesPerOctave: 12,
            tuning: Tuning.et12,
            name: 'minorMelodicPentatonic'
        )

    }
}
