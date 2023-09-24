/*

A factory that creates a timeline which calls a function at each time step and uses an envelope to feed it progressing values.

// Example:
(
Ndef(\s, {|amp=0.0|
    SinOsc.ar(freq:110, phase:0.0, mul:amp)!2
});

// Make the trajectory
t = TimedTrajectory(
    totalDuration: 8.0,
    timeGrain: 0.1,
    func: {|envValue, timeVal, normalizedIndex, index, numSteps|
        "envValue: %, timeVal: %, normalizedIndex: %, index: %, numSteps: %".format(
            envValue,
            timeVal,
            normalizedIndex,
            index,
            numSteps
        ).postln;

        Ndef(\s).set(\amp, envValue);

        if(index == (numSteps - 1),
            Ndef(\s).set(\amp, 0.0);
        })
    },
    env: Env([0.00001, 1.0], times: [1], curve: \exp),
    clock: nil,
    options: (),
);

Ndef(\s).play;
t.play;
)

*/
TimedTrajectory{
    var <timeline, <array, <envelope;

    *new{|totalDuration, timeGrain=1.0, func, env, clock, options|
        ^super.new().init(totalDuration, timeGrain, func, env, clock, options);
    }

    init{|totalDuration, timeGrain=1.0, func, env, clock, options|
        var numSteps = (totalDuration/timeGrain).asInteger;
        envelope = env;

        // Behind the scenes
        array = Array.fill(numSteps, {|index|
            var time = index * timeGrain;
            var normalizedIndex = index / (numSteps-1);
            var envVal = env.at(normalizedIndex);
            var value = {
                func.value(envVal, time, normalizedIndex, index, numSteps)
            };

            [time, value];
        }).flatten;

        timeline = Timeline.newFromArray(array:array, clock:clock, options:options);
    }

    play{
        timeline.play;
    }

    stop{
        timeline.stop;
    }

    plot{
        ^envelope.plot
    }
}

+ Env{
    asTimedTrajectory{|totalDuration, timeGrain=1.0, func, clock, options|
        ^TimedTrajectory.new(
            totalDuration: totalDuration,
            timeGrain: timeGrain,
            func: func,
            env: this,
            clock: clock,
            options: options,
        )
    }
}
