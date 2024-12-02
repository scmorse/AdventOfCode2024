package day1

import second
import java.io.File
import kotlin.math.abs

fun main() {
  val (list1: List<Long>, list2: List<Long>) = readInput()

  // Part 1
  val distance = list1.sorted().zip(list2.sorted())
    .sumOf { abs(it.first - it.second) }
  println("Part 1 distance: $distance") // 1388114

  // Part 2
  val counts = list2.groupingBy { it }.eachCount()
  val similarity = list1.sumOf { it * (counts[it] ?: 0) }
  println("Part 2 similarity: $similarity") // 23529853
}

private fun readInput(): Pair<List<Long>, List<Long>> {
  return File("src/main/kotlin/day1/input.txt").readLines()
    .map { line ->
      val parts = line.split("   ").apply { require(size == 2) }
      parts.first().toLong() to parts.second().toLong()
    }
    .unzip()
}
