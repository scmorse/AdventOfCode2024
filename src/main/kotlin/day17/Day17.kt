package day17

import java.io.File
import kotlin.math.pow

// https://adventofcode.com/2024/day/17
fun main() {
  val originalProgramState: ProgramState = readInput()

  // Part 1
  val originalProgramOutputCsv = originalProgramState.runProgram().output.joinToString(",")
  println("Part 1 output: $originalProgramOutputCsv")
  check(originalProgramOutputCsv == "7,5,4,3,4,5,3,4,6")

  // Part 2
  var inputsMatchingBits = (0L..1023L).filter {
    originalProgramState.copy(registerA = it).runProgram().output.first() == originalProgramState.program.first()
  }
  var bits = 10
  var expectedMatch = 2

  repeat(originalProgramState.program.size - 1) {
    inputsMatchingBits = inputsMatchingBits
      .flatMap { rightMostBits ->
        (0L..7L).map { newLeftMostBits ->
          (newLeftMostBits shl bits) + rightMostBits
        }
      }
      .filter {
        val output = originalProgramState.copy(registerA = it).runProgram().output
        if (output.size < expectedMatch) false else {
          originalProgramState.copy(registerA = it).runProgram().output.slice(1 until expectedMatch) ==
            originalProgramState.program.slice(1 until expectedMatch)
        }
      }
    bits += 3
    expectedMatch += 1
  }

  val lowestValueWhereProgramGeneratesItself = inputsMatchingBits.min()
  println("Part 2 lowest value where program generates itself: $lowestValueWhereProgramGeneratesItself")
  check(lowestValueWhereProgramGeneratesItself == 164278899142333L)
}

data class ProgramState(
  val registerA: Long,
  val registerB: Long,
  val registerC: Long,
  val instruction: Int = 0,
  val program: List<Byte>,
  val output: List<Byte> = emptyList(),
)

fun ProgramState.runProgram(): ProgramState {
  var programState = this
  while (programState.instruction in programState.program.indices) {
    val instruction = programState.program[programState.instruction]
    val operand = programState.program[programState.instruction + 1]
    programState = when (instruction) {
      0.toByte() -> programState.adv(operand)
      1.toByte() -> programState.bxl(operand)
      2.toByte() -> programState.bst(operand)
      3.toByte() -> programState.jnz(operand)
      4.toByte() -> programState.bxc(operand)
      5.toByte() -> programState.out(operand)
      6.toByte() -> programState.bdv(operand)
      7.toByte() -> programState.cdv(operand)
      else -> throw IllegalStateException()
    }
  }
  return programState
}

fun ProgramState.combo(operand: Byte): Long =
  when (operand) {
    0.toByte() -> 0L
    1.toByte() -> 1L
    2.toByte() -> 2L
    3.toByte() -> 3L
    4.toByte() -> registerA
    5.toByte() -> registerB
    6.toByte() -> registerC
    else -> throw IllegalStateException()
  }

// opcode 0
fun ProgramState.adv(operand: Byte) =
  copy(registerA = registerA / 2L.pow(combo(operand)), instruction = instruction + 2)

// opcode 1
fun ProgramState.bxl(operand: Byte) =
  copy(registerB = registerB xor operand.toLong(), instruction = instruction + 2)

// opcode 2
fun ProgramState.bst(operand: Byte) =
  copy(registerB = combo(operand) % 8, instruction = instruction + 2)

// opcode 3
fun ProgramState.jnz(operand: Byte) =
  copy(instruction = if (registerA == 0L) instruction + 2 else operand.toInt())

// opcode 4
fun ProgramState.bxc(operand: Byte) =
  copy(registerB = registerB xor registerC, instruction = instruction + 2)

// opcode 5
fun ProgramState.out(operand: Byte) =
  copy(output = output + (combo(operand) % 8).toByte(), instruction = instruction + 2)

// opcode 6
fun ProgramState.bdv(operand: Byte) =
  copy(registerB = registerA / 2L.pow(combo(operand)), instruction = instruction + 2)

// opcode 7
fun ProgramState.cdv(operand: Byte) =
  copy(registerC = registerA / 2L.pow(combo(operand)), instruction = instruction + 2)

fun Long.pow(exponent: Long) = this.toDouble().pow(exponent.toInt()).toLong()

private fun readInput(): ProgramState {
  val lines = File("src/main/kotlin/day17/input.txt").readLines()
    .mapNotNull { it.trim().ifBlank { null } }
  return ProgramState(
    registerA = lines.firstNotNullOf { it.substringAfterOrNull("Register A: ") }.toLong(),
    registerB = lines.firstNotNullOf { it.substringAfterOrNull("Register B: ") }.toLong(),
    registerC = lines.firstNotNullOf { it.substringAfterOrNull("Register C: ") }.toLong(),
    program = lines.firstNotNullOf { it.substringAfterOrNull("Program: ") }.split(",").map { it.toByte() },
  )
}

fun String.substringAfterOrNull(delimiter: String): String? =
  if (contains(delimiter)) substringAfter(delimiter) else null
