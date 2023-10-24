/*

This class makes it easier to create an interface for a midi device – it tries to make up for the absolute chaos that is MIDIOut, etc..

See the IAC class for a simple example.

*/
MIDIDeviceInterface{
    var <port, <sourceUID, <endpointUID, <callbacks, <sourceIndex, <destinationIndex;
    classvar <availableDevices;
    classvar <devices, <midiouts;
    classvar >midiDeviceName ;

    *new{|deviceport="", srcUID, endUID|
        var newObject = super.newCopyArgs(deviceport, srcUID, endUID);
        this.initialiseMidiDevices();
        this.syncAvailableDevices(this.midiDeviceName);

        ^newObject.init();
    }

    *initialiseMidiDevices{
        // Connect midi controller
        if(MIDIClient.initialized.not, {
            "MIDIClient not initialized... initializing now".postln;
            MIDIClient.init;
        });
    }

    *getMIDIEndpoint{|deviceName, portName|
        this.initialiseMidiDevices();
        ^MIDIClient.destinations.select{|dest|
            dest.device == deviceName && dest.name == portName
        }.first;
    }

    *getMIDISource{|deviceName, portName|
        this.initialiseMidiDevices();
        ^MIDIClient.sources.select{|src|
            src.device == deviceName && src.name == portName
        }.first;
    }

    *syncAvailableDevices{|midiDeviceName|
        midiouts = midiouts ? Dictionary.new;

        // Inputs
        availableDevices = MIDIClient.sources.select{|source| source.device == midiDeviceName};
        "Found:".postln;
        availableDevices.do{|e| e.postln};

        // Outputs
        MIDIClient.destinations.do{|dest, destNum|
            (dest.device == midiDeviceName).if({
                var midiout = MIDIOut.new(destNum.postln, dest.uid.postln);

                if(midiout.isNil, {
                    "Could not create midiout for %".format(dest.name).error;
                }, {
                    // Set default latency to 0 because it's hardware
                    midiout.latency_(0);

                    "Adding midiout to %".format(destNum).postln;
                    midiouts = midiouts.put(destNum, midiout);
                })
            });
        };
    }

    *prIndexOfMidiDestination{|deviceName, port, uid|
        var ind = nil;
        MIDIClient.destinations.do{|dest, thisIndex|
            var predicate = dest.device == deviceName;
            if(port.notNil, {
                predicate = predicate && dest.name == port;
            });

            if(uid.notNil, {
                predicate = predicate && dest.uid == uid;
            });

            predicate.if({
                ind = thisIndex;
            });
        };

        ^ind
    }

    *prIndexOfMidiSource{|deviceName, port, uid|
        var ind = nil;
        MIDIClient.sources.do{
            |src, thisIndex|
            var predicate = src.device == deviceName;

            if(port.notNil, {
                predicate = predicate && src.name == port;
            });

            if(uid.notNil, {
                predicate = predicate && src.uid == uid;
            });

            predicate.if({
                ind = thisIndex;
            });
        };

        ^ind
    }

    *prMakeID{|deviceName, num|
        ^(deviceName ++ (num.asString.replace("-", "") ? "")).replace(" ", "").asSymbol;
    }

    // This whole method is really nasty
    *connectDevice{|midiDeviceName, port, srcUID, afterConnectionAction|
        var sourceIndex = this.prIndexOfMidiSource(midiDeviceName, port, srcUID);
        if(sourceIndex.isNil, {
            "No midi source found for %, UID %, port %".format(midiDeviceName, srcUID, port).error;
            ^nil;
        }, {
            var midiSource = MIDIClient.sources.at(sourceIndex);
            MIDIIn.connect(sourceIndex, midiSource);
        });
    }

    //------------------------------------------------------------------//
    //                             Instance                             //
    //------------------------------------------------------------------//

    *midiDeviceName{
        ^this.subclassResponsibility()
    }

    init{
        destinationIndex = MIDIDeviceInterface.prIndexOfMidiDestination(this.class.midiDeviceName, port, endpointUID);
        sourceIndex = MIDIDeviceInterface.prIndexOfMidiSource(this.class.midiDeviceName, port, sourceUID);
        this.connect(port, sourceUID);
    }

    connect{|port, inUid|
        this.class.connectDevice(this.class.midiDeviceName, port, inUid);
    }

    device {
        ^devices.select{|dev|
            if(sourceUID.notNil, {
                dev.uid == sourceUID && dev.name == this.class.midiDeviceName && dev.name == port
            }, {
                dev.device == this.class.midiDeviceName && dev.name == port
            })
        }.first
    }

    midiout{
        if(destinationIndex.isNil){
            ("No midi out found at index: " + destinationIndex).error;
            ^nil;
        };

        ^midiouts.at(destinationIndex);
    }

    prResponderKey{|msgType, chan, msgNum|
        ^"%_%_%".format(msgType, chan, msgNum).asSymbol;
    }

    /* Example

    i = IAC.new();

    // Add responder
    i.add({|...msg| ("GOT A NOTE ON!! :" ++ msg).postln}, msgNum: 64, chan: 0, msgType: \noteOn);

    // Send a note to it
    i.midiout.noteOn(0, 64, 120);

    */
    add{|func, msgNum, chan, msgType, permanent=false|
        var key = this.prResponderKey(msgType: msgType, msgNum: msgNum, chan: chan);
        var midifunc = MIDIFunc.new(func: func, msgNum: msgNum, chan: chan, msgType: msgType, srcID: sourceUID).permanent_(permanent);

        callbacks = callbacks ? IdentityDictionary.new;
        callbacks[msgType] = callbacks[msgType] ? IdentityDictionary.new;
        callbacks[msgType][key] = midifunc;
    }

    remove{|msgNum, chan, msgType|
        var key = this.prResponderKey(msgType: msgType, msgNum: msgNum, chan: chan);
        var midifunc = callbacks[msgType][key];

        if(midifunc.notNil, {
            midifunc.free;
            callbacks[msgType][key] = nil;
            callbacks[msgType].remove(key);
        });

    }

    // Send midi to this device
    noteOn{|chan, note, vel|
        this.midiout.noteOn(chan, note, vel);
    }

    noteOff{|chan, note, vel|
        this.midiout.noteOff(chan, note, vel);
    }

    cc{|chan, cc, val|
        this.midiout.control(chan, cc, val);
    }

    // Same as above, just an alias
    control{|chan, cc, val|
        this.midiout.control(chan, cc, val);
    }

    start{
        this.midiout.start;
    }

    stop{
        this.midiout.stop;
    }

    program{|chan, prog|
        this.midiout.program(chan, prog);
    }

    bend{|chan, val|
        this.midiout.bend(chan, val);
    }

}
