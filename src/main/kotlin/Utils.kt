fun <T> List<T>.second(): T {
  if (size < 2) throw NoSuchElementException("List does not have two elements.")
  return this[1]
}
