package day22

import java.io.File
import kotlin.time.measureTime

// https://adventofcode.com/2024/day/22
fun main() {
  val input: List<String> = readInput()

  // Part 1
  val part1Answer = input.sumOf { startingSecret ->
    getPseudoRandomSequence(startingSecret).last()
  }
  println("Part 1 sum of last pseudorandom numbers: $part1Answer")
  check(part1Answer == 17960270302L)

  // Part 2
  measureTime {
    val sumsByDiffs = Array(130_321) { SumAndIds() }
    input.forEachIndexed { id, startingSecret ->
      val seq = getPseudoRandomSequence(startingSecret).map { it.toInt() % 10 }
      var a: Int
      var b = (seq[1] - seq[0] + 9) * 361
      var c = (seq[2] - seq[1] + 9) * 19
      var d = (seq[3] - seq[2] + 9)
      (4..seq.lastIndex).forEach { i ->
        a = b * 19
        b = c * 19
        c = d * 19
        d = seq[i] - seq[i - 1] + 9
        sumsByDiffs[a + b + c + d].addIfNew(id, seq[i])
      }
    }
    val mostBananas = sumsByDiffs.maxOf { it.sum }
    println("Part 2 most bananas for any one instruction: $mostBananas")
  }.also {
    println(it)
  }
}

fun getPseudoRandomSequence(startingSecret: String): List<Long> = buildList {
  var s = startingSecret.toLong()
  add(s)
  repeat(2000) {
    s = ((s shl 6) xor s) and 16777215L
    s = ((s shr 5) xor s) and 16777215L
    s = ((s shl 11) xor s) and 16777215L
    add(s)
  }
}

class SumAndIds {
  private val ids: Array<Long> = Array(34) { 0L }
  var sum: Int = 0
  fun addIfNew(id: Int, value: Int) {
    val idx = id shr 6
    val l = ids[idx]
    val bit = 1L shl (id and 63)
    if ((l and bit) == 0L) {
      ids[idx] = (l or bit)
      sum += value
    }
  }
}

private fun readInput(): List<String> {
  return File("src/main/kotlin/day22/input.txt").readLines()
    .mapNotNull { it.trim().ifBlank { null } }
}
