/*
// Example:

(
var specKey = \freq;
var spec = Spec.specs[specKey];
var window = Window.new();
var slider = spec.asSpecSlider(parent: w, name: specKey).action_({|val| [specKey, val].postln});

window.layout = VLayout();
slider.addToLayout(window.layout);

window.front();
)

*/

SpecSlider{
    var >action, <key, <controlSpec, <nameLabel, <slider, <label, numberBox, <layout;

    *new{|parent, spec, name|
        ^super.new.init(parent, spec, name)
    }

    value{|val|
        slider.value_(val)
    }

    valueAction{|val|
        slider.valueAction_(val)
    }

    init{|parent, spec, name|
        controlSpec = spec;
        key = name;

        nameLabel = key.notNil.if({
            StaticText.new(parent).string_(key.asString)
        });

        slider = Slider.new(parent)
        .value_(spec.unmap(spec.default))
        .orientation_(\horizontal)
        .action_({|obj|
            var val = obj.value;
            var mapped = spec.map(val);

            numberBox.value_(mapped);

            action.value(mapped);
        });

        numberBox = NumberBox.new(parent)
        .value_(spec.default)
        .action_({|obj|
            var val = obj.value;
            var unmapped = spec.unmap(val);
            slider.valueAction_(unmapped)
        });

        label = StaticText.new(parent)
        .string_(spec.units);

        layout = HLayout([nameLabel, s: 2], [ slider, s: 6], [ numberBox, s: 2], [label, s: 1]);

        ^this

    }

    addToLayout{|inLayout|
        inLayout.add(layout);

        ^inLayout;
    }

}

+ControlSpec{
    asSpecSlider{|parent, name|
        ^SpecSlider.new(parent: parent, spec: this, name: name)
    }
}
