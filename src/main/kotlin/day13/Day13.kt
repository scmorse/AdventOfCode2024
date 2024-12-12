package day13

import java.io.File

// https://adventofcode.com/2024/day/13
fun main() {
  val input: List<String> = readInput()

  // Part 1

  // Part 2
}

private fun readInput(): List<String> {
  return File("src/main/kotlin/day13/input.txt").readLines()
    .mapNotNull { it.trim().ifBlank { null } }
}
