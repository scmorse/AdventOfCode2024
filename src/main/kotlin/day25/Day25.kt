package day25

import java.io.File

fun main() {
  val blocks: List<Map<Xy, Char>> = readInput().chunked(8)
    .map { chunk -> chunk.dropLastWhile { it.isEmpty() } }
    .map { chunk ->
      buildMap {
        chunk.forEachIndexed { y, line ->
          line.forEachIndexed { x, char ->
            this@buildMap.put(Xy(x, y), char)
          }
        }
      }
    }

  // Part 1
  val (locks, keys) = blocks.partition { it[Xy(0, 0)] == '#' }
  val numPairsThatFit = locks.pairedWithEach(keys).count { (lock, key) -> !lock.overlapsWith(key) }
  println("Part 1 num lock & key pairs that fit together: $numPairsThatFit")
  check(numPairsThatFit == 3508)
}

fun Map<Xy, Char>.overlapsWith(other: Map<Xy, Char>): Boolean =
  (entries.toList() + other.entries.toList()).groupBy { it.key }
    .values.any { group -> group.count { it.value == '#' } >= 2 }

fun <T, U> List<T>.pairedWithEach(other: List<U>): List<Pair<T, U>> =
  flatMap { t -> other.map { u -> t to u } }

data class Xy(val x: Int, val y: Int)

private fun readInput(): List<String> {
  return File("src/main/kotlin/day25/input.txt").readLines().map { it.trim() }
}
