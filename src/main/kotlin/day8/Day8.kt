package day8

import java.io.File

// https://adventofcode.com/2024/day/8
fun main() {
  val input: List<String> = readInput()

  val grid: Map<Coordinate, Antenna?> =
    input.flatMapIndexed { y, line ->
      line.mapIndexed { x, char ->
        Coordinate(x, y) to if (char == '.') null else Antenna(char, Coordinate(x, y))
      }
    }.toMap()
  val antennasGroupedByType: Map<Char, List<Antenna>> = grid.values.filterNotNull().groupBy { it.char }

  // Part 1
  val numAntinodeCoordinatesForPart1 = antennasGroupedByType.values
    .flatMap { antennas ->
      antennas.pairs().flatMap { (antenna1, antenna2) ->
        listOf(
          antenna1.coordinate - antenna2.coordinate + antenna1.coordinate,
          antenna2.coordinate - antenna1.coordinate + antenna2.coordinate,
        )
      }
    }
    .filter { it in grid }
    .distinct().count()
  println("Part 1 num antinodes: $numAntinodeCoordinatesForPart1")
  check(numAntinodeCoordinatesForPart1 == 357)

  // Part 2
  tailrec fun MutableList<Coordinate>.addMultiplesInGrid(candidate: Coordinate, delta: Coordinate) {
    if (candidate !in grid) return
    add(candidate)
    return addMultiplesInGrid(candidate = candidate + delta, delta)
  }

  val numAntinodeCoordinatesForPart2 = antennasGroupedByType.values
    .flatMap { coordinates ->
      coordinates.pairs().flatMap { (antenna1, antenna2) ->
        buildList {
          addMultiplesInGrid(candidate = antenna1.coordinate, antenna1.coordinate - antenna2.coordinate)
          addMultiplesInGrid(candidate = antenna2.coordinate, antenna2.coordinate - antenna1.coordinate)
        }
      }
    }
    .distinct().count()
  println("Part 2 num antinodes: $numAntinodeCoordinatesForPart2")
  check(numAntinodeCoordinatesForPart2 == 1266)
}

fun <T> List<T>.pairs(): List<Pair<T, T>> = buildList {
  for (i in this@pairs.indices) {
    for (j in (i + 1)..this@pairs.indices.last) {
      add(Pair(this@pairs[i], this@pairs[j]))
    }
  }
}

data class Antenna(val char: Char, val coordinate: Coordinate)

data class Coordinate(val x: Int, val y: Int) {
  operator fun plus(other: Coordinate): Coordinate = Coordinate(x = x + other.x, y = y + other.y)
  operator fun minus(other: Coordinate): Coordinate = Coordinate(x = x - other.x, y = y - other.y)
}

private fun readInput(): List<String> {
  return File("src/main/kotlin/day8/input.txt").readLines()
    .mapNotNull { it.trim().ifBlank { null } }
}
