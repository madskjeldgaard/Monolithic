+ Buffer{
    asOneShotSamplePlayer{|fadeTime=0, out=0, amp=1, playrate=1|
        ^OneShotSamplePlayer.new(buffer: this, fadeTime: fadeTime, out: out, amp: amp, playrate: playrate)
    }
}
