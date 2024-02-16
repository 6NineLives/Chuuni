package me.abhigya.chuunicore.model

fun interface Observer<T> {

    fun observerUpdate(oldValue: T, newValue: T)

}

data class MutableState<T>(private var value : T) {

    private val observers = mutableListOf<Observer<T>>()

    fun get() : T = value

    fun set(newValue : T) {
        val old = value
        value = newValue
        for (observer in observers) {
            observer.observerUpdate(old, value)
        }
    }

    fun addObserver(observer: Observer<T>) {
        observers.add(observer)
    }

    fun removeObserver(observer: Observer<T>) {
        observers.remove(observer)
    }

    fun clearObservers() {
        observers.clear()
    }

}

fun <T> mutableStateOf(value: T): MutableState<T> = MutableState(value)