Test_Pparam : MonolithicTest {
    var param;

    setUp {
        param = Pparam.new(10, controlspec: [0,10]);
    }

    tearDown {
        param = nil;
    }

    test_copypparam {
        var copy = param.copy();

        // Check if envir is the same
        this.assert(copy.envir == param.envir, "After copying, envir should be the same");

        // Check if pattern is the same
        this.assert(copy.pattern == param.pattern, "After copying, pattern should be the same");

        // Change source
        param.source = 5;
        this.assert(copy.source != param.source, "After changing source, copy should not be equal to original");

        // Change spec
        param.spec = [0,5];
        this.assert(copy.spec != param.spec, "After changing spec, copy should not be equal to original");

        // And finally check that they aren't equal
        this.assert(copy != param, "After changing source and spec, copy should not be equal to original");
    }
}

Test_Pctrldef : UnitTest {
    var ctrldef;

    setUp {
    }

    tearDown {
        Pctrldef.all[ctrldef.key] = nil;
    }

    test_newctrldef{
        ctrldef = Pctrldef.new(\testctrl, {|ctrl| Pbind() });
        this.assert(ctrldef.key == \testctrl, "Key should be set correctly");
        this.assert(ctrldef.patternProxy.isKindOf(Pattern), "Contains a pattern");
        this.assert(Pctrldef.all[ctrldef.key] == ctrldef, "Should be added to Pctrldef.all");
    }

    test_copyctrldef{
        var ctrldef2;
        ctrldef = Pctrldef.new(\testctrl, {|ctrl| Pbind(\hej, ctrl[\hej], \ho, ctrl[\ho]) });

        // Add params
        ctrldef.addParam(
            \hej, 0.5, \uni,
            \ho, 7, \octave
        );

        ctrldef2 = ctrldef.copy(\testctrl2);

        // Iterate over all params in origin and check if the target has the same param
        ctrldef.params.keysValuesDo{|paramName, param|
            var copiedParam = ctrldef2.params[paramName];
            this.assert(param.source == copiedParam.source, "Copied param % should have the same source".format(paramName));
            this.assert(param.spec == copiedParam.spec, "Copied param % should have the same spec".format(paramName));
            this.assert(param.envir == copiedParam.envir, "Copied param % should have the same envir".format(paramName));
            this.assert(param.pattern == copiedParam.pattern, "Copied param % should have the same pattern".format(paramName));
        };

        this.assert(ctrldef.key == \testctrl, "Origin Key should be the same");
        this.assert(ctrldef.patternProxy.isKindOf(Pattern), "Origin should still contain a pattern");

        this.assert(ctrldef2.key == \testctrl2, "Copy Key should be set correctly");
        this.assert(ctrldef2.patternProxy.isKindOf(Pattern), "Copy should contain a pattern");

        this.assert(ctrldef.patternProxy != ctrldef2.patternProxy, "Origin and copy should not have the same pattern proxy");
        this.assert(ctrldef.patternProxy.source == ctrldef2.patternProxy.source, "Origin and copy should have the same pattern source");

        this.assert(Pctrldef.all[ctrldef.key] == ctrldef, "Origin should still be added to Pctrldef.all");
        this.assert(Pctrldef.all[ctrldef2.key] == ctrldef2, "Copy should be added to Pctrldef.all");

        // Change params and check again
        ctrldef.params.do{|param|
            param.map(rrand(0.0,1.0))
        };

        // Check parameters again
        ctrldef.params.keysValuesDo{|paramName, param|
            var copiedParam = ctrldef2.params[paramName];

            this.assert(param.source != copiedParam.source, "Copied param % should not have the same source".format(paramName));
        };

        // The two patterns' sources should be different now
        this.assert(ctrldef.patternProxy.source != ctrldef2.patternProxy.source, "Origin and copy should not have the same pattern source");

        this.assert(ctrldef != ctrldef2, "Origin and copy should not be equal");
    }

    test_paramMapping{
        ctrldef = Pctrldef.new(\testctrl, {|ctrl| Pbind() });

        // Add params
        ctrldef.addParam(
            \hej, 0.5, \uni,
        );

        ctrldef.map(\hej, 0.0);

        this.assert(ctrldef.params[\hej].source == 0.0, "Param source should be 0.0");

        ctrldef.map(\hej, 1.0);

        this.assert(ctrldef.params[\hej].source == 1.0, "Param source should be 1.0");

        ctrldef.map(\hej, 2.5);

        this.assert(ctrldef.params[\hej].source == 1.0, "Param source should clip");

        ctrldef.map(\hej, -1.0);

        this.assert(ctrldef.params[\hej].source == 0.0, "Param source should clip");

    }

    // Test with multiple params
    test_paramMappingMulti{
        ctrldef = Pctrldef.new(\testctrl, {|ctrl| Pbind() });

    }

}
