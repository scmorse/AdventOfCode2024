package day1

import partitionIndexed
import kotlin.math.abs

fun main() {
  val (list1, list2) = input.partitionIndexed { i, _ -> i % 2 == 0 }

  // Part 1
  val distance = list1.zip(list2).fold(0) { acc, (a, b) ->
    acc + abs(a - b)
  }
  println("Part 1 distance: $distance")

  // Part 2
  val counts = list2.groupingBy { it }.eachCount()
  val similarity = list1.fold(0) { acc, it ->
    acc + it * counts.getOrDefault(it, 0)
  }
  println("Part 2 similarity: $similarity")
}
