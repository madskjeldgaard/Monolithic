// Contains convenience functions for quark main classes
// Some stuff I am sick of writing again and again
QuarkInterface{
    classvar <packageName;

    *allClasses{
        ^Quarks.classesInPackage(this.packageName)
    }

    *pathName{
        ^if(this.packageName().notNil, {
            PathName(Main.packages.asDict[this.packageName()])
        }, {
            "packageName not defined!".error
        })
    }

    *filesInSubDir{|subdir|
        var path = this.pathName() +/+ subdir;

        ^path.files

    }

    *loadFilesInSubDir{|subdir|
        var files = this.filesInSubDir(subdir);

        ^files.collect{|file|
            file.fullPath.load
        }
    }
}
