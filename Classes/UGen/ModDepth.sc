// A short hand for calculating the modulation depth of things
// Value is the value (eg. pan), modulator is some kind of LFO and modDepth is a value between 0.0 and 1.0 (0.0 = no modulation, 1.0 = full modulation)
// At 0 modDepth the value is returned, at 1.0 the modulator is returned, at 0.5 the value and modulator are mixed 50/50
ModDepth{
    *new{|value, modulator, modDepth|
        ^(1.0 - modDepth) * value + (value * modDepth * modulator);
    }
}

+ UGen{
    modDepth{|modulator, modDepth|
        ^ModDepth.new(this, modulator, modDepth);
    }
}
