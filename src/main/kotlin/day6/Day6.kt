package day5

import java.io.File

fun main() {
  val input: List<String> = readInput()

  // Part 1

  // Part 2
}

private fun readInput(): List<String> {
  return File("src/main/kotlin/day6/input.txt").readLines()
    .mapNotNull { it.trim().ifBlank { null } }
}
