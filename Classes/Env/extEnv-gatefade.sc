+ Env{
    // A gated fade envelope
    *gatefade{|fadeTime=1, sustainLevel=1|
        ^this.asr(fadeTime, sustainLevel, fadeTime, 'sine')
    }

    *fadein{|fadeTime=1, sustainLevel=1, fadeOutTime=0.01|
        ^this.asr(fadeTime, sustainLevel, fadeOutTime, 'sine')
    }

    // Same as above
    *fadeinout{|fadeInTime=1, fadeOutTime=1, sustainLevel=1|
        ^this.asr(fadeInTime, sustainLevel, fadeOutTime, 'sine')
    }
}
