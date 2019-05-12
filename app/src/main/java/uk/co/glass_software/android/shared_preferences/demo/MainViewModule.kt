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

import android.annotation.SuppressLint
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.content.SharedPreferences
import android.view.LayoutInflater
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import uk.co.glass_software.android.boilerplate.core.utils.delegates.Prefs.Companion.prefs
import uk.co.glass_software.android.mumbo.Mumbo
import uk.co.glass_software.android.shared_preferences.demo.model.Counter
import uk.co.glass_software.android.shared_preferences.demo.model.LastOpenDate
import uk.co.glass_software.android.shared_preferences.demo.model.PersonEntry
import uk.co.glass_software.android.shared_preferences.mumbo.MumboEntryFactory
import uk.co.glass_software.android.shared_preferences.persistence.base.KeyValueStore
import uk.co.glass_software.android.shared_preferences.utils.VoidLogger
import java.text.SimpleDateFormat
import javax.inject.Named
import javax.inject.Singleton

@Module
internal class MainViewModule(private val mainActivity: MainActivity) {

    companion object {
        private const val PLAIN_TEXT = "PLAIN_TEXT"
        private const val ENCRYPTED = "ENCRYPTED"
        private const val PREFS_FILENAME = "store"
    }

    @Provides
    @Singleton
    fun provideGson() = Gson()

    @Provides
    @Singleton
    fun provideGsonSerialiser(gson: Gson) = GsonSerialiser(gson)

    @Provides
    @Singleton
    fun providerStoreEntryFactory(serialiser: GsonSerialiser) =
            MumboEntryFactory.builder(mainActivity)
                    .customSerialiser(serialiser)
                    .preferencesFileName(PREFS_FILENAME)
                    .build(Mumbo::tink)

    @Provides
    @Singleton
    @Named(PLAIN_TEXT)
    fun providePlainTextStore(storeEntryFactory: MumboEntryFactory) =
            storeEntryFactory.plainTextStore

    @Provides
    @Singleton
    @Named(ENCRYPTED)
    fun provideEncryptedStore(storeEntryFactory: MumboEntryFactory) =
            storeEntryFactory.encryptedStore

    @Provides
    @Singleton
    fun provideCounter(@Named(PLAIN_TEXT) plainTextStore: KeyValueStore) =
            Counter(plainTextStore)

    @Provides
    @Singleton
    fun provideLastOpenDate(@Named(ENCRYPTED) encryptedStore: KeyValueStore) =
            LastOpenDate(encryptedStore)

    @Provides
    @Singleton
    fun providePersonEntry(@Named(ENCRYPTED) encryptedStore: KeyValueStore) =
            PersonEntry(encryptedStore)

    @Provides
    @Singleton
    fun provideInflater() =
            mainActivity.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater

    @Provides
    @Singleton
    @SuppressLint("SimpleDateFormat")
    fun provideSimpleDateFormat() =
            SimpleDateFormat("hh:mm:ss")

    @Provides
    @Singleton
    @Named(PLAIN_TEXT)
    fun providePlainTextPreferences() =
            mainActivity.context().prefs(PREFS_FILENAME).file

    @Provides
    @Singleton
    @Named(ENCRYPTED)
    //used only to display values as stored on disk, should not be used directly in practice
    fun provideEncryptedPreferences() =
            mainActivity.context().prefs("mumbo_$PREFS_FILENAME").file

    @Provides
    @Singleton
    fun provideMainPresenter(personEntry: PersonEntry,
                             counter: Counter,
                             lastOpenDate: LastOpenDate,
                             @Named(PLAIN_TEXT) plainTextStore: KeyValueStore,
                             @Named(ENCRYPTED) encryptedStore: KeyValueStore,
                             storeEntryFactory: MumboEntryFactory): MainMvpContract.MainMvpPresenter =
            MainPresenter(
                    mainActivity,
                    VoidLogger(),
                    personEntry,
                    counter,
                    lastOpenDate,
                    plainTextStore,
                    encryptedStore,
                    storeEntryFactory
            )

    @Provides
    @Singleton
    fun provideExpandableListAdapter(presenter: MainMvpContract.MainMvpPresenter,
                                     lastOpenDate: LastOpenDate,
                                     counter: Counter,
                                     inflater: LayoutInflater,
                                     simpleDateFormat: SimpleDateFormat,
                                     @Named(PLAIN_TEXT) plainTextPreferences: SharedPreferences,
                                     @Named(ENCRYPTED) encryptedPreferences: SharedPreferences,
                                     storeEntryFactory: MumboEntryFactory) =
            ExpandableListAdapter(
                    presenter,
                    lastOpenDate,
                    counter,
                    inflater,
                    simpleDateFormat,
                    plainTextPreferences,
                    encryptedPreferences,
                    storeEntryFactory
            )
}
