package day24

import java.io.File

fun main() {
  // Part 1
  val (gateLines, equationLines) = readInput().partition { line -> line[3] == ':' }

  val gates = buildMap {
    gateLines.forEach { line ->
      put(line.substringBefore(":"), line.substringAfter(": ").toInt())
    }
  }

  val equations = buildList {
    equationLines.forEach { line ->
      val (_, left, op, right, out) = Regex("(.*) (.*) (.*) -> (.*)").findAll(line)
        .first().groupValues
      this@buildList.add(Equation(left, Op.valueOf(op), right, out))
    }
  }

  val part1Answer = evaluateEquations(gates, equations)
  println("Part 1 answer: $part1Answer")
  check(part1Answer == 60614602965288L)

  // Part 2
  val correctEquations =
    listOf(
      Equation("x00", Op.XOR, "y00", "z00"),
      Equation("x00", Op.AND, "y00", "c00"),
    ) + (1..44).flatMap { i ->
      val nPrior = "${i - 1}".padStart(2, '0')
      val n = "$i".padStart(2, '0')
      listOf(
        Equation("x$n", Op.XOR, "y$n", "sa$n"),
        Equation("x$n", Op.AND, "y$n", "ca$n"),
        Equation("c$nPrior", Op.XOR, "sa$n", "z$n"),
        Equation("c$nPrior", Op.AND, "sa$n", "cb$n"),
        Equation("ca$n", Op.OR, "cb$n", "c$n"),
      )
    }

  var best = 0
  var bests = mutableListOf<Pair<Equation, Equation>>()
  equations.forEachPair { a, b ->
    val eqs = equations - setOf(a, b) + listOf(
      a.copy(out = b.out),
      b.copy(out = a.out),
    )
    val score = correlateAndCountMatches(eqs, correctEquations)
    if (score > best) {
      best = score
      bests = mutableListOf(a to b)
    } else if (score == best) {
      bests.add(a to b)
    }
  }

  check(bests.size == 4)
  val part2Answer = bests.flatMap { (a, b) -> listOf(a.out, b.out) }.sorted().joinToString(",")
  println("Part 2 pairs to swap: $part2Answer")
  check(part2Answer == "cgr,hpc,hwk,qmd,tnt,z06,z31,z37")
}

fun correlateAndCountMatches(
  equations: List<Equation>,
  correctEquations: List<Equation>,
): Int {
  val mapping = mutableMapOf<String, String>().apply {
    (0..44).forEach { i ->
      val n = "$i".padStart(2, '0')
      put("x$n", "x$n")
      put("y$n", "y$n")
      put("z$n", "z$n")
    }
  }

  while (true) {
    val sizeAtStart = mapping.size

    equations.forEach { equation ->
      val (knownInputs, unknownInputs) = equation.inputs.partition { it in mapping }
      val (knownOutputs, unknownOutputs) = listOf(equation.out).partition { it in mapping }
      if ((unknownInputs + unknownOutputs).size == 1) {
        val correctEquationMatches = correctEquations.filter { ce ->
          ce.op == equation.op && knownInputs.all { mapping[it] in ce.inputs } && knownOutputs.all { mapping[it] == ce.out }
        }
        if (correctEquationMatches.size == 1) {
          if (unknownOutputs.isNotEmpty()) {
            // found these suspicious gates in the output position because they don't appear as
            // the input to another "XOR" and "AND" gate, like the other equation outputs do.
            if (unknownOutputs.none { it in setOf("qmd", "tnt") }) {
              mapping[equation.out] = correctEquationMatches.first().out
            }
          } else {
            mapping[unknownInputs.first()] =
              (correctEquationMatches.first().inputs - knownInputs.map { mapping[it]!! }).first()
          }
        }
      }
    }

    if (mapping.size == sizeAtStart) break
  }

  val matchingSeen = equations.mapNotNull {
    Equation(
      left = mapping[it.left] ?: return@mapNotNull null,
      op = it.op,
      right = mapping[it.right] ?: return@mapNotNull null,
      out = mapping[it.out] ?: return@mapNotNull null
    )
  }.filter { it in correctEquations }

  return matchingSeen.size
}

fun <T> List<T>.forEachPair(action: (T, T) -> Unit) {
  val asList = toList()
  asList.indices.forEach { i1 ->
    asList.indices.drop(i1 + 1).forEach { i2 ->
      action(asList[i1], asList[i2])
    }
  }
}

fun evaluateEquations(
  input: Map<String, Int>,
  equations: List<Equation>,
): Long {
  val gates = input.toMutableMap()

  while (true) {
    val eq = equations.find {
      gates[it.left] != null && gates[it.right] != null && gates[it.out] == null
    } ?: break

    gates[eq.out] = when (eq.op) {
      Op.XOR -> gates[eq.left]!! xor gates[eq.right]!!
      Op.OR -> gates[eq.left]!! or gates[eq.right]!!
      Op.AND -> gates[eq.left]!! and gates[eq.right]!!
    }
  }

  return (45 downTo 0).joinToString("") { i -> gates["z" + "$i".padStart(2, '0')]!!.toString() }
    .toLong(2)
}

data class Equation private constructor(
  val left: String,
  val op: Op,
  val right: String,
  val out: String,
) {
  val inputs: List<String> get() = listOf(left, right)
  override fun toString(): String = "$left $op $right -> $out"

  companion object {
    operator fun invoke(
      left: String,
      op: Op,
      right: String,
      out: String,
    ): Equation = listOf(left, right).sorted()
      .let { Equation(it.first(), op, it.last(), out) }
  }
}

enum class Op {
  XOR,
  OR,
  AND,
}

private fun readInput(filename: String = "input.txt"): List<String> {
  return File("src/main/kotlin/day24/$filename").readLines()
    .mapNotNull { it.trim().ifBlank { null } }
}
