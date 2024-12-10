package day01

import java.io.File
import kotlin.math.abs

// https://adventofcode.com/2024/day/1
fun main() {
  val (list1: List<Long>, list2: List<Long>) = readInput()

  // Part 1
  val distance = list1.sorted().zip(list2.sorted())
    .sumOf { abs(it.first - it.second) }
  println("Part 1 distance: $distance")
  check(distance == 1388114L)

  // Part 2
  val counts = list2.groupingBy { it }.eachCount()
  val similarity = list1.sumOf { it * (counts[it] ?: 0) }
  println("Part 2 similarity: $similarity")
  check(similarity == 23529853L)
}

private fun readInput(): Pair<List<Long>, List<Long>> {
  return File("src/main/kotlin/day01/input.txt").readLines()
    .map { line ->
      val parts = line.split("   ").apply { require(size == 2) }
      parts.first().toLong() to parts.last().toLong()
    }
    .unzip()
}
