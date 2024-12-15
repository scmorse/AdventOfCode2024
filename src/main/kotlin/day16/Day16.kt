package day16

import java.io.File

// https://adventofcode.com/2024/day/16
fun main() {
  val input: List<String> = readInput()

  // Part 1

  // Part 2
}

private fun readInput(): List<String> {
  return File("src/main/kotlin/day16/input.txt").readLines()
    .mapNotNull { it.trim().ifBlank { null } }
}
