package com.stepanov.bbf.bugfinder.duplicator

import java.io.File
import java.io.FileWriter
import kotlin.math.sqrt

class CoverageComparator() {

    fun compareSus(SUS1: String, SUS2: String) {
        val set1 = File(SUS1).readText().split("\n").toSet()
        val set2 = File(SUS2).readText().split("\n").toSet()
        val numerator = (set1 intersect set2).size
        val denominator = sqrt((set1.size * set2.size).toDouble())
        val relation = numerator / denominator
        println(relation)
    }

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
            if (lineCoverage1.toSet().isEmpty() || lineCoverage2.toSet().isEmpty()) { // if line coverage IS empty
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

    fun computeSuspiciousValue(mutantProgramPath: String, inputProgramCsv: String, resultPath: String) {
        val resultList = arrayListOf<Pair<String, Double>>()
        val mutantArray = arrayListOf<String>()
        File(mutantProgramPath).walkTopDown().forEach { mutantArray.add(it.absolutePath) }
        val coverageInput = File(inputProgramCsv).readText().split("\n")
        var eps: Int
        var sus: Double
        var SUS: Double
        for (file in 1 until coverageInput.lastIndex) { // going for files
            eps = 0
            sus = 0.0
            SUS = 0.0
            val file1 = coverageInput[file] // take file
            val pack1 = file1.split(",")[0]
            val filename1 = file1.split(",")[1]
            val lineCoverage1 = file1.split(",")[3]
            val lines = lineCoverage1.split(";")
            if (lineCoverage1.toSet().isEmpty()) {
                continue
            } else {
                for (line in lines.indices) { // going for lines
                    for (mutant in 1 until mutantArray.lastIndex) { // going for all mutants
                        val coverageMutant = File(mutantArray[mutant]).readText().split("\n") // take our mutant and all its files
                        val file2 = coverageMutant[file] // take file the same as in input src
                        val pack2 = file2.split(",")[0]
                        val filename2 = file2.split(",")[1]
                        val lineCoverage2 = file2.split(",")[3]
                        if (lineCoverage2.toSet().isEmpty()) {
                            continue
                        } else {
                            if ((pack1 == pack2) and (filename1 == filename2) and lineCoverage2.split(";").contains(lines[line])) {
                                eps++
                                break
                            }
                        }
                    }
                    sus += 1 / (sqrt((1 + eps).toDouble()))
                }
                SUS = sus / lines.size
            }
            resultList.add(Pair("package:$pack1,filename:$filename1", SUS))
        }
        resultList.sortByDescending { it.second }

        makeSusRating(resultList, resultPath)

    }

    fun makeSusRating(resultList: ArrayList<Pair<String, Double>>, path: String) {
        val writer = FileWriter(path)
        for (str in resultList) {
            writer.write(str.first + "," + str.second + System.lineSeparator())
        }
        writer.close()
    }


    fun articleSearch() {
        /*

        val listAr = arrayListOf<String>()
        val list = File("/home/vera/Downloads/bugs/coverage/pass").walkTopDown().forEach { listAr.add(it.absolutePath) }

    //val fileCoverage1 = "/home/vera/Downloads/bugs/coverage/test2.kt.csv"
    //val abc1 = File(fileCoverage1).readText().split("\n")

        val resultList = arrayListOf<String>()
        for (f in 1 until listAr.lastIndex + 1) { // go to all mutants
            if (resultList.isEmpty()) resultList.add(listAr[f]) // result list is a RO: if RO empty -> add first finding
            else { // else RO NOT empty -> compare mutant with all mutants in result list
                val abc2 = File(listAr[f]).readText().split("\n") // take our mutant
                var flag = false // flag for add or not in result list current mutant
                for (p in 0 until resultList.lastIndex + 1) { // going through all results mutants to compare with current
                    val abc1 = File(resultList[p]).readText().split("\n") // take result mutant and slice it for files
                    var count = 0 // count of files
                    var bigRelation = 0.0 // sum of relations of all files
                    for (i in 1 until abc1.lastIndex + 1) { // going for files
                        val cov1 = abc1[i] // take file
                        val cov2 = abc2[i]
                        if (!cov1.equals(cov2)) { // if files not equals
                            val set1 = cov1.split(",")[3] // take line coverage of file
                            val set2 = cov2.split(",")[3]

                            if (set1.toSet().isEmpty() || set2.toSet().isEmpty()) { // if line coverage IS empty
                                continue
                            } else { // else line coverage NOT empty
                                // calc formulae
                                // get line coverage -> making set from it splitting by ";" -> do intersect
                                // get line coverage -> making set from it splitting by ";" -> multiply power
                                // get relation
                                val sumNumerator = (set1.split(";").toSet() intersect set2.split(";").toSet()).size
                                val sumDenominator = set1.split(";").toSet().size * set2.split(";").toSet().size
                                val relation = sumNumerator / Math.sqrt(sumDenominator.toDouble()) // relation for ONE file
                                if (relation != 1.0) { // if files not equals by calculating a relation
                                    bigRelation += relation // BIG relation for ALL files
                                    count++
                                }
                            }
                        }
                    }
                    val result = bigRelation / count
                    if (result < 0.88) { // the same
                        flag = false
                        break
                    } else flag = true
                }
                if (flag) {
                    resultList.add(listAr[f])
                }
            }

        }

         */

    }

}