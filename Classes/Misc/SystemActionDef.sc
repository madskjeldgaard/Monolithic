// Define a CmdPeriod once and only once, keep track in global variable
/*

CmdPeriodDef(\hej, {"yoyoyo222".postln})
CmdPeriodDef(\hej, {"yiyiiyiy".postln})
CmdPeriodDef.all;
CmdPeriodDef(\hej).free

*/
CmdPeriodDef {
    var <name, <object;
    classvar <all;

    *initClass{
        all = all ?? {IdentityDictionary.new};
    }

    *new{|name, object|
        var singleton;

        if(all[name].notNil, {
            all[name].free;
        });

        ^super.newCopyArgs(name, object).init
    }

    init{
        CmdPeriod.add(object);
        all.put(name, this);
    }


    removeObject{
        CmdPeriod.remove(object);
    }

    free{
        this.removeObject();
        all.removeAt(name);
    }
}
