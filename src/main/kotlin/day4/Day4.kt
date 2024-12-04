package day4

import java.io.File

private val SEARCH_DIRECTIONS = listOf(
  Coordinate(-1, -1), Coordinate(-1, 0), Coordinate(-1, 1),
  Coordinate(0, -1), /* Coordinate(0, 0) */ Coordinate(0, 1),
  Coordinate(1, -1), Coordinate(1, 0), Coordinate(1, 1),
)

data class Coordinate(val x: Int, val y: Int)

// https://adventofcode.com/2024/day/4
fun main() {
  val input: List<String> = readInput()

  // Part 1
  var countOfXMAS = 0
  for ((lineNumber, line) in input.withIndex()) {
    for ((offset, char) in line.withIndex()) {
      if (char != 'X') continue
      val start = Coordinate(x = offset, y = lineNumber)
      for (searchDirection in SEARCH_DIRECTIONS) {
        if (input.getAt(start + searchDirection * 1) != 'M') continue
        if (input.getAt(start + searchDirection * 2) != 'A') continue
        if (input.getAt(start + searchDirection * 3) != 'S') continue
        countOfXMAS++
      }
    }
  }
  println("Part 1 count of XMAS: $countOfXMAS")
  check(countOfXMAS == 2500)

  // Part 2
  var countOfXShapedMAS = 0
  for ((lineNumber, line) in input.withIndex()) {
    for ((offset, char) in line.withIndex()) {
      if (char != 'A') continue
      val middle = Coordinate(x = offset, y = lineNumber)
      if (input.getAdjacentCharsOnAxis(middle, axisX = 1, axisY = 1) != setOf('M', 'S')) continue
      if (input.getAdjacentCharsOnAxis(middle, axisX = 1, axisY = -1) != setOf('M', 'S')) continue
      countOfXShapedMAS++
    }
  }
  println("Part 2 count of MAS in X shape: $countOfXShapedMAS")
  check(countOfXShapedMAS == 1933)
}

private fun List<String>.getAdjacentCharsOnAxis(start: Coordinate, axisX: Int, axisY: Int): Set<Char> =
  setOfNotNull(
    this.getAt(Coordinate(x = start.x + axisX, y = start.y + axisY)),
    this.getAt(Coordinate(x = start.x + axisX * -1, y = start.y + axisY * -1)),
  )

private operator fun Coordinate.times(other: Int) = Coordinate(x = x * other, y = y * other)

private operator fun Coordinate.plus(other: Coordinate) = Coordinate(x = x + other.x, y = y + other.y)

private fun List<String>.getAt(coordinate: Coordinate): Char? {
  val line = this.getOrNull(coordinate.y) ?: return null
  return line.getOrNull(coordinate.x)
}


private fun readInput(): List<String> {
  return File("src/main/kotlin/day4/input.txt").readLines()
    .map { it.trim() }
    .filter { it.isNotBlank() }
}