Monolithic : QuarkInterface {
    *packageName{
        ^'Monolithic'
    }

    *runTests{
        MonolithicTest.allSubclasses.do{|testclass|
            testclass.run
        }
    }
}
