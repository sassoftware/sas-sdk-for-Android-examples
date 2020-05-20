package com.sas.covid19.kotlin

/**
 * Return a copy of this list with the element at the given position moved to a
 * new position.
 *
 * @param fromPos
 *     the position in this list
 *
 * @param toPos
 *     the position in the new list
 */
fun <T> List<T>.move(fromPos: Int, toPos: Int) =
    when {
        fromPos < toPos ->
            slice(0 until fromPos) + slice(fromPos + 1 until toPos + 1) +
                get(fromPos) + slice(toPos + 1 until size)
        fromPos > toPos ->
            slice(0 until toPos) + get(fromPos) + slice(toPos until fromPos) +
                slice(fromPos + 1 until size)
        else -> toList()
    }

/**
 * Return a new list with the given element inserted.
 */
fun <T> List<T>.with(position: Int, item: T) =
    slice(0 until position) + item + slice(position until size)
