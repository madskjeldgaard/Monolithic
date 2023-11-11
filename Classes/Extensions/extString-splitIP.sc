+ String{
    splitIP{
        var ipString = this;
        var splitAt = ".";
        var splits = ipString.findAll(splitAt) ++ [ipString.size];
        var lastSplit = 0;
        ^splits.collect{|splitTo, splitNum|
            var splitFrom = lastSplit;
            var result;

            result = ipString[splitFrom..splitTo].asInteger;

            lastSplit = splitTo + 1;

            result
        }
    }
}
