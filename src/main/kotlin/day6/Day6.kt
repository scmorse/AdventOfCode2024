package day5

import java.io.File

fun main() {
  val input: List<String> = readInput()

  // Step 1

  // Step 2
}

private fun readInput(): List<String> {
  return File("src/main/kotlin/day6/input.txt").readLines()
    .map { it.trim() }
    .filter { it.isNotBlank() }
}
