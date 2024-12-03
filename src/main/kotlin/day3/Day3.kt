package day3

import java.io.File

private val mulRegex = Regex("""mul\((?<arg1>\d{1,3}),(?<arg2>\d{1,3})\)""")
private val enableMulRegex = Regex("""do\(\)""")
private val disableMulRegex = Regex("""don't\(\)""")

// https://adventofcode.com/2024/day/3
fun main() {
  val input: String = readInput()

  // Part 1
  val mulCommands: Sequence<MulCommand> = mulRegex.findAll(input)
    .map { matchResult ->
      MulCommand(
        product = matchResult.groups["arg1"]!!.value.toLong() * matchResult.groups["arg2"]!!.value.toLong(),
        range = matchResult.range,
      )
    }
  println("Part 1 sum of products: ${mulCommands.sumOf { it.product }}") // 167650499

  // Part 2
  val enableMulCommands = enableMulRegex.findAll(input).map { EnableMulCommand(range = it.range) }
  val disableMulCommands = disableMulRegex.findAll(input).map { DisableMulCommand(range = it.range) }
  val commands: Sequence<RecognizedCommand> = (mulCommands + enableMulCommands + disableMulCommands)
    .sortedBy { it.range.first }

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
  abstract val range: IntRange
}

data class MulCommand(val product: Long, override val range: IntRange) : RecognizedCommand()
data class EnableMulCommand(override val range: IntRange) : RecognizedCommand()
data class DisableMulCommand(override val range: IntRange) : RecognizedCommand()

private fun readInput(): String {
  return File("src/main/kotlin/day3/input.txt").readText()
}