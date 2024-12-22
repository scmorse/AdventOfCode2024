package day22

import java.io.File

// https://adventofcode.com/2024/day/22
fun main() {
  val input: List<String> = readInput()

  // Part 1
  val part1Answer = input.sumOf { startingSecret ->
    getPseudoRandomSequence(startingSecret).last()
  }
  println("Part 1 sum of last pseudorandom numbers: $part1Answer")
  check(part1Answer == 17960270302L)

  // Part 2
  val sumsByDiffs = mutableMapOf<List<Int>, SumAndIds>()
  input.forEachIndexed { id, startingSecret ->
    getPseudoRandomSequence(startingSecret)
      .map { (it % 10).toInt() }
      .windowed(5)
      .forEach { groupOf5 ->
        val diffs = groupOf5.zipWithNext().map { (a, b) -> a - b }
        sumsByDiffs.getOrPut(diffs) { SumAndIds() }.addIfNew(id, groupOf5.last())
      }
  }
  val mostBananas = sumsByDiffs.values.maxOf { it.sum }
  println("Part 2 most bananas for any one instruction: $mostBananas")
  check(mostBananas == 2042)
}

fun getPseudoRandomSequence(startingSecret: String): Sequence<Long> = sequence {
  var s = startingSecret.toLong()
  yield(s)
  repeat(2000) {
    s = ((s * 64L) xor s) % 16777216L
    s = ((s / 32L) xor s) % 16777216L
    s = ((s * 2048L) xor s) % 16777216L
    yield(s)
  }
}

class SumAndIds(
  private val ids: MutableSet<Int> = mutableSetOf(),
  var sum: Int = 0,
) {
  fun addIfNew(id: Int, value: Int) {
    if (ids.add(id)) sum += value
  }
}

private fun readInput(): List<String> {
  return File("src/main/kotlin/day22/input.txt").readLines()
    .mapNotNull { it.trim().ifBlank { null } }
}