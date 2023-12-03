// A simple class that holds a numeric value, and when you change it, it will gradually change it to the new value over time.
SlidingValue{
    classvar clock;
    var <destinationValue = 0.0, <currentValue = 0.0, <slideTime = 1.0, <>timeGrain = 0.01;

    // This function is called with the current value, at each step of the slide.
    var <>func;

    // The routine that handles the sliding.
    var task;

    *new{|value, function|
        ^super.new.init(value, function);
    }

    *initClass{
        clock = TempoClock.new.permanent_(true);
    }

    time_{|newSlideTime|
        slideTime = newSlideTime;
        timeGrain = (slideTime / 100.0).max(minTimeGrain);
    }


    init{|value, function|
        destinationValue = value;
        func = function;
    }

    set{|value|
        destinationValue = value;
        if(task == nil){
            task = this.prMakeRoutine();
            task.play(argClock: clock);
        }{
            task.stop();
            task = this.prMakeRoutine();
            task.play(argClock: clock);
        }

    }

    prMakeRoutine{
        ^Task.new({
            var deltaValue = destinationValue - currentValue;
            var steps = slideTime / timeGrain;
            var stepValue = deltaValue / steps;
            var done = false;

            steps.do{
                currentValue = currentValue + stepValue;
                func.value(currentValue);

                if(deltaValue > 0){
                    if(currentValue >= destinationValue){
                        func.value(destinationValue);
                        done = true;
                    }
                }{
                    if(currentValue <= destinationValue){
                        func.value(destinationValue);
                        done = true;
                    }
                };

                done.not.if({
                    timeGrain.wait;
                });
            };

            task = nil;

        })
    }

}
