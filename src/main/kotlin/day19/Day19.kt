package day19

import arrow.core.MemoizationCache
import arrow.core.MemoizedDeepRecursiveFunction
import java.io.File

// https://adventofcode.com/2024/day/19
fun main() {
  val puzzles: List<TowelMatchPuzzle> = readInput()

  // Part 1
  val numPatternsWithAtLeastOneSolution = puzzles.count { (pattern, towels) ->
    findTowelsToFormPattern(pattern, towels).isNotEmpty()
  }
  println("Part 1 num patterns with at least one solution: $numPatternsWithAtLeastOneSolution")
  check(numPatternsWithAtLeastOneSolution == 333)

  // Part 2
  val totalNumCombinationsToFormPatterns = puzzles.sumOf { puzzle ->
    countTowelCombinationsToFormPatternMemoized(puzzle)
  }
  println("Part 2 total num combinations to form patterns: $totalNumCombinationsToFormPatterns")
  check(totalNumCombinationsToFormPatterns == 678536865274732L)
}

data class TowelMatchPuzzle(val pattern: String, val towels: List<String>)

fun findTowelsToFormPattern(pattern: String, towels: List<String>): List<String> {
  if (pattern in towels) return listOf(pattern)
  for (towel in towels) {
    if (!pattern.startsWith(towel)) continue
    val towelsToFormRestOfPattern = findTowelsToFormPattern(pattern.drop(towel.length), towels)
    if (towelsToFormRestOfPattern.isNotEmpty()) {
      return listOf(towel) + towelsToFormRestOfPattern
    }
  }
  return emptyList()
}

val countTowelCombinationsToFormPatternMemoized: DeepRecursiveFunction<TowelMatchPuzzle, Long> =
  MemoizedDeepRecursiveFunction(MapMemoizationCache()) { (pattern, towels) ->
    var matches = 0L
    for (towel in towels) {
      if (towel == pattern) {
        matches++
        continue
      }
      if (!pattern.startsWith(towel)) continue
      matches += callRecursive(TowelMatchPuzzle(pattern.drop(towel.length), towels))
    }
    matches
  }

// The default cache for MemoizedDeepRecursiveFunction is an Atomic cache, which is
// significantly slower (45s vs 100ms for this problem).
class MapMemoizationCache : MemoizationCache<TowelMatchPuzzle, Long> {
  private val mapCache = mutableMapOf<TowelMatchPuzzle, Long>()
  override fun get(key: TowelMatchPuzzle): Long? = mapCache[key]
  override fun set(key: TowelMatchPuzzle, value: Long): Long = value.also { mapCache[key] = value }
}

private fun readInput(): List<TowelMatchPuzzle> {
  val lines = File("src/main/kotlin/day19/input.txt").readLines()
    .mapNotNull { it.trim().ifBlank { null } }
  val towels: List<String> = lines.first().split(",").map { it.trim() }
  return lines.drop(1).map { pattern -> TowelMatchPuzzle(pattern, towels) }
}
