package day4

import java.io.File

// https://adventofcode.com/2024/day/4
fun main() {
  val input: List<String> = readInput()

  // Part 1
  var countOfXMAS = 0
  for ((lineNumber, line) in input.withIndex()) {
    for ((offset, char) in line.withIndex()) {
      if (char != 'X') continue
      val start = Coordinate(x = offset, y = lineNumber)
      for (direction in Direction.entries) {
        if (input[start + direction * 1] != 'M') continue
        if (input[start + direction * 2] != 'A') continue
        if (input[start + direction * 3] != 'S') continue
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
      if (input.getAdjacentCharsOnAxis(middle, Direction.NE) != setOf('M', 'S')) continue
      if (input.getAdjacentCharsOnAxis(middle, Direction.NW) != setOf('M', 'S')) continue
      countOfXShapedMAS++
    }
  }
  println("Part 2 count of MAS in X shape: $countOfXShapedMAS")
  check(countOfXShapedMAS == 1933)
}

data class Coordinate(val x: Int, val y: Int)

enum class Direction(val x: Int, val y: Int) {
  NW(x = -1, y = 1), N(x = 0, y = 1), NE(x = 1, y = 1),
  W(x = -1, y = 0), E(x = 1, y = 0),
  SW(x = -1, y = -1), S(x = 0, y = -1), SE(x = 1, y = -1),
}

data class Vector(val direction: Direction, val magnitude: Int) {
  val x: Int get() = magnitude * direction.x
  val y: Int get() = magnitude * direction.y
}

private operator fun List<String>.get(coordinate: Coordinate): Char? {
  val line = this.getOrNull(coordinate.y) ?: return null
  return line.getOrNull(coordinate.x)
}

private fun List<String>.getAdjacentCharsOnAxis(middle: Coordinate, direction: Direction): Set<Char> =
  setOfNotNull(this[middle + direction * 1], this[middle + direction * -1])

private operator fun Direction.times(steps: Int) = Vector(direction = this, magnitude = steps)
private operator fun Coordinate.plus(vector: Vector) = Coordinate(x = x + vector.x, y = y + vector.y)

private fun readInput(): List<String> {
  return File("src/main/kotlin/day4/input.txt").readLines()
    .map { it.trim() }
    .filter { it.isNotBlank() }
}