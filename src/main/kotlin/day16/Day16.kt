package day16

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs
import kotlin.math.min

// https://adventofcode.com/2024/day/16
fun main() {
  val (start: Coordinate, target: Coordinate, map: Map<Coordinate, Char>) = readInput()

  // Part 1
  val bestPath: Path = getBestPath(
    startCoordinateAndDirection = start to Direction.E,
    targetCoordinateAndDirection = target to null, // null means any direction when you reach the target is acceptable
    map = map,
  )!!
  println("Part 1 lowest possible score: ${bestPath.distanceTraveled}")
  check(bestPath.distanceTraveled == 98484)

  // Part 2
  val coordinatesOnBestPath: MutableSet<Coordinate> =
    ConcurrentHashMap.newKeySet<Coordinate>().apply { addAll(bestPath.coordinates()) }
  val memoizationMap: MutableMap<Coordinate, Boolean> =
    ConcurrentHashMap(bestPath.coordinates().associateWith { true })

  fun Coordinate.unMemoizedIsOnOneBestPath(): Boolean {
    return List(4) { this }.zip(Direction.entries)
      .filter { (coordinate, direction) -> map[coordinate - direction] != '#' }
      .any { intermediateTarget ->
        val startToMid = getBestPath(
          start to Direction.E,
          targetCoordinateAndDirection = intermediateTarget,
          map,
        )?.distanceTraveled ?: return false
        val midToEnd = getBestPath(
          startCoordinateAndDirection = intermediateTarget,
          targetCoordinateAndDirection = target to null,
          map,
        )?.distanceTraveled ?: return false
        startToMid + midToEnd == bestPath.distanceTraveled
      }
  }

  fun Coordinate.isOnOneBestPath(): Boolean {
    if (map[this] == '#') return false
    memoizationMap[this]?.let { return it }
    return unMemoizedIsOnOneBestPath().also { memoizationMap[this] = it }
  }

  while (true) {
    val sizeBefore = coordinatesOnBestPath.size
    val neighbors = coordinatesOnBestPath
      .flatMap { coordinate -> coordinate.neighbors() }
      .distinct()
      .filter { map[it] != null && map[it] != '#' }
      .filter { it !in memoizationMap }
    runBlocking(Dispatchers.Default) {
      neighbors
        .map { it to async { it.isOnOneBestPath() } }
        .forEach { (coordinate, isOnBestPathDeferred) ->
          if (isOnBestPathDeferred.await()) {
            coordinatesOnBestPath.add(coordinate)
          }
        }
    }
    val sizeAfter = coordinatesOnBestPath.size
    if (sizeBefore == sizeAfter) {
      break
    }
  }
  println("part 2 num coordinates on one of the best paths: ${coordinatesOnBestPath.size}")
  check(coordinatesOnBestPath.size == 531)
}

fun getBestPath(
  startCoordinateAndDirection: Pair<Coordinate, Direction>,
  targetCoordinateAndDirection: Pair<Coordinate, Direction?>,
  map: Map<Coordinate, Char>,
): Path? {
  val (start, startDirection) = startCoordinateAndDirection
  val (target, targetDirection) = targetCoordinateAndDirection
  val queue: PriorityQueue<Path> = PriorityQueue { a: Path, b: Path ->
    a.heuristicDistanceTo(targetCoordinateAndDirection) - b.heuristicDistanceTo(targetCoordinateAndDirection)
    if (a.heuristicDistanceTo(targetCoordinateAndDirection) != b.heuristicDistanceTo(targetCoordinateAndDirection)) {
      a.heuristicDistanceTo(targetCoordinateAndDirection) - b.heuristicDistanceTo(targetCoordinateAndDirection)
    } else {
      b.steps.size - a.steps.size
    }
  }
  queue.add(
    Path(
      coordinate = start,
      direction = startDirection,
      start = start,
      steps = emptyList(),
      distanceTraveled = 0,
    )
  )

  val addedScores = mutableMapOf(startCoordinateAndDirection to 0)
  while (queue.isNotEmpty()) {
    val search = queue.remove()
    if (search.coordinate == target && (targetDirection == null || targetDirection == search.direction)) {
      return search
    }

    for (nextStep in listOf(TurnLeft, TurnRight, DirectionStep(search.direction))) {
      val nextCoordinate = when (nextStep) {
        is DirectionStep -> search.coordinate + nextStep.direction
        TurnLeft -> search.coordinate
        TurnRight -> search.coordinate
      }
      if (map[nextCoordinate] == '#') continue

      val nextDirection = when (nextStep) {
        is DirectionStep -> search.direction
        TurnLeft -> search.direction.turnLeft()
        TurnRight -> search.direction.turnRight()
      }

      val nextDistanceTraveled = search.distanceTraveled + when (nextStep) {
        is DirectionStep -> 1
        TurnLeft, TurnRight -> 1000
      }
      if (addedScores[nextCoordinate to nextDirection]?.let { nextDistanceTraveled >= it } == true) {
        // We've found path that arrives at the same coordinate heading in the same direction with less cost
        continue
      }

      queue.add(
        Path(
          coordinate = nextCoordinate,
          direction = nextDirection,
          start = search.start,
          steps = search.steps + nextStep,
          distanceTraveled = nextDistanceTraveled,
        ).also {
          addedScores[nextCoordinate to nextDirection] = it.distanceTraveled
        }
      )
    }
  }
  return null
}

sealed class PathStep
data object TurnRight : PathStep()
data object TurnLeft : PathStep()
data class DirectionStep(val direction: Direction) : PathStep()

data class Path(
  val coordinate: Coordinate,
  val direction: Direction,
  val start: Coordinate,
  val steps: List<PathStep>,
  val distanceTraveled: Int,
) {

  fun coordinates(): List<Coordinate> = buildList {
    var currCoordinate = start
    add(currCoordinate)
    steps.filterIsInstance<DirectionStep>().forEach {
      currCoordinate += it.direction
      this@buildList.add(currCoordinate)
    }
  }

  fun heuristicDistanceTo(target: Pair<Coordinate, Direction?>): Int {
    return distanceTraveled + when {
      coordinate == target.first -> {
        pointsToTurnFrom(direction, to = target.second)
      }
      coordinate.x == target.first.x -> {
        val yDirectionToTargetCoordinate = if (target.first.y > coordinate.y) Direction.S else Direction.N
        pointsToTurnFrom(direction, to = yDirectionToTargetCoordinate) +
          abs(coordinate.y - target.first.y) +
          pointsToTurnFrom(yDirectionToTargetCoordinate, to = target.second)
      }
      coordinate.y == target.first.y -> {
        val xDirectionToTargetCoordinate = if (target.first.x > coordinate.x) Direction.E else Direction.W
        pointsToTurnFrom(direction, to = xDirectionToTargetCoordinate) +
          abs(coordinate.x - target.first.x) +
          pointsToTurnFrom(xDirectionToTargetCoordinate, to = target.second)
      }
      else -> {
        val xDirectionToTargetCoordinate = if (target.first.x > coordinate.x) Direction.E else Direction.W
        val yDirectionToTargetCoordinate = if (target.first.y > coordinate.y) Direction.S else Direction.N
        abs(coordinate.x - target.first.x) + abs(coordinate.y - target.first.y) + min(
          pointsToTurnFrom(direction, to = xDirectionToTargetCoordinate) +
            pointsToTurnFrom(xDirectionToTargetCoordinate, to = yDirectionToTargetCoordinate) +
            pointsToTurnFrom(yDirectionToTargetCoordinate, to = target.second),
          pointsToTurnFrom(direction, to = yDirectionToTargetCoordinate) +
            pointsToTurnFrom(yDirectionToTargetCoordinate, to = xDirectionToTargetCoordinate) +
            pointsToTurnFrom(xDirectionToTargetCoordinate, to = target.second)
        )
      }
    }
  }
}

fun pointsToTurnFrom(direction: Direction, to: Direction?): Int =
  if (to == null) 0 else when (direction) {
    to -> 0
    -to -> 2000
    else -> 1000
  }

data class Coordinate(val x: Int, val y: Int) {
  operator fun plus(direction: Direction) = Coordinate(x = x + direction.x, y = y + direction.y)
  operator fun minus(direction: Direction) = Coordinate(x = x - direction.x, y = y - direction.y)
  fun neighbors(): List<Coordinate> = Direction.entries.map { this + it }
}

enum class Direction(val x: Int, val y: Int) {
  N(x = 0, y = -1),
  E(x = 1, y = 0),
  S(x = 0, y = 1),
  W(x = -1, y = 0);

  operator fun unaryMinus(): Direction =
    when (this) {
      N -> S; E -> W; S -> N; W -> E
    }

  fun turnLeft(): Direction =
    when (this) {
      N -> W; E -> N; S -> E; W -> S
    }

  fun turnRight(): Direction =
    when (this) {
      N -> E; E -> S; S -> W; W -> N
    }
}

private fun readInput(): Triple<Coordinate, Coordinate, Map<Coordinate, Char>> {
  val input = File("src/main/kotlin/day16/input.txt").readLines()
    .mapNotNull { it.trim().ifBlank { null } }

  lateinit var start: Coordinate
  lateinit var target: Coordinate
  val map: Map<Coordinate, Char> = buildMap {
    input.forEachIndexed { y, line ->
      line.forEachIndexed { x, char ->
        if (char == 'S') start = Coordinate(x, y)
        if (char == 'E') target = Coordinate(x, y)
        this@buildMap.put(Coordinate(x, y), char)
      }
    }
  }
  return Triple(start, target, map)
}
