package day12

import java.io.File

// https://adventofcode.com/2024/day/12
fun main() {
  val input: List<String> = readInput()

  // Part 1
  val gardens: List<Set<Coordinate>> = findGardens(input)
  val totalPriceForPart1 = gardens.sumOf { it.area() * it.perimeter() }
  println("Part 1 total price: $totalPriceForPart1")
  check(totalPriceForPart1 == 1415378L)

  // Part 2
  val totalPriceForPart2 = gardens.sumOf { garden -> garden.area() * garden.numSides() }
  println("Part 2 total price: $totalPriceForPart2")
  check(totalPriceForPart2 == 862714L)
}

// Part 1

private fun findGardens(input: List<String>): List<Set<Coordinate>> {
  val coordinatesNeedingPlacement: MutableSet<Coordinate> =
    input.flatMapIndexedTo(mutableSetOf()) { y, line ->
      line.indices.map { x -> Coordinate(x, y) }
    }
  val gardens = mutableListOf<Set<Coordinate>>()
  while (coordinatesNeedingPlacement.isNotEmpty()) {
    val garden = mutableSetOf(coordinatesNeedingPlacement.first())
    garden.extendWithin(input)
    garden.forEach { coordinatesNeedingPlacement.remove(it) }
    gardens.add(garden)
  }
  return gardens
}

fun MutableSet<Coordinate>.extendWithin(
  input: List<String>,
) {
  val garden = this@extendWithin
  while (true) {
    val gardenSizeBefore = garden.size
    garden.flatMap { gardenBox -> Direction.entries.map { gardenBox to gardenBox + it } }
      .forEach { (gardenBox, gardenBoxNeighbor) ->
        if (input[gardenBox] == input[gardenBoxNeighbor]) {
          garden.add(gardenBoxNeighbor)
        }
      }
    if (garden.size == gardenSizeBefore) {
      break
    }
  }
}

fun Set<Coordinate>.area() = size

fun Set<Coordinate>.perimeter(): Long = sumOf { gardenCoordinate ->
  Direction.entries.count { direction -> (gardenCoordinate + direction) !in this@perimeter }.toLong()
}

// Part 2

fun Set<Coordinate>.numSides(): Long {
  val garden = this@numSides
  val gardenEdges: MutableSet<Pair<Coordinate, Coordinate>> = garden.flatMapTo(mutableSetOf()) { gardenCoordinate ->
    Direction.entries.mapNotNull { direction ->
      val neighbor = gardenCoordinate + direction
      if (neighbor !in garden) gardenCoordinate to neighbor else null
    }
  }
  var sides = 0L
  while (gardenEdges.isNotEmpty()) {
    sides++
    val edge = gardenEdges.first()
    when {
      edge.first.x == edge.second.x -> {
        gardenEdges.removeEdgesInDirection(start = edge + Direction.E, Direction.E)
        gardenEdges.removeEdgesInDirection(start = edge, Direction.W)
      }
      else -> {
        gardenEdges.removeEdgesInDirection(start = edge + Direction.N, Direction.N)
        gardenEdges.removeEdgesInDirection(start = edge, Direction.S)
      }
    }
  }
  return sides
}

fun MutableSet<Pair<Coordinate, Coordinate>>.removeEdgesInDirection(
  start: Pair<Coordinate, Coordinate>,
  direction: Direction,
) {
  var removing = start
  while (removing in this) {
    remove(removing)
    removing += direction
  }
}

// Common

data class Coordinate(val x: Int, val y: Int)

enum class Direction(val x: Int, val y: Int) {
  N(x = 0, y = -1),
  E(x = 1, y = 0),
  S(x = 0, y = 1),
  W(x = -1, y = 0),
}

operator fun List<String>.get(coordinate: Coordinate): Char? = getOrNull(coordinate.y)?.getOrNull(coordinate.x)

private operator fun Coordinate.plus(direction: Direction) = Coordinate(x = x + direction.x, y = y + direction.y)

private operator fun Pair<Coordinate, Coordinate>.plus(direction: Direction) =
  Pair(first + direction, second + direction)

private fun readInput(): List<String> {
  return File("src/main/kotlin/day12/input.txt").readLines()
    .mapNotNull { it.trim().ifBlank { null } }
}
