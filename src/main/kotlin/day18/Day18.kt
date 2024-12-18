package day18

import arrow.core.memoize
import java.io.File
import java.util.*
import kotlin.math.abs

// https://adventofcode.com/2024/day/18
fun main() {
  val (start: Coordinate, target: Coordinate, corruptedMemoryCoordinates: List<Coordinate>) = readInput()

  // Part 1
  val bestPathFor1024 = getBestPath(start, target, corruptedMemoryCoordinates.take(1024).toMap())!!
  println("Part 1 min steps to solve maze with 1024 corrupted coordinates: ${bestPathFor1024.steps.size}")
  check(bestPathFor1024.steps.size == 380)

  // Part 2
  val getBestPathMemoized: (Int) -> Path? = { numCorruptedCoordinates: Int ->
    getBestPath(start, target, corruptedMemoryCoordinates.take(numCorruptedCoordinates).toMap())
  }.memoize()
  val firstCoordinateThatMakesMazeUnsolvable: Coordinate = (1..corruptedMemoryCoordinates.size).toList()
    .binarySearch { numCorruptedCoordinates ->
      when {
        getBestPathMemoized(numCorruptedCoordinates) != null -> -1
        getBestPathMemoized(numCorruptedCoordinates - 1) == null -> 1
        else -> 0
      }
    }
    .let { corruptedMemoryCoordinates[it] }
  println("Part 2 first coordinate that makes the maze unsolvable: $firstCoordinateThatMakesMazeUnsolvable")
  check(firstCoordinateThatMakesMazeUnsolvable == Coordinate(x = 26, y = 50))
}

fun List<Coordinate>.toMap(): Map<Coordinate, Char> {
  val corrupted = this
  return buildMap {
    (0..70).forEach { y ->
      (0..70).forEach { x ->
        val coordinate = Coordinate(x = x, y = y)
        this@buildMap.put(coordinate, if (coordinate in corrupted) '#' else '.')
      }
    }
  }
}

fun getBestPath(
  start: Coordinate,
  target: Coordinate,
  map: Map<Coordinate, Char>,
): Path? {
  val queue: PriorityQueue<Path> = PriorityQueue { a: Path, b: Path ->
    if (a.heuristicDistanceTo(target) != b.heuristicDistanceTo(target)) {
      a.heuristicDistanceTo(target) - b.heuristicDistanceTo(target)
    } else {
      b.steps.size - a.steps.size
    }
  }
  queue.add(
    Path(
      coordinate = start,
      steps = emptyList(),
      start = start,
    )
  )

  val addedScores = mutableMapOf(start to 0)
  while (queue.isNotEmpty()) {
    val search: Path = queue.remove()
    if (search.coordinate == target) {
      return search
    }

    for (direction in Direction.entries.filter { map[search.coordinate + it] == '.' }) {
      val nextCoordinate = search.coordinate + direction
      val nextDistanceTraveled = search.steps.size + 1
      if (addedScores[nextCoordinate]?.let { nextDistanceTraveled >= it } == true) {
        // We've found path that arrives at the same coordinate with same or less cost
        continue
      }

      queue.add(
        Path(
          coordinate = nextCoordinate,
          steps = search.steps + Pair(direction, nextCoordinate),
          start = search.start,
        ).also {
          addedScores[nextCoordinate] = it.steps.size
        }
      )
    }
  }
  return null
}

data class Path(
  val coordinate: Coordinate,
  val steps: List<Pair<Direction, Coordinate>>,
  val start: Coordinate,
) {
  fun heuristicDistanceTo(target: Coordinate) =
    steps.size + abs(coordinate.x - target.x) + abs(coordinate.y - target.y)
}

data class Coordinate(val x: Int, val y: Int) {
  operator fun plus(direction: Direction) = Coordinate(x = x + direction.x, y = y + direction.y)
  operator fun minus(direction: Direction) = Coordinate(x = x - direction.x, y = y - direction.y)
}

enum class Direction(val x: Int, val y: Int) {
  N(x = 0, y = -1),
  E(x = 1, y = 0),
  S(x = 0, y = 1),
  W(x = -1, y = 0)
}

private fun readInput(): Triple<Coordinate, Coordinate, List<Coordinate>> {
  val corruptedMemoryCoordinates: List<Coordinate> =
    File("src/main/kotlin/day18/input.txt").readLines()
      .mapNotNull { it.trim().ifBlank { null } }
      .map { line -> line.split(",").let { Coordinate(x = it.first().toInt(), y = it.last().toInt()) } }

  return Triple(Coordinate(0, 0), Coordinate(70, 70), corruptedMemoryCoordinates)
}
