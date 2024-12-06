package day6

import java.io.File

fun main() {
  val (originalStart: Coordinate, grid: Grid) = readInput()

  // Part 1
  val traveledPath: List<Pair<Coordinate, Direction>> = grid.getGuardPath(originalStart to Direction.N).filterNotNull()
  val uniqueCoordinatesInPath = traveledPath.map { (coordinate, _) -> coordinate }.distinct()
  println("Part 1 visited ${uniqueCoordinatesInPath.size} positions")
  check(uniqueCoordinatesInPath.size == 4819)

  // Part 2
  val numObstaclesThatForceGuardIntoLoopNew = traveledPath
    .zipWithNext { startCoordinateAndDirection, newObstacleCoordinateAndDirection ->
      startCoordinateAndDirection to newObstacleCoordinateAndDirection.first
    }
    .distinctBy { (_, newObstacleCoordinate) -> newObstacleCoordinate }
    .sumOf { (startCoordinateAndDirection, newObstacleCoordinate) ->
      val path = grid.copyWithObstacleAt(newObstacleCoordinate).getGuardPath(startCoordinateAndDirection)
      if (path.last() != null) 1L else 0L
    }
  println("Part 2 num obstacles that could force guard into loop: $numObstaclesThatForceGuardIntoLoopNew")
  check(numObstaclesThatForceGuardIntoLoopNew == 1796L)
}

data class Coordinate(val x: Int, val y: Int)

enum class Direction(val x: Int, val y: Int) {
  N(x = 0, y = -1),
  E(x = 1, y = 0),
  S(x = 0, y = 1),
  W(x = -1, y = 0),
}

fun Direction.turnClockwise(): Direction =
  when (this) {
    Direction.N -> Direction.E
    Direction.E -> Direction.S
    Direction.S -> Direction.W
    Direction.W -> Direction.N
  }

class Grid(private val input: List<String>) {
  operator fun get(coordinate: Coordinate): Char? {
    val line = input.getOrNull(coordinate.y) ?: return null
    return line.getOrNull(coordinate.x)
  }

  fun getGuardPath(start: Pair<Coordinate, Direction>): List<Pair<Coordinate, Direction>?> {
    val (startLocation: Coordinate, startDirection: Direction) = start
    val traveled = mutableListOf<Pair<Coordinate, Direction>?>(Pair(startLocation, startDirection))
    val traveledSet = mutableSetOf(Pair(startLocation, startDirection))
    var location = startLocation
    var direction = startDirection
    while (true) {
      when (this[location + direction]) {
        '#' -> direction = direction.turnClockwise()
        null -> {
          traveled.add(null)
          break
        }
        else -> {
          location += direction
          val locationAndDirection = Pair(location, direction)
          traveled.add(locationAndDirection)
          if (locationAndDirection in traveledSet) {
            return traveled // loop
          } else {
            traveledSet.add(locationAndDirection)
          }
        }
      }
    }
    return traveled
  }

  fun copyWithObstacleAt(coordinate: Coordinate): Grid {
    check(this[coordinate] == '.')
    return Grid(
      input.mapIndexed { lineNumber, line ->
        if (lineNumber == coordinate.y) {
          line.replaceRange(coordinate.x..coordinate.x, "#")
        } else line
      }
    )
  }

}

private operator fun Coordinate.plus(direction: Direction) = Coordinate(x = x + direction.x, y = y + direction.y)

private fun readInput(): Pair<Coordinate, Grid> {
  val lines = File("src/main/kotlin/day6/input.txt").readLines()
    .mapNotNull { it.trim().ifBlank { null } }
  val grid = Grid(lines)
  val start: Coordinate = lines.withIndex().firstNotNullOf { (lineNumber, line) ->
    line.withIndex().firstNotNullOfOrNull { (offset, char) ->
      if (char == '^') Coordinate(x = offset, y = lineNumber) else null
    }
  }
  return Pair(start, grid)
}
