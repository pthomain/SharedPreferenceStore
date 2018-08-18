/*
 * Copyright (C) 2017 Glass Software Ltd
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package uk.co.glass_software.android.shared_preferences.demo

import android.content.Context
import android.widget.EditText
import android.widget.Switch

import com.google.gson.Gson

import java.util.Date

import io.reactivex.Observable
import uk.co.glass_software.android.boilerplate.log.SimpleLogger
import uk.co.glass_software.android.shared_preferences.StoreEntryFactory
import uk.co.glass_software.android.shared_preferences.demo.model.Counter
import uk.co.glass_software.android.shared_preferences.demo.model.LastOpenDate
import uk.co.glass_software.android.shared_preferences.demo.model.Person
import uk.co.glass_software.android.shared_preferences.demo.model.PersonEntry
import uk.co.glass_software.android.shared_preferences.persistence.base.KeyValueEntry
import uk.co.glass_software.android.shared_preferences.persistence.base.KeyValueStore
import uk.co.glass_software.android.shared_preferences.utils.StoreMode


internal class MainPresenter(context: Context) {

    val storeEntryFactory: StoreEntryFactory = StoreEntryFactory.builder(context)
            .logger(SimpleLogger(BuildConfig.DEBUG))
            .customSerialiser(GsonSerialiser(Gson()))
            .build()

    private val plainTextStore = storeEntryFactory.plainTextStore
    private val encryptedStore = storeEntryFactory.encryptedStore

    private val counter = Counter(plainTextStore)
    private val lastOpenDate = LastOpenDate(encryptedStore)
    private val personEntry = PersonEntry(encryptedStore)

    init {
        createOrUpdatePerson()
    }

    private fun createOrUpdatePerson() {
        val lastSeenDate = Date()
        var person = personEntry.get()

        if (person == null) {
            person = Person()
            person.age = 30
            person.firstName = "John"
            person.name = "Smith"
        }

        person.lastSeenDate = lastSeenDate
        personEntry.save(person)
    }

    fun onPause() {
        counter.save(counter.get(1)!! + 1)
        lastOpenDate.save(Date())
        createOrUpdatePerson()
    }

    fun counter() = counter

    fun lastOpenDate() = lastOpenDate

    fun getKey(entry: Map.Entry<String, *>) = entry.key

    fun getStoreEntry(editText: EditText,
                      encryptedSwitch: Switch): KeyValueEntry<String> {
        val key = editText.text.toString()

        return if (key.isEmpty()) {
            VoidEntry()
        } else storeEntryFactory
                .open(key,
                        if (encryptedSwitch.isChecked) StoreMode.ENCRYPTED else StoreMode.PLAIN_TEXT,
                        String::class.java
                )

    }

    fun observeChanges() = plainTextStore.observeChanges().mergeWith(encryptedStore.observeChanges())!!

    private inner class VoidEntry : KeyValueEntry<String> {
        override fun save(value: String?) {}

        override operator fun get(defaultValue: String?) = defaultValue

        override fun drop() {}

        override fun getKey() = ""

        override fun exists() = false
    }
}
