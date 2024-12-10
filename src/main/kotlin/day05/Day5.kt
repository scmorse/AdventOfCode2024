package day05

import java.io.File

// https://adventofcode.com/2024/day/5
fun main() {
  val (rules: Set<Rule>, manuals: List<Manual>) = readInput()

  // Part 1
  val (validManuals, invalidManuals) = manuals.partition { manual -> !manual.breaksAnyRule(rules) }
  val sumOfMidpointsOfValidManuals = validManuals.sumOf { manual -> manual.midpoint() }
  println("Part 1 sum of midpoints of valid manuals: $sumOfMidpointsOfValidManuals")
  check(sumOfMidpointsOfValidManuals == 4790L)

  // Part 2
  val sumOfMidpointsOfInvalidManuals = invalidManuals.sumOf { manual ->
    manual.sortedWith(rules.toComparator()).midpoint()
  }
  println("Part 2 sum of midpoints of invalid manuals: $sumOfMidpointsOfInvalidManuals")
  check(sumOfMidpointsOfInvalidManuals == 6319L)
}

data class Rule(val before: String, val after: String)
typealias Manual = List<String>

fun Manual.breaksAnyRule(rules: Set<Rule>): Boolean {
  for ((i, manualBefore) in withIndex()) {
    for (manualAfter in slice((i + 1)..lastIndex)) {
      if (rules.contains(Rule(before = manualAfter, after = manualBefore))) return true
    }
  }
  return false
}

fun Set<Rule>.toComparator(): Comparator<String> =
  Comparator { a, b ->
    if (contains(Rule(before = a, after = b))) -1
    else if (contains(Rule(before = b, after = a))) 1
    else 0
  }

fun Manual.midpoint(): Long = this[size / 2].toLong()

private fun readInput(): Pair<Set<Rule>, List<Manual>> {
  val rules = mutableSetOf<Rule>()
  val manuals = mutableListOf<Manual>()
  File("src/main/kotlin/day05/input.txt").readLines()
    .mapNotNull { it.trim().ifBlank { null } }
    .forEach { line ->
      if (line.contains("|")) {
        rules.add(line.split("|").let { (before, after) -> Rule(before, after) })
      } else {
        manuals.add(line.split(",").also { check(it.size % 2 == 1) })
      }
    }
  return Pair(rules, manuals)
}
