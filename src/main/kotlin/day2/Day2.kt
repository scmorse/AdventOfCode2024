package day2

import second
import java.io.File

fun main() {
  val input = readInput()

  // Part 1
  val numSafeForPart1 = input.count { row -> row.isSafeForPart1() }
  println("Safe for part 1: $numSafeForPart1") // 479

  // Part 2
  val numSafeForPart2 = input.count { row -> row.isSafeForPart2() }
  println("Safe for part 2: $numSafeForPart2") // 531
}

fun List<Long>.isSafeForPart2(): Boolean {
  return indices
    .map { iToOmit -> filterIndexed { i, _ -> i != iToOmit } }
    .any { it.isSafeForPart1() }
}

fun List<Long>.isSafeForPart1(): Boolean =
  if (first() < second()) {
    zipWithNext().all { it.second - it.first in 1..3 }
  } else {
    zipWithNext().all { it.first - it.second in 1..3 }
  }

private fun readInput(): List<List<Long>> {
  return File("src/main/kotlin/day2/input.txt").readLines()
    .map { line ->
      line.split(" ").map { it.toLong() }
    }
}
