package com.stepanov.bbf.bugfinder.duplicator

import java.io.File
import java.io.FileWriter
import kotlin.math.abs
import kotlin.math.sqrt

class SuspiciousValueComputation {

    fun computeSuspiciousValue(mutantProgramPath: String, inputProgramCsv: String, resultPath: String) {
        val resultList = arrayListOf<Pair<String, Double>>()
        val mutantArray = arrayListOf<String>()
        File(mutantProgramPath).walkTopDown().forEach { mutantArray.add(it.absolutePath) }
        val coverageInput = File(inputProgramCsv).readText().split("\n")
        var eps: Int
        var sus: Double
        var SUS: Double
        for (file in 1 until coverageInput.lastIndex) { // going for compiler files
            println(file)
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
                            if (lineCoverage1 == lineCoverage2 || ((pack1 == pack2) and (filename1 == filename2) and lineCoverage2.split(";").contains(lines[line]))) {
                                eps++
                            }
                        }
                    }
                    sus += 1 / (sqrt((1 + eps).toDouble()))
                }
                SUS = sus / lines.size
            }
            if (SUS != 0.0) resultList.add(Pair("package:$pack1,filename:$filename1", SUS))
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

    fun compareSus(SUS1: String, SUS2: String): Double {
//        val set1 = File(SUS1).readText().split("\n").toSet()
//        val set2 = File(SUS2).readText().split("\n").toSet()
//        val numerator = (set1 intersect set2).size
//        val denominator = sqrt((set1.size * set2.size).toDouble())
//        val relation = numerator / denominator
//        return relation
        val file1 = File(SUS1).readText()
        val file2 = File(SUS2).readText()

        val array1 = file1.split("\n")
        val array2 = file2.split("\n")

        val name1 = arrayListOf<String>()
        val values1 = arrayListOf<Double>()
        val name2 = arrayListOf<String>()
        val values2 = arrayListOf<Double>()

        var str: List<String>
        for (element in array1) {
            if (element.isEmpty()) continue
            str = element.split(",")
            name1.add(str[0] + str[1])
            values1.add(str[2].toDouble())
        }

        for (element in array2) {
            if (element.isEmpty()) continue
            str = element.split(",")
            name2.add(str[0] + str[1])
            values2.add(str[2].toDouble())
        }

        var numerator = 0
        var index1: Int
        var index2: Int
        for (name in name1) {
            if (name2.contains(name)) {
                index1 = name1.indexOf(name)
                index2 = name2.indexOf(name)
                if (abs(values1[index1] - values2[index2]) < 0.1) {
                    numerator++
                }
            }
        }

        return (numerator / sqrt((name1.size * name2.size).toDouble()))
    }

}
