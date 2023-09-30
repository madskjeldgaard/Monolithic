+Symbol{
    synthDescExists{|synthdesclib|
        synthdesclib = synthdesclib ? SynthDescLib.global;
        ^synthdesclib.at(this).notNil
    }

    synthdefBinaryExists{
        var synthdefname = this;
        var synthdeffilesOnDisk = PathName(Platform.userAppSupportDir +/+ "synthdefs").files;

        ^synthdeffilesOnDisk.select({|file| file.fileNameWithoutExtension.asSymbol == synthdefname}).first.notNil
    }
}
