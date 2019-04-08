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

import androidx.lifecycle.Lifecycle.Event.ON_PAUSE
import androidx.lifecycle.Lifecycle.Event.ON_RESUME
import androidx.lifecycle.OnLifecycleEvent
import io.reactivex.disposables.CompositeDisposable
import uk.co.glass_software.android.boilerplate.core.mvp.MvpPresenter
import uk.co.glass_software.android.boilerplate.core.utils.log.Logger
import uk.co.glass_software.android.shared_preferences.demo.MainMvpContract.*
import uk.co.glass_software.android.shared_preferences.demo.model.Counter
import uk.co.glass_software.android.shared_preferences.demo.model.LastOpenDate
import uk.co.glass_software.android.shared_preferences.demo.model.Person
import uk.co.glass_software.android.shared_preferences.demo.model.PersonEntry
import uk.co.glass_software.android.shared_preferences.mumbo.MumboEntryFactory
import uk.co.glass_software.android.shared_preferences.mumbo.store.StoreMode.ENCRYPTED
import uk.co.glass_software.android.shared_preferences.mumbo.store.StoreMode.PLAIN_TEXT
import uk.co.glass_software.android.shared_preferences.persistence.base.KeyValueStore
import java.util.*

internal class MainPresenter(mvpView: MainMvpView,
                             private val logger: Logger,
                             private val personEntry: PersonEntry,
                             private val counter: Counter,
                             private val lastOpenDate: LastOpenDate,
                             private val plainTextStore: KeyValueStore,
                             private val encryptedStore: KeyValueStore,
                             private val storeEntryFactory: MumboEntryFactory)
    : MvpPresenter<MainMvpView, MainMvpPresenter, MainViewComponent>(mvpView, CompositeDisposable()),
        MainMvpPresenter {

    init {
        createOrUpdatePerson()
    }

    private fun createOrUpdatePerson() {
        personEntry.get(Person(
                age = 30,
                firstName = "John",
                name = "Smith"
        ))
                .copy(lastSeenDate = Date())
                .let { personEntry.save(it) }
    }

    @OnLifecycleEvent(ON_PAUSE)
    fun onPause() {
        counter.save(counter.get(1).plus(1))
        lastOpenDate.save(Date())
        createOrUpdatePerson()
    }

    @OnLifecycleEvent(ON_RESUME)
    fun onResume() {
        plainTextStore.observeChanges()
                .mergeWith(encryptedStore.observeChanges())
                .startWith("ignore")
                .autoSubscribe { mvpView.showEntries() }
    }

    override fun getKey(entry: Map.Entry<String, *>) = entry.key

    override fun getStoreEntry(key: String,
                               isEncrypted: Boolean) =
            if (key.isEmpty()) null
            else storeEntryFactory.open(
                    key,
                    if (isEncrypted) ENCRYPTED else PLAIN_TEXT,
                    String::class.java
            )

    override fun logger() = logger

}
