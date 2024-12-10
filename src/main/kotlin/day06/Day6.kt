package day06

import java.io.File

// https://adventofcode.com/2024/day/6
fun main() {
  val (originalStart: Coordinate, grid: Grid) = readInput()

  // Part 1
  val path: List<Pair<Coordinate, Direction>> = grid.getGuardPath(originalStart to Direction.N).filterNotNull()
  val numUniqueCoordinatesVisited = path.distinctBy { (coordinate, _) -> coordinate }.count()
  println("Part 1 visited $numUniqueCoordinatesVisited unique coordinates")
  check(numUniqueCoordinatesVisited == 4819)

  // Part 2
  //   1. Obstacles off of the guard's original path would not affect his path, so we only need to consider
  //      placing obstacles at coordinates where the guard walks on his original path.
  //   2. Since the guard will follow the same path until the point where the new obstacle is placed, we don't
  //      need to restart the simulation from the starting point each time. Instead, we can start from the
  //      coordinate just before the new obstacle to determine if that path will end in a loop.
  val numObstaclesThatForceGuardIntoLoopNew = path
    .zipWithNext { startCoordinateAndDirection, newObstacleCoordinateAndDirection ->
      startCoordinateAndDirection to newObstacleCoordinateAndDirection.first
    }
    .distinctBy { (_, newObstacleCoordinate) -> newObstacleCoordinate }
    .count { (startCoordinateAndDirection, newObstacleCoordinate) ->
      grid.copyWithObstacleAt(newObstacleCoordinate)
        .getGuardPath(startCoordinateAndDirection)
        .isLoop()
    }
  println("Part 2 num obstacles that could force guard into loop: $numObstaclesThatForceGuardIntoLoopNew")
  check(numObstaclesThatForceGuardIntoLoopNew == 1796)
}

typealias Path = List<Pair<Coordinate, Direction>?>

data class Coordinate(val x: Int, val y: Int)

enum class Direction(val x: Int, val y: Int) {
  N(x = 0, y = -1),
  E(x = 1, y = 0),
  S(x = 0, y = 1),
  W(x = -1, y = 0),
}

class Grid(private val input: List<String>) {
  operator fun get(coordinate: Coordinate): Char? {
    val line = input.getOrNull(coordinate.y) ?: return null
    return line.getOrNull(coordinate.x)
  }

  fun getGuardPath(start: Pair<Coordinate, Direction>): Path {
    val (startLocation: Coordinate, startDirection: Direction) = start
    val path = mutableListOf<Pair<Coordinate, Direction>?>(Pair(startLocation, startDirection))
    val traveledSet = mutableSetOf(Pair(startLocation, startDirection))
    var location = startLocation
    var direction = startDirection
    while (true) {
      when (this[location + direction]) {
        '#' -> direction = direction.turnClockwise()
        null -> {
          path.add(null)
          break
        }
        else -> {
          location += direction
          val locationAndDirection = Pair(location, direction)
          path.add(locationAndDirection)
          if (locationAndDirection in traveledSet) {
            return path // loop
          }
          traveledSet.add(locationAndDirection)
        }
      }
    }
    return path
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

// Ending with null means the path went off-grid, so non-null means the guard looped back to a
// coordinate and direction where it had been before.
fun Path.isLoop(): Boolean = last() != null

fun Direction.turnClockwise(): Direction =
  when (this) {
    Direction.N -> Direction.E
    Direction.E -> Direction.S
    Direction.S -> Direction.W
    Direction.W -> Direction.N
  }

private operator fun Coordinate.plus(direction: Direction) = Coordinate(x = x + direction.x, y = y + direction.y)

private fun readInput(): Pair<Coordinate, Grid> {
  val lines = File("src/main/kotlin/day06/input.txt").readLines()
    .mapNotNull { it.trim().ifBlank { null } }
  val grid = Grid(lines)
  val start: Coordinate = lines.withIndex().firstNotNullOf { (lineNumber, line) ->
    line.withIndex().firstNotNullOfOrNull { (offset, char) ->
      if (char == '^') Coordinate(x = offset, y = lineNumber) else null
    }
  }
  return Pair(start, grid)
}
