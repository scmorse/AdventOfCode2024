package day20

import java.io.File
import kotlin.math.abs

// https://adventofcode.com/2024/day/20
fun main() {
  val (start: Coordinate, target: Coordinate, map: Map<Coordinate, Char>) = readInput()
  val bestDistancesToTarget: Map<Coordinate, Int> = getBestDistancesTo(target, map)
  val bestDistancesToStart: Map<Coordinate, Int> = getBestDistancesTo(start, map)

  // Part 1
  val numCheatsSavingAtLeast100Picoseconds =
    bestDistancesToStart.keys
      .flatMap { startCheat ->
        startCheat.pairedWithEachValidEndCheatCoordinate(map, maxDistance = 2)
      }
      .count { (startCheat, endCheat) ->
        getCheatSavings(start, startCheat, endCheat, bestDistancesToStart, bestDistancesToTarget) >= 100
      }
  println("Part 1 num cheats saving at least 100 picoseconds: $numCheatsSavingAtLeast100Picoseconds")
  check(numCheatsSavingAtLeast100Picoseconds == 1499)

  // Part 2
  val numLongRangeCheatsSavingAtLeast100Picoseconds =
    bestDistancesToStart.keys
      .flatMap { startCheat ->
        startCheat.pairedWithEachValidEndCheatCoordinate(map, maxDistance = 20)
      }
      .count { (startCheat, endCheat) ->
        getCheatSavings(start, startCheat, endCheat, bestDistancesToStart, bestDistancesToTarget) >= 100
      }

  println("Part 2 num long-range cheats saving at least 100 picoseconds: $numLongRangeCheatsSavingAtLeast100Picoseconds")
  check(numLongRangeCheatsSavingAtLeast100Picoseconds == 1027164)
}

fun getBestDistancesTo(target: Coordinate, map: Map<Coordinate, Char>): Map<Coordinate, Int> {
  var fringe = setOf(target)
  var fringeDistance = 0
  val distances = mutableMapOf(target to 0)
  while (true) {
    val nextFringe = fringe
      .flatMap { coordinate -> coordinate.neighbors() }
      .filter { neighbor -> neighbor !in distances && neighbor in map && map[neighbor] != '#' }
      .toSet()
      .onEach { distances[it] = fringeDistance + 1 }
    if (nextFringe.isEmpty()) {
      return distances
    }
    fringe = nextFringe
    fringeDistance++
  }
}

fun Coordinate.pairedWithEachValidEndCheatCoordinate(
  map: Map<Coordinate, Char>,
  maxDistance: Int,
): List<Pair<Coordinate, Coordinate>> =
  buildList {
    val startCheat = this@pairedWithEachValidEndCheatCoordinate
    for (xDelta in -maxDistance..maxDistance) {
      val yDeltaMax = maxDistance - abs(xDelta)
      for (yDelta in -yDeltaMax..yDeltaMax) {
        val endCheat = Coordinate(x = x + xDelta, y = y + yDelta)
        if (map[endCheat] == '#' || endCheat !in map) continue
        if (startCheat.distanceTo(endCheat) !in 2..maxDistance) continue
        this@buildList.add(startCheat to endCheat)
      }
    }
  }

fun getCheatSavings(
  start: Coordinate,
  startCheat: Coordinate,
  endCheat: Coordinate,
  bestDistancesToStart: Map<Coordinate, Int>,
  bestDistancesToTarget: Map<Coordinate, Int>,
): Int {
  val distWithoutCheat = bestDistancesToTarget[start]!!
  val distWithCheat =
    bestDistancesToStart[startCheat]!! + startCheat.distanceTo(endCheat) + bestDistancesToTarget[endCheat]!!
  return distWithoutCheat - distWithCheat
}

data class Coordinate(val x: Int, val y: Int) {
  operator fun plus(direction: Direction) = Coordinate(x = x + direction.x, y = y + direction.y)
  fun distanceTo(other: Coordinate): Int = abs(x - other.x) + abs(y - other.y)
  fun neighbors(): List<Coordinate> = Direction.entries.map { this + it }
}

enum class Direction(val x: Int, val y: Int) {
  N(x = 0, y = -1),
  E(x = 1, y = 0),
  S(x = 0, y = 1),
  W(x = -1, y = 0)
}

private fun readInput(): Triple<Coordinate, Coordinate, Map<Coordinate, Char>> {
  val lines = File("src/main/kotlin/day20/input.txt").readLines()
    .mapNotNull { it.trim().ifBlank { null } }

  lateinit var start: Coordinate
  lateinit var target: Coordinate
  val map: Map<Coordinate, Char> = buildMap {
    lines.forEachIndexed { y, line ->
      line.forEachIndexed { x, char ->
        if (char == 'S') start = Coordinate(x, y)
        if (char == 'E') target = Coordinate(x, y)
        this@buildMap.put(Coordinate(x, y), char)
      }
    }
  }
  return Triple(start, target, map)
}