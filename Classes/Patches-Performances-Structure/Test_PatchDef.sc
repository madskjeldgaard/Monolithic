Test_PatchDef : MonolithicTest {
    var patchdef;
    setUp {
        patchdef = PatchDef.new(\i, configFunc: {|data| }, stopFunc: {|data| }, playFunc: {|data| }, cleanupFunc: {|data| });
    }

    tearDown {
        var key = patchdef.key;
        patchdef.stop;
        patchdef.clear;
        // PatchDef.at(key) = nil;
    }

    test_copyData{
        patchdef.data['hej'] = 15812;
        patchdef.copy(\i2);

        this.assert(patchdef.data == PatchDef(\i2).data, "Copying data from one PatchDef to another works");

        patchdef.data['hej'] = 20812;
        this.assert(patchdef.data != PatchDef(\i2).data, "Changing data in one PatchDef does not affect the other");

        // After copy, the functions are the same between the two
        // patchdefs, but the data is different
        this.assert(patchdef.configFunc == PatchDef(\i2).configFunc, "Copying a PatchDef copies the configFunc");
        this.assert(patchdef.stopFunc == PatchDef(\i2).stopFunc, "Copying a PatchDef copies the stopFunc");
        this.assert(patchdef.playFunc == PatchDef(\i2).playFunc, "Copying a PatchDef copies the playFunc");
        this.assert(patchdef.cleanupFunc == PatchDef(\i2).cleanupFunc, "Copying a PatchDef copies the cleanupFunc");

        // Modify the functions in one PatchDef, and make sure it does not affect the other
        patchdef.configFunc = {|data| data['hej'] = 1234 };
        patchdef.stopFunc = {|data| data['hej'] = 1234 };
        patchdef.playFunc = {|data| data['hej'] = 1234 };
        patchdef.cleanupFunc = {|data| data['hej'] = 1234 };

        this.assert(patchdef.configFunc != PatchDef(\i2).configFunc, "Changing configFunc in one PatchDef does not affect the other");
        this.assert(patchdef.stopFunc != PatchDef(\i2).stopFunc, "Changing stopFunc in one PatchDef does not affect the other");
        this.assert(patchdef.playFunc != PatchDef(\i2).playFunc, "Changing playFunc in one PatchDef does not affect the other");
        this.assert(patchdef.cleanupFunc != PatchDef(\i2).cleanupFunc, "Changing cleanupFunc in one PatchDef does not affect the other");

    }
}
