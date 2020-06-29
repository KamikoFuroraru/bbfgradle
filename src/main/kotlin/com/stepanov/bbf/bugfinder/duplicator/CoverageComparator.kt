package com.stepanov.bbf.bugfinder.duplicator

import java.io.File
import java.io.FileWriter
import kotlin.math.sqrt

class CoverageComparator() {

    fun computeSimilarity(mutantProgram: String, inputProgramCsv: String): String {
        return if (File(mutantProgram).isDirectory) computeSimilarityBtwAllPrograms(mutantProgram, inputProgramCsv)
        else computeSimilarityBtw2Programs(mutantProgram, inputProgramCsv).toString()
    }

    fun computeSimilarityBtw2Programs(mutantProgramCsv: String, inputProgramCsv: String): Double {
        val coverage1 = File(inputProgramCsv).readText().split("\n")
        val coverage2 = File(mutantProgramCsv).readText().split("\n") // take our mutant
        var count = 0 // count of files
        var bigRelation = 0.0 // sum of relations of all files
        var relation: Double
        for (i in 1 until coverage1.lastIndex + 1) { // going for files
            relation = computeSimilarityBtw2Files(coverage1[i], coverage2[i])

            if (relation != -1.0) {
                bigRelation += relation // BIG relation for ALL files
                count++
            }
        }
        return bigRelation / count
    }

    fun computeSimilarityBtwAllPrograms(mutantProgramPath: String, inputProgramCsv: String): String {
        val mutantArray = arrayListOf<String>()
        File(mutantProgramPath).walkTopDown().forEach { mutantArray.add(it.absolutePath) }
        val resultList = arrayListOf<String>()
        val coverage1 = File(inputProgramCsv).readText().split("\n")
        for (f in 1 until mutantArray.lastIndex + 1) { // go to all mutants
            val coverage2 = File(mutantArray[f]).readText().split("\n") // take our mutant
            var count = 0 // count of files
            var bigRelation = 0.0 // sum of relations of all files
            var relation = -1.0
            for (i in 1 until coverage1.lastIndex + 1) { // going for files
                relation = computeSimilarityBtw2Files(coverage1[i], coverage2[i])

                if (relation != -1.0) {
                    bigRelation += relation // BIG relation for ALL files
                    count++
                }
            }
            //break
            //if (relation > 0.8 && !resultList.contains(relation.toString())) { // the same
            //println("mutant: " + File(listAr[f]).nameWithoutExtension + " similarity: " + result.toString())
            resultList.add("mutant: " + File(mutantArray[f]).nameWithoutExtension + " similarity: " + (bigRelation / count).toString())
            // }
        }
        return resultList.joinToString(separator = "\n")
    }

    fun computeSimilarityBtw2Files(coverage1: String, coverage2: String): Double {
        return if (coverage1 != coverage2) { // if files not equals
            val lineCoverage1 = coverage1.split(",")[3] // take line coverage of file
            val lineCoverage2 = coverage2.split(",")[3]
            if (lineCoverage1.toSet().isEmpty() || lineCoverage2.toSet().isEmpty() || (lineCoverage1 == lineCoverage2)) { // if line coverage IS empty
                -1.0
            } else {
                //println(set1 + "FFFFFFFFFFF" + set2)
                val sumNumerator = (lineCoverage1.split(";").toSet() intersect lineCoverage2.split(";").toSet()).size
                val sumDenominator = lineCoverage1.split(";").toSet().size * lineCoverage2.split(";").toSet().size
                val relation = sumNumerator / sqrt(sumDenominator.toDouble()) // relation for ONE file
                if (relation != 1.0) { // if files not equals by calculating a relation
                    relation
                } else -1.0
            }
        } else -1.0
    }

}