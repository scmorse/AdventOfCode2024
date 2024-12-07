package day7

import java.io.File

// https://adventofcode.com/2024/day/7
fun main() {
  val input: List<Problem> = readInput()

  // Part 1
  val sumOfTargetsInPt1SolvableProblems = input
    .filter { problem -> isProblemSolvablePart1(problem.target, problem.subjects.first(), problem.subjects.drop(1)) }
    .sumOf { it.target }
  println("Part 1 sum of targets in solvable problems: $sumOfTargetsInPt1SolvableProblems")
  check(sumOfTargetsInPt1SolvableProblems == 66343330034722L)

  // Part 2
  val sumOfTargetsInPt2SolvableProblems = input
    .filter { problem -> isProblemSolvablePart2(problem.target, problem.subjects.first(), problem.subjects.drop(1)) }
    .sumOf { it.target }
  println("Part 2 sum of targets in solvable problems: $sumOfTargetsInPt2SolvableProblems")
  check(sumOfTargetsInPt2SolvableProblems == 637696070419031L)
}

fun isProblemSolvablePart1(target: Long, accumulator: Long, subjects: List<Long>): Boolean {
  if (accumulator == target) return true
  if (accumulator > target || subjects.isEmpty()) return false

  val rightHandSide = subjects.first()
  val nextSubjects = subjects.drop(1)
  if (isProblemSolvablePart1(target, accumulator * rightHandSide, nextSubjects)) return true
  if (isProblemSolvablePart1(target, accumulator + rightHandSide, nextSubjects)) return true
  return false
}

fun isProblemSolvablePart2(target: Long, accumulator: Long, subjects: List<Long>): Boolean {
  if (accumulator == target) return true
  if (accumulator > target || subjects.isEmpty()) return false

  val rightHandSide = subjects.first()
  val nextSubjects = subjects.drop(1)
  if (isProblemSolvablePart2(target, accumulator * rightHandSide, nextSubjects)) return true
  if (isProblemSolvablePart2(target, accumulator + rightHandSide, nextSubjects)) return true
  if (isProblemSolvablePart2(target, accumulator concatAsLong rightHandSide, nextSubjects)) return true
  return false
}

infix fun Long.concatAsLong(other: Long): Long = (toString() + other.toString()).toLong()

data class Problem(val target: Long, val subjects: List<Long>)

private fun readInput(): List<Problem> {
  return File("src/main/kotlin/day7/input.txt").readLines()
    .mapNotNull { it.trim().ifBlank { null } }
    .map { line ->
      Problem(
        target = line.substringBefore(":").toLong(),
        subjects = line.substringAfter(": ").split(" ").map { it.toLong() },
      )
    }
}
