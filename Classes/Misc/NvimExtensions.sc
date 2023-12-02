/*

Map keys in NeoVim to SuperCollider code.

Needs SCNvim to be installed and running.

Example:

~f6Func = { "hejhej".postln };
NvimMap.mapSC("n", "<F6>", "~f6Func.value()");


*/

NvimMap : Nvim {

    // Maps a key in scnvim
    *map{|mode, lhs, rhs|
        var luacode = format(
            "vim.keymap.set('%', '%', %, {silent = true})",
            mode, lhs, rhs
        );

        this.luaeval(luacode);
    }

    // Same as above, but take a piece of supercollider code as a string and map that
    *mapSC{|mode="n", lhs, scCodeString|
        var rhs;
        var luaFunc = "require \"scnvim\".send([[" ++ scCodeString ++ "]])";

        rhs = format("'<cmd>lua %<cr>'", luaFunc);

        this.map(mode, lhs, rhs);
    }

}

NvimNotify : Nvim{

    *notify{|msg, loglevel=2|
        var luacode = format(
            "vim.notify('%', %)",
            msg, loglevel
        );

        this.luaeval(luacode);
    }

}

NvimOpen : Nvim {

    *openTab{|path|
        var luacode = format(
            "vim.cmd('tabedit %')",
            path
        );

        this.luaeval(luacode);

    }
}
