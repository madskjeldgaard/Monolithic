+ Buffer{
    asLoopingSamplePlayer{|fadeTime=0, out=0, amp=0.125, playrate=1|
        ^LoopingSamplePlayer.new(buffer: this, fadeTime: fadeTime, out: out, amp: amp, playrate: playrate)
    }
}
