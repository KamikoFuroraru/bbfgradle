package com.stepanov.bbf.bugfinder.duplicator

import com.stepanov.bbf.bugfinder.SingleFileBugFinder
import org.apache.log4j.PropertyConfigurator
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

fun main(args: Array<String>) {

    PropertyConfigurator.configure("src/main/resources/bbfLog4j.properties")
    PropertyConfigurator.configure("src/main/resources/reduktorLog4j.properties")

    val file = File("") // add filename to CoverageComparator
    val fileCoverage = ""
    if (!File(fileCoverage).exists()) {
        val fileExec = Instrumenter(file.absolutePath).instrument()
        CoverageReportMaker(fileCoverage, fileExec).createReport()
    }

    SingleFileBugFinder(file.absolutePath).findBugsInFile()

    //println(CoverageComparator().computeSimilarity("/home/vera/Downloads/bugs/coverage/pass"))
/*
    val array = CoverageComparator().computeSimilarity("/home/vera/Downloads/bugs/coverage/pass").split("\n")
    for (i in array.indices) {
        if (array[i].split(" ")[3].toDouble() < 0.817) {
            //println("/home/vera/Downloads/bugs/coverage/pass/${array[i].split((" "))[1]}.csv")
            Files.delete(Paths.get("/home/vera/Downloads/bugs/coverage/pass/${array[i].split((" "))[1]}.csv"))
            Files.delete(Paths.get("/home/vera/Downloads/bugs/mutant/pass/${array[i].split((" "))[1]}.kt"))
        }
    }
 */

    //CoverageComparator().computeSuspiciousValue("/home/vera/Downloads/bugs/coverage/pass", "/home/vera/Downloads/bugs/coverage/mutant8300903189029575255.csv", "/home/vera/Downloads/finding/test2/rating-test2-dup8300.txt")
    //CoverageComparator().compareSus("/home/vera/Downloads/finding/test2/rating-test2.txt", "/home/vera/Downloads/finding/test2/rating-test2-dup8300.txt")
}

