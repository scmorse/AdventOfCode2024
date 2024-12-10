package day10

import java.io.File

// https://adventofcode.com/2024/day/10
fun main() {
  val terrainGrid: List<List<Terrain>> = readInput()

  // Part 1
  val trailHeads = terrainGrid.flatMap { line -> line.filter { it.elevation == 0 } }
  val sumOfTrailheadScores = trailHeads.sumOf { trailHead ->
    terrainGrid.findAllTrails(trailHead = trailHead).distinctBy { it.last() }.count()
  }
  println("Part 1 sum of trailhead scores: $sumOfTrailheadScores")
  check(sumOfTrailheadScores == 644)

  // Part 2
  val sumOfTrailheadRatings = trailHeads.sumOf { trailHead ->
    terrainGrid.findAllTrails(trailHead = trailHead).count()
  }
  println("Part 2 sum of trailhead ratings: $sumOfTrailheadRatings")
  check(sumOfTrailheadRatings == 1366)
}

fun List<List<Terrain>>.findAllTrails(
  trailHead: Terrain,
): List<List<Terrain>> {
  val terrainGrid = this@findAllTrails
  var candidateTrails = listOf(listOf(trailHead))
  (1..9).forEach { nextStepElevation ->
    candidateTrails = candidateTrails.pairedWithEach(Direction.entries).mapNotNull { (candidateTrail, direction) ->
      val nextStepOnTrail = terrainGrid[candidateTrail.last().location + direction] ?: return@mapNotNull null
      if (nextStepOnTrail.elevation != nextStepElevation) return@mapNotNull null
      candidateTrail + nextStepOnTrail
    }
  }
  return candidateTrails
}

fun <T, U> List<T>.pairedWithEach(other: List<U>): List<Pair<T, U>> =
  flatMap { t -> other.map { u -> t to u } }

data class Terrain(val elevation: Int, val location: Coordinate)

data class Coordinate(val x: Int, val y: Int)

enum class Direction(val x: Int, val y: Int) {
  N(x = 0, y = -1),
  E(x = 1, y = 0),
  S(x = 0, y = 1),
  W(x = -1, y = 0),
}

operator fun Coordinate.plus(direction: Direction) = Coordinate(x = x + direction.x, y = y + direction.y)

operator fun List<List<Terrain>>.get(coordinate: Coordinate): Terrain? =
  getOrNull(coordinate.y)?.getOrNull(coordinate.x)

private fun readInput(): List<List<Terrain>> {
  return File("src/main/kotlin/day10/input.txt").readLines()
    .mapNotNull { it.trim().ifBlank { null } }
    .mapIndexed { y, line ->
      line.mapIndexed { x, char ->
        Terrain(elevation = char.digitToInt(), location = Coordinate(x, y))
      }
    }
}
