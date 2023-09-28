+ String{
    asOSCFunc{
        ^OSCFunc.new(func:{|msg|
            msg.postln;
        }, path:this);
    }
}
