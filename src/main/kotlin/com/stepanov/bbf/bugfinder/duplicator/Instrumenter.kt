package com.stepanov.bbf.bugfinder.duplicator

import org.apache.commons.exec.CommandLine
import org.apache.commons.exec.DefaultExecutor
import org.apache.commons.exec.ExecuteException
import java.io.File

class Instrumenter(val pathToFile: String) {
    private val pathToJacocoAgent = ""
    private val pathToJacocoExec = ""
    private val pathToKotlinCompilerJAR = ""

    fun instrument(): String {
        val file = File(pathToFile)
        val execPath = pathToJacocoExec + file.nameWithoutExtension + ".exec"

        val line = "java -javaagent:$pathToJacocoAgent=destfile=$execPath -jar $pathToKotlinCompilerJAR $pathToFile"
        val cmdLine: CommandLine = CommandLine.parse(line)
        val executor = DefaultExecutor()
        try {
            executor.execute(cmdLine)
        } catch (e: ExecuteException) {
        }
        return execPath
    }
}