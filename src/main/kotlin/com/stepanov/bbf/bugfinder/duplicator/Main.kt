package com.stepanov.bbf.bugfinder.duplicator

import com.stepanov.bbf.bugfinder.SingleFileBugFinder
import org.apache.commons.io.FilenameUtils
import org.apache.log4j.PropertyConfigurator
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

fun main(args: Array<String>) {
    PropertyConfigurator.configure("src/main/resources/bbfLog4j.properties")
    PropertyConfigurator.configure("src/main/resources/reduktorLog4j.properties")

    val file = File("/home/vera/Downloads/dup_test/3/sraoo.kt")

    val path = FilenameUtils.getFullPath(file.absolutePath)
    val dupPath = "$path${file.nameWithoutExtension}"
    val mutantPath = "$dupPath/mutant"
    val coveragePath = "$dupPath/coverage"
    val fileCoverage = "$dupPath/${file.nameWithoutExtension}.csv"
/*
    if (!File(mutantPath).exists()) File(mutantPath).mkdirs()
    if (!File(coveragePath).exists()) File(coveragePath).mkdir()
    if (!File("$dupPath/similarity-list-pass.txt").exists()) File("$dupPath/similarity-list-pass.txt").createNewFile()
    if (!File(fileCoverage).exists()) {
        val fileExec = Instrumenter(file.absolutePath).instrument()
        CoverageReportMaker(fileCoverage, fileExec).createReport()
    }

    SingleFileBugFinder(file.absolutePath).findBugsInFile()
*/
    //println(CoverageComparator().computeSimilarity(coveragePath, fileCoverage))
/*
    val array = CoverageComparator().computeSimilarity(coveragePath, fileCoverage).split("\n")
    for (i in array.indices) {
        if (array[i].split(" ")[3].toDouble() < 0.8) {
            //println("$coveragePath/${array[i].split((" "))[1]}.csv")
            Files.delete(Paths.get("$coveragePath/${array[i].split((" "))[1]}.csv"))
            Files.delete(Paths.get("$mutantPath/${array[i].split((" "))[1]}.kt"))
        }
    }
*/
    //CoverageComparator().computeSuspiciousValue(coveragePath, fileCoverage, "$path/SUS_${file.nameWithoutExtension}.txt")
    CoverageComparator().compareSus("$path/SUS_BACKEND_sxnlqpu_FILE.txt", "/home/vera/Downloads/dup_test/3/SUS_BACKEND_uqsajys_FILE.txt")

}

