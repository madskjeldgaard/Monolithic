+ Env{
    // A gated fade envelope
    *gatefade{|fadeTime=1, sustainLevel=1|
        ^this.asr(fadeTime, sustainLevel, fadeTime, 'sine')
    }

    *fadein{|fadeTime=1, sustainLevel=1|
        ^this.asr(fadeTime, sustainLevel, 0, 'sine')
    }
}
