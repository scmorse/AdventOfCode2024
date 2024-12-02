
fun <T> List<T>.second(): T {
  if (size < 2) throw NoSuchElementException("List does not have two elements.")
  return this[1]
}

inline fun <T> Iterable<T>.partitionIndexed(predicate: (index: Int, T) -> Boolean): Pair<List<T>, List<T>> {
  val first = ArrayList<T>()
  val second = ArrayList<T>()
  forEachIndexed { i, element ->
    if (predicate(i, element)) {
      first.add(element)
    } else {
      second.add(element)
    }
  }
  return Pair(first, second)
}
