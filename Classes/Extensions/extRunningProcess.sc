+ String{
    isRunningProcess{
        var checked = ("ps aux | grep -v grep | grep -ci " ++ this).unixCmdGetStdOut;

        ^checked.asInteger.asBoolean;
    }

    isNotRunningProcess{
        ^this.isRunningProcess.not;
    }
}
