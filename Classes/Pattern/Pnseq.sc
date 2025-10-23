// A Pseq with livecodeable items
/*

Example:

p = Pnseq([2,4]);
z = p.asStream;

z.next;
z.next;

// Now change item at second position
p.items.at(1).set(666);

z.next;
z.next;

// Now with a spec to control the output
p = Pnseq([0.25,0.5,0.85], [1.0,100.0,\exp].asSpec);
z = p.asStream;

z.next;
z.next;

// Now use the gui to set values
(
p = Pnseq(Array.fill(16, 0.5), [100.0,500.0,\exp].asSpec);

Pdef(\livecodeFreaq,
    Pbind(
        \freq, p,
        \dur, 0.25,
    )
).play;

p.gui;
)

*/
Pnseq : Pattern{
    var <>items, <>repeats;

    *new{|seq, spec, repeats=inf|
        ^super
        .new()
        .items_(
            seq.collect{|seqItem|
                // FIXME: This is a bit ugly, but Pparam needs an initial source before setting a new one with .map using the spec
                Pparam(0, spec).map(seqItem)
            }
        )
        .repeats_(repeats)
    }

    embedInStream { |inval|
        var repeats = this.repeats ? 1;
        var stream = Pseq(items, repeats).asStream;
        ^stream.embedInStream(inval);
    }

    asStream {
        ^Routine{|inval|
            var stream = this.embedInStream(inval);
            stream.do{|val|
                inval = val.yield;
            }
        }
    }

    gui { |parent, bounds|
        var win, view, spec, snap, vals, n, sliders, onChange;
        spec = items[0].spec ? ControlSpec(0, 1); // fallback if no spec
        snap = spec.step ? 0;
        n = items.size;
        vals = items.collect { |pp| pp.getUnmapped};
        onChange = { |msv|
            msv.value.do { |v, i|
                items[i].map(v);
                // items[i].set(mapped);
            };
        };
        if (parent.notNil) {
            view = MultiSliderView(parent, bounds ? Rect(0,0,400,250));
            view.size_(n);
            view.value_(vals);
            view.elasticMode_(true);
            view.isFilled_(true);
            view.step_(snap);
            view.action_({ onChange.value(view) });
            ^view
        } {
            win = Window("Pnseq GUI", bounds ? Rect(100,100,400,300));
            view = MultiSliderView(win, Rect(10,10,300,250));
            view.size_(n);
            view.value_(vals);
            view.elasticMode_(true);
            view.isFilled_(true);
            view.step_(snap);
            view.action_({ onChange.value(view) });
            win.front;
            ^win
        }
    }
}
