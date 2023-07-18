+ EventPatternProxy{
    toggle{
        if(this.isPlaying, {
            this.stop();
        }, {
            this.play();
        })

    }
}
