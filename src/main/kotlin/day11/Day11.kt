package day11

import java.io.File

// https://adventofcode.com/2024/day/11
fun main() {
  val input: List<Long> = readInput()

  // Part 1
  var stones = input
  repeat(25) { i ->
    stones = stones.flatMap { stone ->
      val stoneStr = "$stone"
      when {
        stone == 0L -> listOf(1L)
        stoneStr.length % 2 == 0 -> listOf(stoneStr.leftHalf.toLong(), stoneStr.rightHalf.toLong())
        else -> listOf(stone * 2024L)
      }
    }
  }
  println("Part 1 num stones after 25 blinks: ${stones.size}")
  check(stones.size == 189547)

  // Part 2
  val numStonesAfter75Blinks = input.sumOf { getNumStonesAfterBlinks(it, 75) }
  println("Part 2 num stones after 75 blinks: $numStonesAfter75Blinks")
  check(numStonesAfter75Blinks == 224577979481346L)
}

val memoizeMap = mutableMapOf<Pair<Long, Int>, Long>()
fun getNumStonesAfterBlinks(stone: Long, totalNumIterations: Int): Long {
  if (totalNumIterations == 0) return 1

  memoizeMap[stone to totalNumIterations]?.let { return it }

  val stoneStr = "$stone"
  return when {
    stone == 0L -> getNumStonesAfterBlinks(1L, totalNumIterations - 1)
    stoneStr.length % 2 == 0 -> {
      getNumStonesAfterBlinks(stoneStr.leftHalf.toLong(), totalNumIterations - 1) +
        getNumStonesAfterBlinks(stoneStr.rightHalf.toLong(), totalNumIterations - 1)
    }
    else -> getNumStonesAfterBlinks(stone * 2024L, totalNumIterations - 1)
  }.also {
    memoizeMap[stone to totalNumIterations] = it
  }
}

private val String.leftHalf get() = slice(0 until length / 2)
private val String.rightHalf get() = slice(length / 2 until length)

private fun readInput(): List<Long> {
  return File("src/main/kotlin/day11/input.txt").readLines()
    .firstNotNullOf { it.trim().ifBlank { null } }
    .split(" ")
    .map { it.toLong() }
}
