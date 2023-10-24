IAC : MIDIDeviceInterface {

    *new{|deviceport="Bus 1"|
        var endpoint = MIDIDeviceInterface.getMIDIEndpoint(this.midiDeviceName, deviceport);
        var source = MIDIDeviceInterface.getMIDISource(this.midiDeviceName, deviceport);
        ^super.new(deviceport, source.uid, endpoint.uid)
    }

    *midiDeviceName{
        ^"IAC Driver"
    }

}
