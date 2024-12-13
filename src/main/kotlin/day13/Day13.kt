package day13

import java.io.File

// https://adventofcode.com/2024/day/13
fun main() {
  val input: List<ClawMachine> = readInput()

  // Part 1
  val numTokensToWinPrizesForPart1 = input.sumOf { clawMachine ->
    (0..100L).forEach { numButtonAPushes ->
      val remaining = clawMachine.prize - clawMachine.buttonA * numButtonAPushes
      if (remaining.x < 0 || remaining.y < 0) return@sumOf 0L
      (remaining / clawMachine.buttonB)?.let {
        if (it.x >= 0 && it.x == it.y) return@sumOf 3 * numButtonAPushes + it.x
      }
    }
    0L
  }
  println("Part 1 min tokens to win prizes: $numTokensToWinPrizesForPart1")
  check(numTokensToWinPrizesForPart1 == 28138L)

  // Part 2
  val numTokensToWinPrizesForPart2 = input
    .map { it.copy(prize = it.prize + Xy(10000000000000L, 10000000000000L)) }
    .sumOf { (buttonA, buttonB, prize) ->
      val numAsNumerator = prize * buttonB.inv()
      val numAsDenominator = buttonA * buttonB.inv()
      if (numAsNumerator % numAsDenominator != 0L) return@sumOf 0L

      val numBsNumerator = prize * buttonA.inv()
      val numBsDenominator = buttonB * buttonA.inv()
      if (numBsNumerator % numBsDenominator != 0L) return@sumOf 0L

      val numAs = numAsNumerator / numAsDenominator
      val numBs = numBsNumerator / numBsDenominator

      numAs * 3 + numBs
    }
  println("Part 2 min tokens to win prizes: $numTokensToWinPrizesForPart2")
  check(numTokensToWinPrizesForPart2 == 108394825772874L)
}

data class Xy(val x: Long, val y: Long) {
  operator fun plus(other: Xy) = Xy(x = x + other.x, y = y + other.y)
  operator fun minus(other: Xy) = Xy(x = x - other.x, y = y - other.y)
  operator fun times(other: Long) = Xy(x = x * other, y = y * other)
  operator fun times(other: Xy): Long = x * other.x + y * other.y
  operator fun div(other: Xy): Xy? {
    if (x % other.x != 0L) return null
    if (y % other.y != 0L) return null
    return Xy(x = x / other.x, y = y / other.y)
  }

  fun inv() = Xy(x = y, y = -x)
}

data class ClawMachine(
  val buttonA: Xy,
  val buttonB: Xy,
  val prize: Xy,
)

private fun readInput(): List<ClawMachine> {
  return File("src/main/kotlin/day13/input.txt").readLines()
    .mapNotNull { it.trim().ifBlank { null } }
    .windowed(3)
    .filterIndexed { i, _ -> i % 3 == 0 }
    .map { gameTextLines ->
      val (buttonAText, buttonBText, prizeText) = gameTextLines
      ClawMachine(
        buttonA = Xy(
          x = buttonAText.substringAfter("X+").substringBefore(",").toLong(),
          y = buttonAText.substringAfter("Y+").toLong()
        ),
        buttonB = Xy(
          x = buttonBText.substringAfter("X+").substringBefore(",").toLong(),
          y = buttonBText.substringAfter("Y+").toLong()
        ),
        prize = Xy(
          x = prizeText.substringAfter("X=").substringBefore(",").toLong(),
          y = prizeText.substringAfter("Y=").toLong()
        )
      )
    }
}
