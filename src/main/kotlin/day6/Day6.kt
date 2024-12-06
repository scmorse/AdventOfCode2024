package day6

import java.io.File

fun main() {
  val (start: Coordinate, grid: Grid) = readInput()

  // Part 1
  val traveledLocations = grid.getGuardPath(start, Direction.N).filterNotNull().distinctBy { it }
  println("Part 1 visited ${traveledLocations.size} positions")
  check(traveledLocations.size == 4819)

  // Part 2
  val numObstaclesThatForceGuardIntoLoop = (traveledLocations - start)
    .sumOf { newObstacleCoordinate ->
      val path = grid.withObstacleAt(newObstacleCoordinate).getGuardPath(start, Direction.N)
      if (path.last() != null) 1L else 0L
    }
  println("Part 2 num obstacles that could force guard into loop: $numObstaclesThatForceGuardIntoLoop")
  check(numObstaclesThatForceGuardIntoLoop == 1796L)
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

  fun getGuardPath(startLocation: Coordinate, startDirection: Direction): List<Coordinate?> {
    val traveled = mutableListOf<Coordinate?>(startLocation)
    val traveledWithDirection = mutableSetOf(Pair(startLocation, startDirection))
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
          traveled.add(location)
          val locationAndDirection = Pair(location, direction)
          if (locationAndDirection in traveledWithDirection) {
            return traveled // loop
          } else {
            traveledWithDirection.add(locationAndDirection)
          }
        }
      }
    }
    return traveled
  }

  fun withObstacleAt(coordinate: Coordinate): Grid {
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
