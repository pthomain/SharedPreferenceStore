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

import dagger.Component
import uk.co.glass_software.android.boilerplate.core.mvp.base.MvpContract.*
import uk.co.glass_software.android.boilerplate.core.utils.log.Logger
import uk.co.glass_software.android.shared_preferences.persistence.base.KeyValueEntry
import javax.inject.Singleton

internal interface MainMvpContract {

    interface MainMvpView : MvpView<MainMvpView, MainMvpPresenter, MainViewComponent> {

        fun showEntries()

    }

    interface MainMvpPresenter : Presenter<MainMvpView, MainMvpPresenter, MainViewComponent> {

        fun getKey(entry: Map.Entry<String, *>): String

        fun getStoreEntry(key: String,
                          isEncrypted: Boolean): KeyValueEntry<String>?

        fun logger(): Logger

    }

    @Singleton
    @Component(modules = [MainViewModule::class])
    interface MainViewComponent : ViewComponent<MainMvpView, MainMvpPresenter, MainViewComponent> {

        fun inject(mainActivity: MainActivity)
    }

}