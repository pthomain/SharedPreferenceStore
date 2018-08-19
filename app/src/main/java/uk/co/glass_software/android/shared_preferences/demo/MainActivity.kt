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

import android.database.DataSetObserver
import android.net.Uri
import android.os.Bundle
import android.support.customtabs.CustomTabsIntent
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.view.View
import android.widget.EditText
import android.widget.ExpandableListView
import android.widget.Switch
import io.reactivex.disposables.Disposable

class MainActivity : AppCompatActivity() {

    private lateinit var listAdapter: ExpandableListAdapter
    private lateinit var presenter: MainPresenter
    private var subscription: Disposable? = null

    private val keyEditText by lazy { findViewById<EditText>(R.id.input_key) }
    private val valueEditText by lazy { findViewById<EditText>(R.id.input_value) }
    private val encryptedSwitch by lazy { findViewById<Switch>(R.id.encrypted_switch) }
    private val listView by lazy { findViewById<ExpandableListView>(R.id.result) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<View>(R.id.button_save).setOnClickListener { onSaveClicked() }
        findViewById<View>(R.id.button_delete).setOnClickListener { onDeleteClicked() }
        findViewById<View>(R.id.github).setOnClickListener { openGithub() }

        presenter = MainPresenter(this)
        listAdapter = ExpandableListAdapter(this, presenter)
        listView.setAdapter(listAdapter)

        listAdapter.registerDataSetObserver(object : DataSetObserver() {
            override fun onChanged() {
                for (i in 0 until listAdapter.groupCount) {
                    listView.expandGroup(i)
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        listAdapter.showEntries()
        subscription = presenter.observeChanges()
                .subscribe { _ -> listAdapter.showEntries() }
    }

    override fun onPause() {
        super.onPause()
        subscription?.dispose()
        presenter.onPause()
    }

    private fun onSaveClicked() {
        val storeEntry = presenter.getStoreEntry(keyEditText, encryptedSwitch)
        val value = valueEditText.text.toString()
        storeEntry.save(if (value.isEmpty()) null else value)
    }

    private fun onDeleteClicked() {
        val storeEntry = presenter.getStoreEntry(keyEditText, encryptedSwitch)
        storeEntry.drop()
    }

    private fun openGithub() {
        val builder = CustomTabsIntent.Builder()
        val customTabsIntent = builder.build()
        customTabsIntent.launchUrl(this, Uri.parse("https://github.com/pthomain/SharedPreferenceStore"))
    }

    companion object {
        init {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
    }
}