package com.stepanov.bbf.bugfinder.executor.compilers

import com.stepanov.bbf.bugfinder.duplicator.CoverageComparator
import com.stepanov.bbf.bugfinder.duplicator.CoverageReportMaker
import com.stepanov.bbf.bugfinder.duplicator.Instrumenter
import com.stepanov.bbf.bugfinder.executor.CommonCompiler
import com.stepanov.bbf.bugfinder.executor.CompilerArgs
import com.stepanov.bbf.bugfinder.executor.CompilingResult
import com.stepanov.bbf.bugfinder.util.Stream
import com.stepanov.bbf.bugfinder.util.copyFullJarImpl
import com.stepanov.bbf.bugfinder.util.copyJarImpl
import com.stepanov.bbf.bugfinder.util.writeRuntimeToJar
import com.stepanov.bbf.reduktor.executor.KotlincInvokeStatus
import com.stepanov.bbf.reduktor.util.MsgCollector
import org.apache.commons.io.FileUtils
import org.apache.log4j.Logger
import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageLocation
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.config.IncrementalCompilation
import org.jetbrains.kotlin.config.Services
import org.jetbrains.kotlin.utils.PathUtil
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.jar.JarInputStream
import java.util.jar.JarOutputStream

open class JVMCompiler(open val arguments: String = "") : CommonCompiler() {
    override val compilerInfo: String
        get() = "JVM $arguments"

    override val pathToCompiled: String
        get() = "tmp/tmp.jar"


    override fun checkCompiling(pathToFile: String): Boolean {
        val status = tryToCompile(pathToFile)
        return !MsgCollector.hasCompileError && !status.hasTimeout && !MsgCollector.hasException
    }

    override fun getErrorMessageWithLocation(pathToFile: String): Pair<String, List<CompilerMessageLocation>> {
        val status = tryToCompile(pathToFile)
        return status.combinedOutput to status.locations
    }

    override fun isCompilerBug(pathToFile: String): Boolean {
        val status = tryToCompile(pathToFile)

        val simPath = ""
        val simList = File(simPath).readText().split("\n")

        if (!MsgCollector.hasCompileError && !status.hasTimeout && !MsgCollector.hasException) {
            val tempPath = ""
            val tempFile = File.createTempFile("mutant", ".kt", File(tempPath))
            val tempCoveragePath = ""
            tempFile.writeText(File(pathToFile).readText())
            val fileExec = Instrumenter(tempFile.absolutePath).instrument()
            CoverageReportMaker(tempCoveragePath, fileExec).createReport()
            val sim = CoverageComparator().computeSimilarity(tempCoveragePath).toDouble()
            if (sim < 0.81 || simList.contains(sim.toString())) {
                println("IGNORE Pass similarity = $sim; Mutant = ${tempFile.name}")
                Files.delete(Paths.get(tempFile.absolutePath))
                Files.delete(Paths.get(tempCoveragePath))
            }
            else {
                println("ADD Pass similarity = $sim; Mutant = ${tempFile.name}")
                Files.write(Paths.get(simPath), (sim.toString() + "\n").toByteArray(), StandardOpenOption.APPEND);
            }
        }

/*
        val simPath = ""
        val simList = File(simPath).readText().split("\n")
        if (MsgCollector.hasException) {
            val tempPath = ""
            val tempFile = File.createTempFile("mutant", ".kt", File(tempPath))
            val tempCoveragePath = ""
            tempFile.writeText(File(pathToFile).readText())
            val fileExec = Instrumenter(tempFile.absolutePath).instrument()
            CoverageReportMaker(tempCoveragePath, fileExec).createReport()
            val sim = CoverageComparator().computeSimilarity(tempCoveragePath).toDouble()
            if (sim < 0.85 || simList.contains(sim.toString())) {
                println("IGNORE Fail similarity = $sim; Mutant = ${tempFile.name}")
                Files.delete(Paths.get(tempFile.absolutePath))
                Files.delete(Paths.get(tempCoveragePath))
            }
            else {
                println("ADD Fail similarity = $sim; Mutant = ${tempFile.name}")
                Files.write(Paths.get(simPath), (sim.toString() + "\n").toByteArray(), StandardOpenOption.APPEND);
            }
        }
*/
        return MsgCollector.hasException

    }

//    override fun compile(path: String): CompilingResult {
//        val kotlinc = CompilerArgs.pathToKotlinc
//        val command =
//            if (arguments.isEmpty())
//                "$kotlinc $path -include-runtime -d $pathToCompiled"
//            else
//                "$kotlinc $path -include-runtime $arguments -d $pathToCompiled"
//        val status: String
//        try {
//            status = commonExec(command, Stream.BOTH)
//        } catch (e: Exception) {
//            return CompilingResult(-1, "")
//        }
//        return if (analyzeErrorMessage(status)) CompilingResult(
//            0,
//            pathToCompiled
//        ) else CompilingResult(-1, "")
//    }


    override fun compile(path: String, includeRuntime: Boolean): CompilingResult {
        val threadPool = Executors.newCachedThreadPool()
        val tmpJar = "$pathToCompiled.jar"

        val args = if (arguments.isEmpty())
            "$path -d $tmpJar"
        else
            "$path $arguments -d $tmpJar"
        val compiler = K2JVMCompiler()
        val compilerArgs =
            K2JVMCompilerArguments().apply { K2JVMCompiler().parseArguments(args.split(" ").toTypedArray(), this) }
        if (CompilerArgs.classpath.isNotEmpty())
            compilerArgs.classpath = CompilerArgs.classpath
        else
            compilerArgs.classpath =
                "${CompilerArgs.jvmStdLibPaths.joinToString(
                    postfix = ":",
                    separator = ":"
                )}:${System.getProperty("java.class.path")}"

        compilerArgs.jdkHome = CompilerArgs.jdkHome
        compilerArgs.jvmTarget = CompilerArgs.jvmTarget
        IncrementalCompilation.setIsEnabledForJvm(true)

        val services = Services.EMPTY
        MsgCollector.clear()
        val futureExitCode = threadPool.submit {
            compiler.exec(MsgCollector, services, compilerArgs)
        }
        var hasTimeout = false
        try {
            futureExitCode.get(10L, TimeUnit.SECONDS)
        } catch (ex: TimeoutException) {
            hasTimeout = true
            futureExitCode.cancel(true)
        }

        val status = KotlincInvokeStatus(
            MsgCollector.crashMessages.joinToString("\n") +
                    MsgCollector.compileErrorMessages.joinToString("\n"),
            !MsgCollector.hasCompileError,
            MsgCollector.hasException,
            hasTimeout
        )
        if (status.hasException || status.hasTimeout || !status.isCompileSuccess) return CompilingResult(-1, "")
        val res = File(pathToCompiled)
        val input = JarInputStream(File(tmpJar).inputStream())
        val output = JarOutputStream(res.outputStream(), input.manifest)
        copyFullJarImpl(output, File(tmpJar))
        if (includeRuntime)
            CompilerArgs.jvmStdLibPaths.forEach { writeRuntimeToJar(it, output) }
        output.finish()
        input.close()
        File(tmpJar).delete()
        return CompilingResult(0, pathToCompiled)
    }

    override fun tryToCompile(pathToFile: String): KotlincInvokeStatus {
        val threadPool = Executors.newCachedThreadPool()
        val trashDir = "tmp/trash/"
        //Clean dir
        if (File(trashDir).exists())
            FileUtils.cleanDirectory(File(trashDir))
        val args =
            if (arguments.isEmpty())
                "$pathToFile -d $trashDir".split(" ")
            else
                "$pathToFile $arguments -d $trashDir".split(" ")
        val compiler = K2JVMCompiler()
        val compilerArgs = K2JVMCompilerArguments().apply { K2JVMCompiler().parseArguments(args.toTypedArray(), this) }
        if (CompilerArgs.classpath.isNotEmpty())
            compilerArgs.classpath = CompilerArgs.classpath
        else
            compilerArgs.classpath =
                "${CompilerArgs.jvmStdLibPaths.joinToString(
                    postfix = ":",
                    separator = ":"
                )}:${System.getProperty("java.class.path")}"

        compilerArgs.jdkHome = CompilerArgs.jdkHome
        compilerArgs.jvmTarget = CompilerArgs.jvmTarget
        IncrementalCompilation.setIsEnabledForJvm(true)

        val services = Services.EMPTY
        MsgCollector.clear()
        val futureExitCode = threadPool.submit {
            compiler.exec(MsgCollector, services, compilerArgs)
        }
        var hasTimeout = false
        try {
            futureExitCode.get(10L, TimeUnit.SECONDS)
        } catch (ex: TimeoutException) {
            hasTimeout = true
            futureExitCode.cancel(true)
        }
        val status = KotlincInvokeStatus(
            MsgCollector.crashMessages.joinToString("\n") +
            MsgCollector.compileErrorMessages.joinToString("\n"),
            !MsgCollector.hasCompileError,
            MsgCollector.hasException,
            hasTimeout,
            MsgCollector.locations.toMutableList()
        )
        return status
    }

    override fun exec(path: String, streamType: Stream): String = commonExec("java -jar $path", streamType)

    private fun analyzeErrorMessage(msg: String): Boolean = !msg.split("\n").any { it.contains(": error:") }

    private val log = Logger.getLogger("compilerErrorsLog")
}