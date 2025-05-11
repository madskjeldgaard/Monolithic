PNoteEcho : FilterPattern {
    var <>pattern, <>delayTimes, <>ampCurve, <>pitchOffset, <>numEchos;

    *new { |pattern, delayTimes = 0.25, ampCurve = 0.5, pitchOffset = 0, numEchos = 3|
        ^super.new().init(pattern, delayTimes, ampCurve, pitchOffset, numEchos);
    }

    init { |pt, dt, ac, po, ne|
        pattern = pt;
        delayTimes = dt;
        ampCurve = ac;
        pitchOffset = po;
        numEchos = ne;
    }

    embedInStream { |inval|
        var eventStream = pattern.asStream;
        var delayStream = delayTimes.asStream;
        var ampStream = ampCurve.asStream;
        var pitchStream = pitchOffset.asStream;
        var numEchosStream = numEchos.asStream;
        var originalEvent;

        while {
            originalEvent = eventStream.next(inval);
            originalEvent.notNil
        } {
            var currentDelay = delayStream.next(inval) ? 0.25;
            var currentAmp = ampStream.next(inval) ? 0.5;
            var currentPitch = pitchStream.next(inval) ? 0;
            var numEchos = (numEchosStream.next(inval) ? 1);

            // Yield the original event first
            inval = originalEvent.copy.yield;

            // Create echo events
            numEchos.do { |i|
                var echoEvent = originalEvent.deepCopy;
                var amp = (echoEvent[\amp] ? 1) * (currentAmp.pow(i + 1));
                var delay = currentDelay * (i + 1);
                var pitch = currentPitch * (i + 1);

                // Apply timing - using delta is most reliable
                echoEvent[\delta] =  delay;

                echoEvent.removeAt(\dur);

                // Apply amplitude
                echoEvent[\amp] = amp;

                echoEvent[\ctranspose] = (echoEvent[\ctranspose] ? 0) + pitch;

                // Post the echo event for debugging
                // ("Echo" + (i+1) + ":" + echoEvent).postln;

                inval = echoEvent.yield;
            };
        };

        ^inval;
    }
}
