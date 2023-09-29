+ String{
    asOSCFunc{|func|
        func = func ? { |msg| msg.postln };
        ^OSCFunc.new(func:func, path:this);
    }
}
