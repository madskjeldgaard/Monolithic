SimpleSynthBrowser {
    classvar window, argText, synthList;

    *new{
        var synthNames = SynthDescLib.global.synthDescs.keys.asArray.sort;

        var font = Font.default.size_(14);

        window = Window.new(this.name);

        argText = TextView.new(window);

        synthList = ListView.new(parent:window)
        .items_(synthNames)
        .action_({|obj|
            var index = obj.value;
            var val = synthNames[index];
            var text = this.argArrayStringFor(val);

            argText.string = text;
        })
        ;

        window.layout = VLayout.new(
            StaticText.new(window).string_("Synth:").font_(font.bold_(true)),
            synthList,
            StaticText.new(window).string_("ArgArray:").font_(font.bold_(true)),
            argText
        );

        window.front;
    }

    *argArrayStringFor{|synthName|
        var synthDesc = SynthDescLib.global.synthDescs.at(synthName.asSymbol);
        var controlNames = synthDesc.controls;

        var outString = "";
        controlNames.do{|ctrl, index|
            var name = ctrl.name;
            var default = ctrl.defaultValue;
            var break = if(index != (controlNames.size - 1), { "\n" }, { "" });

            outString = outString ++ "%, %,%".format("\\" ++ name, default, break);
        };

        ^outString
    }

}
