package day15

import java.io.File
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

// https://adventofcode.com/2024/day/15
fun main() {
  val (gridLines: List<String>, directions: List<Direction>) = readInput()

  // Part 1
  withGrid(gridLines) { grid: Grid ->
    var robot = getRobotCoordinate()
    for (direction in directions) {
      val nextEmpty = findNextEmptyAlong(robot to direction) ?: continue
      grid[robot] = '.'
      if (nextEmpty == robot + direction) {
        grid[nextEmpty] = '@'
      } else {
        grid[nextEmpty] = 'O'
        grid[robot + direction] = '@'
      }
      robot += direction
    }

    val sumOfGpsForPart1 = sumOfGps()
    println("Part 1 sum of GPS: $sumOfGpsForPart1")
    check(sumOfGpsForPart1 == 1509863)
  }

  // Part 2
  withGrid(gridLines.widenForPart2()) { grid: Grid ->
    var robot = getRobotCoordinate()
    for (direction in directions) {
      when (direction) {
        Direction.E, Direction.W -> {
          val nextEmpty = findNextEmptyAlong(robot to direction) ?: continue
          var moving = nextEmpty - direction
          while (true) {
            val movingChar = grid[moving]
            if (movingChar == '#') break
            grid[moving + direction] = grid[moving]!!
            grid[moving] = '.'
            if (movingChar == '@') break
            moving -= direction
          }
          robot = moving + direction
        }
        Direction.N, Direction.S -> {
          val pairs = robot.getStartDestinationPairs(direction)
          pairs.forEach { (start, destination) ->
            grid[destination] = grid[start]!!
            grid[start] = '.'
          }
          if (pairs.isNotEmpty()) robot += direction
        }
      }
    }

    val sumOfGpsForPart2 = sumOfGps()
    println("Part 2 sum of GPS: $sumOfGpsForPart2")
    check(sumOfGpsForPart2 == 1548815)
  }
}

class Grid(original: List<String>) {
  private val state: MutableList<MutableList<Char>> =
    original.mapTo(mutableListOf()) { line -> line.mapTo(mutableListOf()) { it } }

  fun Coordinate.getStartDestinationPairs(direction: Direction): List<Pair<Coordinate, Coordinate>> {
    val hereCoordinate = this
    val thereCoordinate = hereCoordinate + direction
    val there = this@Grid[thereCoordinate]
    return when (there) {
      '.' -> return listOf(hereCoordinate to thereCoordinate)
      ']' -> {
        val x = (thereCoordinate).getStartDestinationPairs(direction)
          .ifEmpty { return emptyList() }
        val x2 = (thereCoordinate + Direction.W).getStartDestinationPairs(direction)
          .ifEmpty { return emptyList() }
        (x2 + x).distinct() + listOf(hereCoordinate to thereCoordinate)
      }
      '[' -> {
        val x = (thereCoordinate).getStartDestinationPairs(direction)
          .ifEmpty { return emptyList() }
        val x2 = (thereCoordinate + Direction.E).getStartDestinationPairs(direction)
          .ifEmpty { return emptyList() }
        (x + x2).distinct() + listOf(hereCoordinate to thereCoordinate)
      }
      else -> emptyList()
    }
  }

  fun getRobotCoordinate(): Coordinate {
    state.forEachIndexed { y, line ->
      line.forEachIndexed { x, char ->
        if (char == '@') return Coordinate(x, y)
      }
    }
    throw IllegalArgumentException()
  }

  fun findNextEmptyAlong(startAndDirection: Pair<Coordinate, Direction>): Coordinate? {
    val (start, direction) = startAndDirection
    var search: Coordinate = start + direction
    while (this[search] != '.' && this[search] != '#') {
      search += direction
    }
    return if (this[search] == '.') search else null
  }

  operator fun set(coordinate: Coordinate, value: Char) {
    state[coordinate.y][coordinate.x] = value
  }

  operator fun get(coordinate: Coordinate): Char? = with(state) {
    getOrNull(coordinate.y)?.getOrNull(coordinate.x)
  }

  fun sumOfGps(): Int {
    return state.withIndex().sumOf { (y, line) ->
      line.withIndex().sumOf { (x, char) -> if (char == 'O' || char == '[') 100 * y + x else 0 }
    }
  }

  fun print() {
    state.forEach { line ->
      println(line.joinToString(separator = ""))
    }
  }
}

data class Coordinate(val x: Int, val y: Int) {
  operator fun plus(direction: Direction) = Coordinate(x = x + direction.x, y = y + direction.y)
  operator fun minus(direction: Direction) = Coordinate(x = x - direction.x, y = y - direction.y)
}

enum class Direction(val x: Int, val y: Int) {
  N(x = 0, y = -1),
  E(x = 1, y = 0),
  S(x = 0, y = 1),
  W(x = -1, y = 0),
}

fun List<String>.widenForPart2(): List<String> =
  map { line ->
    line.flatMap { char ->
      when (char) {
        '#' -> listOf('#', '#')
        'O' -> listOf('[', ']')
        '.' -> listOf('.', '.')
        '@' -> listOf('@', '.')
        else -> throw IllegalArgumentException()
      }
    }.joinToString(separator = "")
  }

private fun readInput(): Pair<List<String>, List<Direction>> {
  val rawInput = File("src/main/kotlin/day15/input.txt").readLines()
    .mapNotNull { it.trim().ifBlank { null } }
  val gridLines = rawInput.takeWhile { it.startsWith('#') }
  val moves = rawInput.takeLastWhile { it[0] in "<v>^" }.joinToString(separator = "").map {
    when (it) {
      '^' -> Direction.N
      '>' -> Direction.E
      'v' -> Direction.S
      '<' -> Direction.W
      else -> throw IllegalArgumentException()
    }
  }
  return Pair(gridLines, moves)
}

@OptIn(ExperimentalContracts::class)
inline fun <T> withGrid(
  gridLines: List<String>,
  block: Grid.(grid: Grid) -> T,
) {
  contract {
    callsInPlace(block, InvocationKind.EXACTLY_ONCE)
  }
  val grid = Grid(gridLines)
  grid.block(grid)
}
