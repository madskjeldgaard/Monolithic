/*

Map keys in NeoVim to SuperCollider code.

Needs SCNvim to be installed and running.

Example:

NvimMap.mapSC("n", "<F9>", "\"hejhej\".postln");

*/
NvimMap {
    *luaeval{|code|
        if(\SCNvim.asClass.notNil, {
            \SCNvim.asClass.luaeval(code);
        })
    }

    // Maps a key in scnvim
    *map{|mode, lhs, rhs|
        var luacode = format(
            "vim.keymap.set('%', '%', %, {silent = true})",
            mode, lhs, rhs
        ).postln;

        this.luaeval(luacode);
    }

    // Same as above, but take a piece of supercollider code as a string and map that
    *mapSC{|mode="n", lhs, scCodeString|
        var rhs;
        var luaFunc = "require \"scnvim\".send([[" ++ scCodeString ++ "]])";

        rhs = format("'<cmd>lua %<cr>'", luaFunc);

        this.map(mode, lhs, rhs.postln);
    }

}

NvimNotify{
    *luaeval{|code|
        if(\SCNvim.asClass.notNil, {
            \SCNvim.asClass.luaeval(code);
        })
    }

    *notify{|msg, loglevel=3|
        var luacode = format(
            "vim.notify('%', %)",
            msg, loglevel
        ).postln;

        this.luaeval(luacode);
    }

}
