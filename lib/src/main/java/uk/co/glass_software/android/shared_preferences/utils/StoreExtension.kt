package uk.co.glass_software.android.shared_preferences.utils

import uk.co.glass_software.android.shared_preferences.StoreEntryFactory
import uk.co.glass_software.android.shared_preferences.persistence.base.KeyValueEntry
import kotlin.reflect.KProperty

inline fun <reified T> StoreEntryFactory.openDelegated(key: StoreKey,
                                                       defaultValue: T? = null): DelegatedEntry<T> {
    val entry = open<T>(key)
    return DelegatedEntry(entry, defaultValue)
}

inline fun <reified T> StoreEntryFactory.openDelegated(key: String,
                                                       mode: StoreMode,
                                                       defaultValue: T? = null): DelegatedEntry<T> {
    val entry = open<T>(key, mode, T::class.java)
    return DelegatedEntry(entry, defaultValue)
}

class DelegatedEntry<T> constructor(entry: KeyValueEntry<T>,
                                    defaultValue: T?) {
    var value: T? by StoreEntryDelegate(entry, defaultValue)
}

class StoreEntryDelegate<T>(private val entry: KeyValueEntry<T>,
                            private val defaultValue: T? = null) {

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T? = entry.get(defaultValue)

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
        entry.save(value)
    }
}