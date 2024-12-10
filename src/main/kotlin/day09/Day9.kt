package day09

import java.io.File
import java.util.*

// https://adventofcode.com/2024/day/9
fun main() {
  val input: String = readInput()

  // Part 1
  val checksumForPart1 = input.parseDiskAllocations().defragmentForPart1().checksum()
  println("Part 1 checksum: $checksumForPart1")
  check(checksumForPart1 == 6448989155953L)

  // Part 2
  val checksumForPart2 = input.parseDiskBlockAllocations()
    .defragmentForPart2()
    .flatMap { block ->
      val item = when (block.item) {
        is DiskBlockAllocation.FileStorage -> DiskAllocation.FileStorage(fileId = block.item.fileId)
        is DiskBlockAllocation.FreeSpace -> DiskAllocation.FreeSpace
      }
      List(block.item.size) { item }
    }
    .checksum()
  println("Part 2 checksum: $checksumForPart2")
  check(checksumForPart2 == 6476642796832L)
}

// Part 1 code

sealed class DiskAllocation {
  data object FreeSpace : DiskAllocation()
  data class FileStorage(val fileId: Int) : DiskAllocation()
}

fun String.parseDiskAllocations(): MutableList<DiskAllocation> =
  flatMapIndexedTo(mutableListOf()) { i, char ->
    val diskAllocation = if (i % 2 == 0) DiskAllocation.FileStorage(fileId = i / 2) else DiskAllocation.FreeSpace
    List(char.digitToInt()) { diskAllocation }
  }

fun MutableList<DiskAllocation>.defragmentForPart1() = apply {
  var start = indices.first
  var end = indices.last

  while (true) {
    if (start !in indices) break
    val freeSpace = (this[start] as? DiskAllocation.FreeSpace)
    if (freeSpace == null) {
      start++
      continue
    }

    if (end !in indices) break
    val fileStorage = (this[end] as? DiskAllocation.FileStorage)
    if (fileStorage == null) {
      end--
      continue
    }

    if (start > end) break
    this[start] = this[end]
    this[end] = DiskAllocation.FreeSpace
  }
}

// Part 2 code

sealed class DiskBlockAllocation {
  abstract val location: IntRange
  val size: Int
    get() = location.last - location.first + 1

  data class FreeSpace(override val location: IntRange) : DiskBlockAllocation()

  data class FileStorage(override val location: IntRange, val fileId: Int) : DiskBlockAllocation()
}

fun String.parseDiskBlockAllocations() = LinkedList<DiskBlockAllocation>().apply {
  var rangeOffset = 0
  this@parseDiskBlockAllocations.forEachIndexed { i, char ->
    val size = char.digitToInt()
    val range = (rangeOffset until (rangeOffset + size))
    rangeOffset += size
    addLast(
      if (i % 2 == 0) {
        DiskBlockAllocation.FileStorage(fileId = i / 2, location = range)
      } else DiskBlockAllocation.FreeSpace(location = range)
    )
  }
}

fun LinkedList<DiskBlockAllocation>.defragmentForPart2() = apply {
  val comparator = compareBy<LinkedList.Node<DiskBlockAllocation>> { it.item.location.first }

  val fileStorageNodes = mutableListOf<LinkedList.Node<DiskBlockAllocation>>()
  val emptyBlocksBySize = (0..9).map { TreeSet(comparator) }
  forEach {
    when (it.item) {
      is DiskBlockAllocation.FileStorage -> fileStorageNodes.add(it)
      is DiskBlockAllocation.FreeSpace -> emptyBlocksBySize[it.item.size].add(it)
    }
  }

  fileStorageNodes.forEachInReverse { fileStorageNode: LinkedList.Node<DiskBlockAllocation> ->
    val emptyBlockGroupWithSpace: TreeSet<LinkedList.Node<DiskBlockAllocation>> =
      emptyBlocksBySize.slice(fileStorageNode.item.size..9)
        .filter { it.isNotEmpty() }
        .minByOrNull { it.first().item.location.first } ?: return@forEachInReverse
    val emptyBlockNode: LinkedList.Node<DiskBlockAllocation> = emptyBlockGroupWithSpace.first()
    if (comparator.compare(emptyBlockNode, fileStorageNode) > 0) return@forEachInReverse
    emptyBlockGroupWithSpace.removeFirst()

    setRelativeOrder(
      fileStorageNode.prev,
      LinkedList.Node(DiskBlockAllocation.FreeSpace(fileStorageNode.item.location)),
      fileStorageNode.next,
    )

    val emptyBlockNodeOldPrev: LinkedList.Node<DiskBlockAllocation>? = emptyBlockNode.prev
    val emptyBlockNodeOldNext: LinkedList.Node<DiskBlockAllocation>? = emptyBlockNode.next

    if (emptyBlockNode.item.size == fileStorageNode.item.size) {
      setRelativeOrder(emptyBlockNodeOldPrev, fileStorageNode, emptyBlockNodeOldNext)
    } else {
      val newFreeSpaceLocation =
        (emptyBlockNode.item.location.first + fileStorageNode.item.size)..emptyBlockNode.item.location.last
      val newFreeSpaceNode: LinkedList.Node<DiskBlockAllocation> =
        LinkedList.Node(DiskBlockAllocation.FreeSpace(newFreeSpaceLocation))
      setRelativeOrder(emptyBlockNodeOldPrev, fileStorageNode, newFreeSpaceNode, emptyBlockNodeOldNext)
      emptyBlocksBySize[newFreeSpaceNode.item.size].add(newFreeSpaceNode)
    }
  }
}

fun List<DiskAllocation>.checksum(): Long = withIndex().sumOf { (i, allocation) ->
  i.toLong() * when (allocation) {
    is DiskAllocation.FileStorage -> allocation.fileId.toLong()
    DiskAllocation.FreeSpace -> 0L
  }
}

class LinkedList<T>(
  private var first: Node<T>? = null,
  private var last: Node<T>? = null,
) : Iterable<LinkedList.Node<T>> {
  class Node<T>(val item: T, var prev: Node<T>? = null, var next: Node<T>? = null)

  override fun iterator(): Iterator<Node<T>> = iterator {
    var node = first
    while (node != null) {
      yield(node)
      node = node.next
    }
  }

  fun addLast(item: T) {
    if (first == null || last == null) {
      first = Node(item = item)
      last = first
      return
    }
    val newLast = Node(item, next = null, prev = last)
    last!!.next = newLast
    last = newLast
  }

  fun setRelativeOrder(vararg order: Node<T>?) {
    order.toList().zipWithNext { a, b ->
      a?.next = b
      b?.prev = a
    }
  }
}

fun <T> List<T>.forEachInReverse(action: (T) -> Unit) {
  (indices.last downTo indices.first).forEach { i -> action(this[i]) }
}

private fun readInput(): String {
  return File("src/main/kotlin/day09/input.txt").readText().trim()
}
