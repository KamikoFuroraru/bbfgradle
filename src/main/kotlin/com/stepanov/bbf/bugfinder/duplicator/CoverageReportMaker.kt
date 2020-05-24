package com.stepanov.bbf.bugfinder.duplicator

import org.jacoco.core.analysis.Analyzer
import org.jacoco.core.analysis.CoverageBuilder
import org.jacoco.core.analysis.ICounter
import org.jacoco.core.tools.ExecFileLoader
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

/*
    This code executes what is executed by the command:
    "java -jar $pathToJacocoCli report $pathToJacocoExec --csv $pathToCoverageReport --classfiles $pathToKotlinCompilerJAR"

    But for our purposes, the standard coverage option with --xml or -csv are not suitable,
    because it does not show which specific lines we visited or are inconvenient for further work.
    Therefore, here the coverage is collected in a form convenient for further work, like:
    PACKAGE,CLASS,LINES_COVERED_COUNT,LINES_COVERED
    org.jetbrains.kotlin.psi.stubs.elements,KtImportDirectiveElementType,2,34;35

    Here we must specify the path to the:
    1) $pathToKotlinCompilerJAR, which we obtained by execute "dist" command from kotlin language project
    2) $pathToJacocoExec, which we obtained by using --javaagent option
    3) $pathToCoverageReport, the path to the resultant coverage we were looking for
*/

class CoverageReportMaker(val pathToCoverageReport: String, val pathToJacocoExec: String) {

    private val pathToKotlinCompilerJAR = ""

    fun createReport() {
        val executionDataFile = File(pathToJacocoExec)
        val classesDirectory = File(pathToKotlinCompilerJAR)
        val execFileLoader = ExecFileLoader()
        execFileLoader.load(executionDataFile)
        val coverageBuilder = CoverageBuilder()
        val analyzer = Analyzer(execFileLoader.executionDataStore, coverageBuilder)
        analyzer.analyzeAll(classesDirectory)
        val writer = BufferedWriter(FileWriter(pathToCoverageReport))
        var joiner = StringJoiner(";")
        writer.write("PACKAGE,CLASS,LINES_COVERED_COUNT,LINES_COVERED\n")
        for (cc in coverageBuilder.classes) {
            writer.write(cc.packageName.replace("/", ".") + ",")
            writer.write(cc.name.replace(cc.packageName + "/", "") + ",")
            writer.write(cc.lineCounter.coveredCount.toString())
            writer.write(",")
            if (cc.lineCounter.coveredCount == 0) {
                writer.write("\n")
                continue
            }
            for (i in cc.firstLine..cc.lastLine) {
                if (cc.getLine(i).status == ICounter.FULLY_COVERED) {
                    joiner.add(i.toString())
                }
            }
            writer.write(joiner.toString())
            writer.write("\n")
            joiner = StringJoiner(";")
        }
        writer.close()
        Files.delete(Paths.get(pathToJacocoExec))
    }
}