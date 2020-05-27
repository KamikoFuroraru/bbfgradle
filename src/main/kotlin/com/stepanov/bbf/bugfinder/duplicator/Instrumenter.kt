package com.stepanov.bbf.bugfinder.duplicator

import org.apache.commons.exec.CommandLine
import org.apache.commons.exec.DefaultExecutor
import org.apache.commons.exec.ExecuteException
import java.io.File

class Instrumenter(val pathToFile: String) {
    private val pathToJacocoAgent = "/home/vera/Downloads/jacoco-0.8.5/lib/jacocoagent.jar"
    private val pathToJacocoExec = "/home/vera/Downloads/bugs/"
    private val pathToKotlinCompilerJAR = "/home/vera/Downloads/kotlin-build-1.3.72-release-483/dist/kotlinc/lib/kotlin-compiler.jar"

    fun instrument(): String {
        val file = File(pathToFile)
        val execPath = pathToJacocoExec + file.nameWithoutExtension + ".exec"

        val line = if (pathToFile.split("/").last().split("_").first() == "BACKEND")
            "java -javaagent:$pathToJacocoAgent=destfile=$execPath -jar $pathToKotlinCompilerJAR -Xnew-inference $pathToFile"
        else "java -javaagent:$pathToJacocoAgent=destfile=$execPath -jar $pathToKotlinCompilerJAR $pathToFile"
        val cmdLine: CommandLine = CommandLine.parse(line)
        val executor = DefaultExecutor()
        try {
            executor.execute(cmdLine)
        } catch (e: ExecuteException) {
        }
        return execPath
    }
}