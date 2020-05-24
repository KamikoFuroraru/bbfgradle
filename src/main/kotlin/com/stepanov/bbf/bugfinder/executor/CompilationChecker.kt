package com.stepanov.bbf.bugfinder.executor

import com.intellij.psi.PsiFile
import com.stepanov.bbf.bugfinder.manager.Bug
import com.stepanov.bbf.bugfinder.manager.BugType
import com.stepanov.bbf.bugfinder.util.saveOrRemoveToTmp
import org.apache.log4j.Logger
import org.jetbrains.kotlin.psi.KtFile
import java.io.File

open class CompilationChecker(private val compilers: List<CommonCompiler>) : Checker() {

    override fun isCompilationSuccessful(project: Project): Boolean {
        val path = project.saveOrRemoveToTmp(true)
        val res = compilers.all { it.checkCompiling(path) }

        ///
        /*
        val simPath = ""
        val simList = File(simPath).readText().split("\n")
        if (res) {
            val tempPath = ""
            val tempFile = File.createTempFile("mutant", ".kt", File(tempPath))
            val tempCoveragePath = ""
            tempFile.writeText(File(path).readText())
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
         */
        ///

        project.saveOrRemoveToTmp(false)
        return res
    }

    override fun isCompilerBug(project: Project): List<Bug> {
        val path = project.saveOrRemoveToTmp(true)
        val res = mutableListOf<Bug>()
        compilers.forEach { compiler ->
            if (compiler.isCompilerBug(path)) {
                val msg = compiler.getErrorMessage(path)
                val type =
                    if (msg.contains("Exception while analyzing expression")) BugType.FRONTEND else BugType.BACKEND
                res.add(Bug(compiler, msg, project, type))
            }
        }
        if (res.size != 0) return res
        val compilersToStatus = compilers.map { it to it.checkCompiling(path) }
        val grouped = compilersToStatus.groupBy { it.first.compilerInfo.split(" ").first() }
        for (g in grouped) {
            if (g.value.map { it.second }.toSet().size != 1) {
                val diffCompilers =
                    g.value.groupBy { it.second }.mapValues { it.value.first().first }.values.toList()
                res.add(
                    Bug(diffCompilers, "", project, BugType.DIFFCOMPILE)
                )
            }
        }
        project.saveOrRemoveToTmp(false)
        return res
    }


    override val additionalConditions: MutableList<(PsiFile) -> Boolean> = mutableListOf()
}