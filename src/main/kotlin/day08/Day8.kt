package day08

import java.io.File

// https://adventofcode.com/2024/day/8
fun main() {
  val input: List<String> = readInput()

  // Part 1
  val grid = Grid(input)
  val numAntinodePointsForPart1 = grid.getGroupsOfAntennasWithSameChar()
    .flatMap { antennaPoints ->
      antennaPoints.pairs().flatMap { (point1, point2) ->
        listOfNotNull(
          grid.getPointsAlongLine(point1 to point2).drop(1).firstOrNull(),
          grid.getPointsAlongLine(point2 to point1).drop(1).firstOrNull(),
        )
      }
    }
    .distinct().count()
  println("Part 1 num antinodes: $numAntinodePointsForPart1")
  check(numAntinodePointsForPart1 == 357)

  // Part 2
  val numAntinodePointsForPart2 = grid.getGroupsOfAntennasWithSameChar()
    .flatMap { points ->
      points.pairs().flatMap { (point1, point2) ->
        grid.getPointsAlongLine(point1 to point2) +
          grid.getPointsAlongLine(point2 to point1)
      }
    }
    .distinct().count()
  println("Part 2 num antinodes: $numAntinodePointsForPart2")
  check(numAntinodePointsForPart2 == 1266)
}

class Grid(input: List<String>) {
  private val antennaMap: Map<Point, Char?> =
    input.flatMapIndexed { y, line ->
      line.mapIndexed { x, char -> Point(x, y) to if (char == '.') null else char }
    }.toMap()

  fun getGroupsOfAntennasWithSameChar(): Collection<List<Point>> = antennaMap.entries
    .mapNotNull { (point, char) -> char?.let { char to point } }
    .groupSecondsByFirsts().values

  fun getPointsAlongLine(line: Pair<Point, Point>): Sequence<Point> = sequence {
    yieldPointsAlongLine(line.second, line.second - line.first)
  }

  private tailrec suspend fun SequenceScope<Point>.yieldPointsAlongLine(point: Point, delta: Point) {
    if (point !in antennaMap) return else yield(point)
    return yieldPointsAlongLine(point = point + delta, delta)
  }
}

fun <T> List<T>.pairs(): List<Pair<T, T>> = buildList {
  for (i in this@pairs.indices) {
    for (j in (i + 1)..this@pairs.indices.last) {
      add(Pair(this@pairs[i], this@pairs[j]))
    }
  }
}

fun <A, B> List<Pair<A, B>>.groupSecondsByFirsts(): Map<A, List<B>> {
  return groupBy { it.first }.mapValues { (_, v) -> v.map { it.second } }
}

data class Point(val x: Int, val y: Int) {
  operator fun plus(other: Point): Point = Point(x = x + other.x, y = y + other.y)
  operator fun minus(other: Point): Point = Point(x = x - other.x, y = y - other.y)
}

private fun readInput(): List<String> {
  return File("src/main/kotlin/day08/input.txt").readLines()
    .mapNotNull { it.trim().ifBlank { null } }
}
