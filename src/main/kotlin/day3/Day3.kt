package day3

import java.io.File

private val mulRegex = Regex("mul\\((\\d{1,3}),(\\d{1,3})\\)")
private val enableMulRegex = Regex("do\\(\\)")
private val disableMulRegex = Regex("don't\\(\\)")

// https://adventofcode.com/2024/day/3
fun main() {
  val input: List<String> = readInput()

  // Part 1
  val mulCommands: List<MulCommand> = input.flatMapIndexed { lineNumber, line ->
    mulRegex.findAll(line).map { matchResult ->
      MulCommand(
        product = matchResult.groupValues[1].toLong() * matchResult.groupValues[2].toLong(),
        line = lineNumber,
        range = matchResult.range,
      )
    }
  }
  println("Part 1 sum of products: ${mulCommands.sumOf { it.product }}") // 167650499

  // Part 2
  val enableMulCommands = input.flatMapIndexed { lineNumber, line ->
    enableMulRegex.findAll(line).map { EnableMulCommand(line = lineNumber, range = it.range) }
  }
  val disableMulCommands = input.flatMapIndexed { lineNumber, line ->
    disableMulRegex.findAll(line).map { DisableMulCommand(line = lineNumber, range = it.range) }
  }
  val commands: List<RecognizedCommand> = (mulCommands + enableMulCommands + disableMulCommands)
    .sortedWith(compareBy<RecognizedCommand> { it.line }.thenBy { it.range.first })

  var enabled = true
  val sumOfEnabledProducts = commands.sumOf { command ->
    when (command) {
      is DisableMulCommand -> enabled = false
      is EnableMulCommand -> enabled = true
      is MulCommand -> if (enabled) return@sumOf command.product
    }
    0L
  }
  println("Part 2 sum of enabled products: $sumOfEnabledProducts") // 95846796
}

sealed class RecognizedCommand {
  abstract val line: Int
  abstract val range: IntRange
}

data class MulCommand(val product: Long, override val line: Int, override val range: IntRange) : RecognizedCommand()
data class EnableMulCommand(override val line: Int, override val range: IntRange) : RecognizedCommand()
data class DisableMulCommand(override val line: Int, override val range: IntRange) : RecognizedCommand()

private fun readInput(): List<String> {
  return File("src/main/kotlin/day3/input.txt").readLines()
    .filter { it.isNotBlank() }
}