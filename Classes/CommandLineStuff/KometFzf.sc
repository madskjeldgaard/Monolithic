/*

*/

FZFdef : Singleton {
    var <finderName, callbackFunction;

    set{|itemsCode, callbackFunc|
        this.setFinder(itemsCode, callbackFunc)
    }

    prCallNeoVim{|call|
        // Wrap like this to avoid problems when not using SCNvim
        if(\SCNvim.asClass.notNil, {
            \SCNvim.asClass.luaeval(call)
        });
    }

    // Retrieve the finder in lua code
    getFinder{
        ^"require'fzf-sc/finders'." ++ this.finderName
    }

    // TODO: This is a massive work in progress
    callFinder{
        var call = "vim.cmd[[" ++ "FzfSC " ++ this.finderName ++ "]]";
        call.postln;
        this.prCallNeoVim(call);
    }

    callback{|result|
        ^callbackFunction.value(result)
    }

    // TODO allow lua callback
    setFinder{|itemsCode, callbackFunc|
        var responderFunc, luacode;
        finderName = this.name;

        callbackFunction = callbackFunc;

        responderFunc = this.class.name ++ "(" ++ "\\" ++ this.name ++ ").callback";
        luacode = "print('adding fzf-sc finder %');"
        ++ "% = function() local sc_code = [[%]];"
        ++ "local supercollider_callback = [[%('\\%s');]];"
        ++ "require'fzf-sc/utils'.fzf_sc_eval(sc_code, supercollider_callback);"
        ++ "end";

        luacode = luacode.format(
            this.finderName,
            this.getFinder(),
            itemsCode,
            responderFunc
        );

        this.prCallNeoVim(luacode.postln);
    }
}
