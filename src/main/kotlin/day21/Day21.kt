package day21

import arrow.core.memoize
import day21.Direction.*
import java.io.File
import kotlin.math.abs

// https://adventofcode.com/2024/day/21
fun main() {
  val input: List<String> = readInput()

  // Part 1
  val part1TotalComplexity = input.sumOf { code ->
    calculateComplexity(code, iterations = 3)
  }
  println("Part 1 total complexity: $part1TotalComplexity")
  check(part1TotalComplexity == 237342L)

  // Part 2
  val part2TotalComplexity = input.sumOf { code ->
    calculateComplexity(code, iterations = 26)
  }
  println("Part 2 total complexity: $part2TotalComplexity")
  check(part2TotalComplexity == 294585598101704L)
}

fun calculateComplexity(code: String, iterations: Int): Long =
  getMinNumPresses(code, iterations) * code.replace("A", "").toLong()

fun getMinNumPresses(code: String, iterations: Int): Long {
  return "A$code"
    .map { char -> NUMBER_PAD.entries.first { it.value == char }.key }
    .zipWithNext()
    .sumOf { (start, target) ->
      getMinNumDirectionalKeypadPresses(
        start = start,
        target = target,
        iterations = iterations,
        padKeys = NUMBER_PAD.keys,
      )
    }
}

val memoizeMap = mutableMapOf<Triple<Coordinate, Coordinate, Int>, Long>()
fun getMinNumDirectionalKeypadPresses(
  start: Coordinate,
  target: Coordinate,
  iterations: Int,
  padKeys: Set<Coordinate>,
): Long {
  val memoizeKey = Triple(start, target, iterations)
  memoizeMap[memoizeKey]?.let { return it }

  return when {
    iterations == 1 -> 1L + abs(target.y - start.y) + abs(target.x - start.x)
    else -> {
      val snDirection = if (target.y > start.y) S else N
      val ewDirection = if (target.x > start.x) E else W
      listOfNotNull(
        getDirectionalKeypadPresses(
          start, snDirection to abs(target.y - start.y), ewDirection to abs(target.x - start.x), padKeys
        ),
        if (target.y == start.y || target.x == start.x) null else {
          getDirectionalKeypadPresses(
            start, ewDirection to abs(target.x - start.x), snDirection to abs(target.y - start.y), padKeys
          )
        },
      ).minOf { presses ->
        (listOf(A) + presses)
          .zipWithNext()
          .sumOf { (start, target) ->
            getMinNumDirectionalKeypadPresses(
              start = start.location(),
              target = target.location(),
              iterations = iterations - 1,
              padKeys = DIRECTIONAL_KEYPAD.keys,
            )
          }
      }
    }
  }.also {
    memoizeMap[memoizeKey] = it
  }
}

private val getDirectionalKeypadPresses =
  fun(
    start: Coordinate,
    steps1: Pair<Direction, Int>,
    steps2: Pair<Direction, Int>,
    padKeys: Set<Coordinate>,
  ): List<DirectionalKeypadPress>? {
    var loc = start
    return buildList {
      repeat(steps1.second) {
        loc += steps1.first
        if (loc !in padKeys) return null
        this@buildList.add(steps1.first)
      }
      repeat(steps2.second) {
        loc += steps2.first
        if (loc !in padKeys) return null
        this@buildList.add(steps2.first)
      }
      this@buildList.add(A)
    }
  }.memoize()

fun DirectionalKeypadPress.location(): Coordinate =
  when (this) {
    N -> Coordinate(1, 0)
    A -> Coordinate(2, 0)
    W -> Coordinate(0, 1)
    S -> Coordinate(1, 1)
    E -> Coordinate(2, 1)
  }

val NUMBER_PAD: Map<Coordinate, Char> = mapOf(
  Coordinate(0, 0) to '7', Coordinate(1, 0) to '8', Coordinate(2, 0) to '9',
  Coordinate(0, 1) to '4', Coordinate(1, 1) to '5', Coordinate(2, 1) to '6',
  Coordinate(0, 2) to '1', Coordinate(1, 2) to '2', Coordinate(2, 2) to '3',
  /*        EMPTY       */ Coordinate(1, 3) to '0', Coordinate(2, 3) to 'A',
)

val DIRECTIONAL_KEYPAD: Map<Coordinate, Char> = mapOf(
  /*        EMPTY       */ Coordinate(1, 0) to '^', Coordinate(2, 0) to 'A',
  Coordinate(0, 1) to '<', Coordinate(1, 1) to 'v', Coordinate(2, 1) to '>',
)

sealed interface DirectionalKeypadPress

data object A : DirectionalKeypadPress

data class Coordinate(val x: Int, val y: Int) {
  operator fun plus(direction: Direction) = Coordinate(x = x + direction.x, y = y + direction.y)
}

enum class Direction(val x: Int, val y: Int) : DirectionalKeypadPress {
  N(x = 0, y = -1),
  E(x = 1, y = 0),
  S(x = 0, y = 1),
  W(x = -1, y = 0),
}

private fun readInput(): List<String> {
  return File("src/main/kotlin/day21/input.txt").readLines()
    .mapNotNull { it.trim().ifBlank { null } }
}