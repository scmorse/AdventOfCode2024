package day14

import java.io.File
import kotlin.math.abs

// https://adventofcode.com/2024/day/14
fun main() {
  val input: List<Robot> = readInput()
  val grid = Xy(x = 101, y = 103)

  // Part 1
  val numRobotsByQuadrantAfter100Steps = input
    .map { (start, step) -> (start + step * 100) % grid }
    .filter { robotLocation -> robotLocation.isNotOnQuadrantCross(grid) }
    .groupingBy { robotLocation -> robotLocation.getQuadrantIn(grid) }
    .eachCount()
  val safetyFactor = numRobotsByQuadrantAfter100Steps.values.fold(1) { acc, count -> acc * count }
  println("Part 1 safety factor: $safetyFactor")
  check(safetyFactor == 236628054)

  // Part 2
  var lowestProximityScoreSoFar = Int.MAX_VALUE
  repeat(Int.MAX_VALUE) { seconds ->
    val locs = input.map { (start, step) -> (start + step * seconds) % grid }
    // lower proximity score means the robot locations are more grouped, as they would be in a drawing
    val proximityScore = locs.sumOf { loc -> loc.distToClosest(locs) }
    if (proximityScore < lowestProximityScoreSoFar) {
      lowestProximityScoreSoFar = proximityScore
      println("Grid after $seconds seconds:")
      print(grid, locs)
      println()
    }
  }
}

// Part 1

private fun Xy.isNotOnQuadrantCross(grid: Xy): Boolean = x != grid.x / 2 && y != grid.y / 2

private fun Xy.getQuadrantIn(grid: Xy): Pair<Int, Int> =
  Pair(x / (grid.x / 2 + 1), y / (grid.y / 2 + 1))

// Part 2

fun Xy.distToClosest(otherRobotLocations: List<Xy>): Int {
  val robotLocation = this
  return otherRobotLocations
    .filter { it != robotLocation }
    .minBy { it distTo robotLocation } distTo robotLocation
}

fun print(grid: Xy, robots: List<Xy>) {
  val countByLocation = robots.groupingBy { it }.eachCount()
  repeat(grid.y) { y ->
    println((0..<grid.x).joinToString("") { x -> countByLocation[Xy(x, y)]?.toString() ?: "." })
  }
}

// Common

data class Robot(val start: Xy, val step: Xy)

data class Xy(val x: Int, val y: Int) {
  operator fun plus(other: Xy) = Xy(x = x + other.x, y = y + other.y)
  operator fun minus(other: Xy) = Xy(x = x - other.x, y = y - other.y)
  operator fun times(other: Int) = Xy(x = x * other, y = y * other)
  operator fun rem(other: Xy) = Xy(x = Math.floorMod(x, other.x), y = Math.floorMod(y, other.y))
  infix fun distTo(other: Xy) = abs(x - other.x) + abs(y - other.y)
}

private fun readInput(): List<Robot> {
  return File("src/main/kotlin/day14/input.txt").readLines()
    .mapNotNull { it.trim().ifBlank { null } }
    .map { line ->
      val (startX, startY) = line.substringAfter("p=").substringBefore(" v=").split(",").map { it.toInt() }
      val (stepX, stepY) = line.substringAfter("v=").split(",").map { it.toInt() }
      Robot(
        start = Xy(x = startX, y = startY),
        step = Xy(x = stepX, y = stepY),
      )
    }
}
