fun <T> List<T>.second(): T {
  if (size < 2) throw NoSuchElementException("List does not have two elements.")
  return this[1]
}

fun <T> List<T>.consecutivePairs(): Sequence<Pair<T, T>> = sequence {
  for (i in indices.drop(1)) yield(get(i - 1) to get(i))
}
