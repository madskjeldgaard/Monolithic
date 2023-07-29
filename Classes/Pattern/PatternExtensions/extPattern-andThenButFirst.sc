// First play a value, then continue with the pattern
+ Pattern{
    butFirst{|firstThis|
        var thenThat = this;
        ^Pseq([firstThis, thenThat], 1)
    }
}

// Play something and then play a static value a repeat amount of times
// Good for fade-ins etc
+ Pattern{
    andThen{|thenThis, repeat|
        var firstThis = this;
        ^Pseq([firstThis, Pseq([thenThis], repeat)], 1)
    }
}
