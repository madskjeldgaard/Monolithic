/*

Make a gui for a Pctrldef or Pcontrol


EXAMPLE:
(

// Create a Pcontrol
p = Pcontrol.new({ |pc|
    pc.addParam(\degree, 0, [-10,10, \lin, 1].asSpec);
    pc.addParam(\amp, 0.1, [0, 1, \lin].asSpec);
    pc.addParam(\scale, Scale.major, [Scale.minor, Scale.major, Scale.melodicMinor]);

    Pbind(
        \scale, pc[\scale].trace,
        \degree,  pc[\degree].trace,
        \amp, pc.at(\amp).trace,
        \dur, 0.25,
    )
});

// Create and show the GUI
g = p.gui;

)
*/

PcontrolGui {
    classvar <>defaultExcludeParams = #[];
    classvar <>defaultIgnoreParams = #[];

    var <pcontrol;  // instance variable with lowercase p
    var collapseArrays;
    var params, paramViews;

    var prExcludeParams;
    var <>ignoreParams;
    var <window;

    var play, quantBox, quantLabel;
    var header, parameterSection;
    var updateInfoFunc;

    var font, headerFont;

    var pcontrolChangedFunc, specChangedFunc;

    *new { | pcontrol, limitUpdateRate = 0, show = true, collapseArrays = false |
        ^super.newCopyArgs(pcontrol, collapseArrays).init(limitUpdateRate, show)
    }

    init { | limitUpdateRate, show |
        this.initFonts();

        params = IdentityDictionary.new();
        paramViews = IdentityDictionary.new();

        window = Window.new(pcontrol.class.name);
        window.layout = VLayout.new(
            this.makeInfoSection(),
            this.makeTransportSection(),
            // parameterSection gets added here in makeParameterSection
        );

        window.view.children.do{ | c | c.font = if(c == header, headerFont, font) };

        this.setUpDependencies(limitUpdateRate.max(0));

        this.makeParameterSection();

        if(show) {
            window.front;
        }
    }

	asView { ^window.asView }

	setUpDependencies { | limitUpdateRate |
		var limitOrder, limitDict, limitScheduler;
		var specAddedFunc;

		specAddedFunc = { | obj ...args |
			var key, spec;
			if(args[0] == \add, {
				key = args[1][0];
				spec = args[1][1];
				if(params[key].notNil and: { params[key] != spec }, {
					{ this.makeParameterSection() }.defer;
				})
			})
		};

		specChangedFunc = { | obj ...args |
			{ this.makeParameterSection() }.defer;
		};

		pcontrolChangedFunc = if(limitUpdateRate > 0, {
			limitOrder = OrderedIdentitySet.new(8);
			limitDict = IdentityDictionary.new();
			limitScheduler = SkipJack.new({
				if(limitOrder.size > 0, {
					limitOrder.do{ | key | this.pcontrolChanged(*limitDict[key]) };
					limitOrder.clear;
					limitDict.clear;
				});
			}, limitUpdateRate, clock: AppClock);
			{ | obj ...args |
				var key = args[0];
				if(key == \set) {
					args[1].pairsDo { | paramKey, v |
						key = (key ++ paramKey).asSymbol;
						limitOrder.add(key);
						limitDict.put(key, [\set, [paramKey, v]]);
					}
				} {
					limitOrder.add(key);
					limitDict.put(key, args);
				}
			}

		}, {

			{ | obj ...args | { this.pcontrolChanged(*args) }.defer }

		});

		Spec.addDependant(specAddedFunc);
		// TODO: Add dependants to pcontrol if needed
		// pcontrol.addDependant(pcontrolChangedFunc);

		window.onClose = {
			limitScheduler.stop;
			// pcontrol.removeDependant(pcontrolChangedFunc);
			Spec.removeDependant(specAddedFunc);
			params.do{ | spec | spec.removeDependant(specChangedFunc) };
		};
	}

	pcontrolChanged { | what, args |
		var key;

		case
		{ what == \set } {
			key = args[0];
			args.pairsDo { | paramKey, val |
				if(params[paramKey].notNil, {
					this.parameterChanged(paramKey, val)
				})
			}
		}
		{ what == \play } {
			play.value_(1);
		}
		{ what == \source } {
			this.makeParameterSection()
		}
		{ what == \stop } {
			play.value_(0)
		}
		{ what == \quant } {
			quantBox.value_(args[0]);
		}
	}

	parameterChanged { | key, val |
		var spec;

		case
		{ val.isNumber } {
			if(paramViews[key][\type] != \number, { this.makeParameterSection() });
			spec = params[key].value;
			paramViews[key][\numBox].value_(spec.constrain(val));
			paramViews[key][\slider].value_(spec.unmap(val));
		}
		{
			"% parameter '%' not set".format(this.class, key).warn;
		}
	}

	makeInfoSection {
		var quantLabel;

		quantLabel = StaticText.new()
		.string_("quant:");

		quantBox = NumberBox.new()
		.clipLo_(0.0)
		.decimals_(2)
		.scroll_step_(0.1) // mouse
		.step_(0.1)        // keys
		.action_({ | obj |
			pcontrol.quant_(obj.value);
		})
		.value_(pcontrol.patternProxy.quant ? 0);

		updateInfoFunc = { | pc |
			quantBox.value = pc.patternProxy.quant ? 0;
		};
		updateInfoFunc.value(pcontrol);

		if(pcontrol.class.name.notNil, {
			header = StaticText.new().string_(pcontrol.class.name)
		});

		^VLayout.new(
			header,
			HLayout.new(quantLabel, [quantBox, a: \left]),
		)
	}

	makeTransportSection {
		var clear, send, scope, free, popup;

		play = Button.new()
		.states_([
			["play"],
			["stop", Color.black, Color.grey(0.5, 0.5)],
		])
		.action_({ | obj |
			if(obj.value == 1, {
				pcontrol.play
			}, {
				pcontrol.stop
			})
		})
		.value_(pcontrol.isPlaying.binaryValue);

		clear = Button.new()
		.states_(#[
			["clear"]
		])
		.action_({ | obj |
			pcontrol.patternProxy.clear;
		});

		send = Button.new()
		.states_(#[
			["send"]
		])
		.action_({ | obj |
			pcontrol.patternProxy.send;
		});

		scope = Button.new()
		.states_(#[
			["scope"]
		])
		.action_({ | obj |
			pcontrol.patternProxy.scope;
		});

		free = Button.new()
		.states_(#[
			["free"]
		])
		.action_({ | obj |
			pcontrol.patternProxy.free;
		});

		popup = PopUpMenu.new()
		.allowsReselection_(true)
		.items_(#[
			"defaults",
			"randomize parameters",
			"vary parameters",
			"document",
			"post",
		])
		.action_({ | obj |
			switch(obj.value,
				0, { this.defaults() },
				1, { this.randomize() },
				2, { this.vary() },
				3, { pcontrol.patternProxy.document },
				4, { pcontrol.patternProxy.asCode.postln },
			)
		})
		.keyDownAction_({ | obj, char |
			if(char == Char.ret, {
				obj.doAction
			})
		})
		.canFocus_(true)
		.fixedWidth_(25);

		^HLayout.new(
			play, clear, free, scope, send, popup
		)
	}

	makeParameterSection {
		var excluded = defaultExcludeParams ++ prExcludeParams;
		var numParams = params.size;

		params.do{ | spec | spec.removeDependant(specChangedFunc) };
		params.clear;

		pcontrol.params.keysValuesDo{ | key, param |
			var spec = param.spec;
			var val = param.source;

			if(this.paramPresentInArray(key, excluded).not, {
				spec.addDependant(specChangedFunc);
				params.put(key, spec);
			});
		};

		if(parameterSection.notNil, { parameterSection.remove });
		parameterSection = this.makeParameterViews().resizeToHint;
		if(parameterSection.bounds.height > (Window.availableBounds.height * 0.5), {
			parameterSection = ScrollView.new().canvas_(parameterSection);
		});
		window.layout.add(parameterSection, 1);
		if(numParams != params.size, {
			{ window.view.resizeToHint }.defer(0.07);
		});
	}

    makeParameterViews {
        var view;

        view = View.new().layout_(VLayout.new());

        paramViews.clear;
        params.sortedKeysValuesDo{ | key, spec |
            var layout, paramVal, valueBox;
            var slider, valueDisplay;
            var minval, maxval;

            paramVal = pcontrol.params[key].source;

            layout = HLayout.new(
                [StaticText.new().string_(key), s: 1]
            );

            case
            // For ArrayedSpec (discrete choices)
            { spec.class == ArrayedSpec } {
                var arrayChoices = spec.array;
                var currentIndex = arrayChoices.indexOf(paramVal) ? 0;
                var numChoices = arrayChoices.size;

                valueDisplay = StaticText.new()
                .string_(arrayChoices[currentIndex].asString);

                // slider = Slider.new()
                // .orientation_(\horizontal)
                // .value_(currentIndex / (numChoices - 1).max(0))
                // .step_(1/(numChoices-1))
                // .action_({ |sl|
                //     var index = (sl.value * (numChoices - 1)).round(1).asInteger;
                //     var val = arrayChoices[index];
                //     valueDisplay.string_(val.asString);
                //     pcontrol.setRawOne(key, val);
                // });

                slider = ListView()
                .items_(arrayChoices.collect{ | item | item.asString })
                .value_(currentIndex)
                .action_({ | lv |
                    var index = lv.value;
                    var val = arrayChoices[index];
                    // valueDisplay.string_(val.asString);
                    pcontrol.setRawOne(key, val);
                });

                paramViews.put(key, (type: \array, slider: slider, display: valueDisplay));

                // layout.add(valueDisplay, 1);
                layout.add(slider, 4);
            }
            // For regular number parameters
            { paramVal.isNumber } {
                // Get min and max values from the spec
                minval = if(spec.respondsTo(\minval)) { spec.minval } { 0 };
                maxval = if(spec.respondsTo(\maxval)) { spec.maxval } { 1 };

                slider = Slider.new()
                .orientation_(\horizontal)
                .value_(
                    if(spec.respondsTo(\unmap)) {
                        spec.unmap(paramVal)
                    } {
                        // Fallback linear mapping
                        (paramVal - minval) / (maxval - minval)
                    }
                )
                .action_({ | obj |
                    var val = if(spec.respondsTo(\map)) {
                        spec.map(obj.value)
                    } {
                        // Fallback linear mapping
                        minval + (obj.value * (maxval - minval))
                    };
                    valueBox.value = val;
                    pcontrol.setRawOne(key, val);
                });

                valueBox = NumberBox.new()
                .action_({ | obj |
                    var val = if(spec.respondsTo(\constrain)) {
                        spec.constrain(obj.value)
                    } {
                        obj.value.clip(minval, maxval)
                    };
                    slider.value_(
                        if(spec.respondsTo(\unmap)) {
                            spec.unmap(val)
                        } {
                            // Fallback linear mapping
                            (val - minval) / (maxval - minval)
                        }
                    );
                    pcontrol.setRawOne(key, val);
                })
                .decimals_(4)
                .value_(
                    if(spec.respondsTo(\constrain)) {
                        spec.constrain(paramVal)
                    } {
                        paramVal.clip(minval, maxval)
                    }
                );

                paramViews.put(key, (type: \number, slider: slider, numBox: valueBox));

                layout.add(valueBox, 1);
                layout.add(slider, 4);
            }
            {
                "% parameter '%' ignored".format(this.class, key).warn;
            };

            view.layout.add(layout)
        };

        view.children.do{ | c | c.font = font };

        ^view
    }

	initFonts {
		var fontSize, headerFontSize;

		fontSize = 14;
		headerFontSize = fontSize * 2;

		headerFont = Font.sansSerif(headerFontSize, bold: true, italic: false);
		font = Font.monospace(fontSize, bold: false, italic: false);
	}

	randomize { | randmin = 0.0, randmax = 1.0 |
		this.filteredParamsDo{ | val, spec |
			spec.map(rrand(randmin, randmax))
		}
	}

	vary { | deviation = 0.1 |
		this.filteredParamsDo{ | val, spec |
			spec.map((spec.unmap(val) + 0.0.gauss(deviation)).clip(0, 1))
		}
	}

	excludeParams { ^prExcludeParams }

	excludeParams_ {| value |
		prExcludeParams = value;
		{ this.makeParameterSection() }.defer;
	}

	defaults {
		this.filteredParamsDo{ | val, spec |
			spec.default
		}
	}

	close {
		^window.close()
	}

	filteredParamsDo { | func |
		this.filteredParams.keysValuesDo{ | key, spec |
			var val = pcontrol.params[key].source;
			val = func.value(val, spec);
			pcontrol.setRawOne(key, val);
		}
	}

	filteredParams {
		var accepted = IdentityDictionary.new;
		var ignored;

		ignored = defaultIgnoreParams ++ ignoreParams ++ defaultExcludeParams ++ prExcludeParams;

		pcontrol.params.keysValuesDo({ | key, param |
			var spec = param.spec;
			var val = param.source;

			if(this.paramPresentInArray(key, ignored).not, {
				if(val.isNumber, {
					accepted.put(key, spec)
				})
			})
		});

		^accepted
	}

	paramPresentInArray { | key, array |
		^array.any{ | param |
			if(param.isString and: { param.indexOf($*).notNil }, {
				param.replace("*", ".*").addFirst($^).add($$).matchRegexp(key.asString)
			}, {
				param.asSymbol == key
			})
		}
	}
}
