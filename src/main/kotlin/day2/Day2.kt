package day2

import second

fun main() {
  // Part 1
  val numSafeForPart1 = input.count { row -> row.isSafeForPart1() }
  println("Safe for part 1: $numSafeForPart1")

  // Part 2
  val numSafeForPart2 = input.count { row -> row.isSafeForPart2() }
  println("Safe for part 2: $numSafeForPart2")
}

fun List<Int>.isSafeForPart2(): Boolean {
  return indices
    .map { iToOmit -> filterIndexed { i, _ -> i != iToOmit} }
    .any { it.isSafeForPart1() }
}

fun List<Int>.isSafeForPart1(): Boolean =
  if (first() < second()) {
    consecutivePairs().all { it.second - it.first in 1..3 }
  } else {
    consecutivePairs().all { it.first - it.second in 1..3 }
  }

fun <T> List<T>.consecutivePairs(): Sequence<Pair<T, T>> = sequence {
  for (i in indices.drop(1)) yield(get(i - 1) to get(i))
}
