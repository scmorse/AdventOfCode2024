package day23

import java.io.File
import java.util.*

// https://adventofcode.com/2024/day/23
fun main() {
  val input: List<String> = readInput()
  val connections: Map<String, SortedSet<String>> = buildMap {
    input.forEach { line ->
      val (a, b) = line.split("-").sorted()
      getOrPut(a) { sortedSetOf<String>() }.add(b)
      getOrPut(b) { sortedSetOf<String>() }.add(a)
    }
  }

  // Part 1
  val numThreePartyLans = connections.entries
    .flatMap { (player, neighbors: SortedSet<String>) ->
      buildList {
        neighbors.forEachPair { neighbor1, neighbor2 ->
          if (!connections[neighbor1]!!.contains(neighbor2)) return@forEachPair
          if (!player.startsWith("t") && !neighbor1.startsWith("t") && !neighbor2.startsWith("t")) return@forEachPair
          add(listOf(player, neighbor1, neighbor2))
        }
      }
    }
    .count()
    .also { check(it % 3 == 0) } / 3 // each party of three is counted three times
  println("Part 1 num three-party LANs containing a player whose name starts with t: $numThreePartyLans")
  check(numThreePartyLans == 1400)

  // Part 2
  val lans = mutableListOf<Set<String>>()
  connections.mapValues { (_, v) -> v.toSet() }
    .forEach { (player, neighbors) ->
      val toAdd = mutableListOf(setOf(player))
      for (lan in lans) {
        if (lan.all { it in neighbors }) {
          toAdd.add(lan + setOf(player))
        }
      }
      lans.addAll(toAdd)
    }
  val playersInLargestLan = lans.maxBy { it.size }.sorted().joinToString(",")
  println("Part 2 players in largest LAN: $playersInLargestLan")
  check(playersInLargestLan == "am,bc,cz,dc,gy,hk,li,qf,th,tj,wf,xk,xo")
}

fun <T> SortedSet<T>.forEachPair(action: (T, T) -> Unit) {
  val asList = toList()
  asList.indices.forEach { i1 ->
    asList.indices.drop(i1 + 1).forEach { i2 ->
      action(asList[i1], asList[i2])
    }
  }
}

private fun readInput(): List<String> {
  return File("src/main/kotlin/day23/input.txt").readLines()
    .mapNotNull { it.trim().ifBlank { null } }
}
