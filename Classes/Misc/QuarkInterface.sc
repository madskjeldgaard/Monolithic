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

    //------------------------------------------------------------------//
    //                 Manage sub directories of quark                  //
    //------------------------------------------------------------------//

    *filesInSubDir{|subdir|
        var path = this.pathName() +/+ subdir;

        ^path.files

    }

    *subdirFile{|subdir, filename|
        var path = this.pathName() +/+ subdir;
        var filepath = path +/+ filename;

        ^filepath.isFile.not.if({
            ("File not found: " + filepath.fullPath).error
        }, {
            filepath.fullPath
        });
    }

    *subdirFileLoad{|subdir, filename|
        ^this.subdirFile(subdir, filename).load
    }

    *loadFilesInSubDir{|subdir|
        var files = this.filesInSubDir(subdir);

        ^files.collect{|file|
            file.fullPath.load
        }
    }
}
