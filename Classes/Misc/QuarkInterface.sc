// Contains convenience functions for quark main classes
// Some stuff I am sick of writing again and again
QuarkInterface{

    *packageName{
        ^this.subclassResponsibility(thisMethod);
    }

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

    //------------------------------------------------------------------//
    //                            Help files                            //
    //------------------------------------------------------------------//

    *generateUndocumentedHelpFiles{
        var quarkInterface = this;

        // FIXME: This is super slow
        var allUndocumented = SCDoc.documents.select{|doc|
            doc.isUndocumentedClass()
        };

        // Select only ones in this quark/package
        allUndocumented = allUndocumented.select{|doc|
            var thisClass = doc.klass;
            quarkInterface.allClasses.indexOfEqual(thisClass).notNil;
        };

        // Generate help files
        allUndocumented.do{|doc|
            var path = this.pathName +/+ "HelpSource" +/+ "Classes";
            var fullPathOutDir = path.fullPath;
            var fullPathFile = (path +/+ doc.klass).fullPath ++ ".schelp";
            var text, file;
            "Generating help file for %".format(doc.klass).postln;
            "Path: %".format(fullPathOutDir).postln;
            "File: %".format(fullPathFile).postln;

            if(path.isFolder.not, {File.mkdir(fullPathOutDir)});

            doc = SCDocEntry.newUndocClass(doc.klass);
            text = SCDoc.makeClassTemplate(doc);
            file = File.open(pathName:fullPathFile, mode:"w");
            file.write(text);
            file.close;
        };

    }
}
