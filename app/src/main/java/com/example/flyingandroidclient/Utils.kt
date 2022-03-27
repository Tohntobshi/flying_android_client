package com.example.flyingandroidclient


fun <T> MutableList<T>.addNotExceed(element: T, maxSize: Int) {
    this.add(element)
    if (this.size > maxSize) this.removeFirst()
}
