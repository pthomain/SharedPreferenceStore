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

import android.app.UiModeManager.MODE_NIGHT_YES
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.database.DataSetObserver
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ExpandableListView
import android.widget.Switch
import androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode
import uk.co.glass_software.android.boilerplate.core.mvp.MvpActivity
import uk.co.glass_software.android.boilerplate.core.utils.findLazy
import uk.co.glass_software.android.boilerplate.core.utils.log.Logger
import uk.co.glass_software.android.shared_preferences.demo.MainMvpContract.*
import javax.inject.Inject

internal class MainActivity : MvpActivity<MainMvpView, MainMvpPresenter, MainViewComponent>(), MainMvpView {

    @Inject
    lateinit var listAdapter: ExpandableListAdapter

    private val keyEditText by findLazy<EditText>(R.id.input_key)
    private val valueEditText by findLazy<EditText>(R.id.input_value)
    private val encryptedSwitch by findLazy<Switch>(R.id.encrypted_switch)
    private val listView by findLazy<ExpandableListView>(R.id.result)
    private val buttonSave by findLazy<Button>(R.id.button_save)
    private val buttonDelete by findLazy<Button>(R.id.button_delete)
    private val buttonGithub by findLazy<View>(R.id.github)
    private lateinit var logger: Logger

    override fun initialiseComponent() =
            DaggerMainMvpContract_MainViewComponent
                    .builder()
                    .mainViewModule(MainViewModule(this))
                    .build()!!.apply { inject(this@MainActivity) }

    override fun onCreateMvpView(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_main)

        buttonSave.setOnClickListener { onSaveClicked() }
        buttonDelete.setOnClickListener { onDeleteClicked() }
        buttonGithub.setOnClickListener { openGithub() }
    }

    override fun onMvpViewCreated() {
        listView.setAdapter(listAdapter)
        listAdapter.registerDataSetObserver(object : DataSetObserver() {
            override fun onChanged() {
                for (i in 0 until listAdapter.groupCount) {
                    listView.expandGroup(i)
                }
            }
        })
    }

    override fun showEntries() {
        listAdapter.showEntries()
    }

    private fun onSaveClicked() {
        valueEditText.text.toString().let {
            getStoreEntry()?.save(if (it.isEmpty()) null else it)
        }
    }

    private fun onDeleteClicked() {
        getStoreEntry()?.drop()
    }

    private fun getStoreEntry() =
            getPresenter().getStoreEntry(
                    keyEditText.text.toString(),
                    encryptedSwitch.isChecked
            )

    private fun openGithub() {
        startActivity(Intent(
                ACTION_VIEW,
                Uri.parse("https://github.com/pthomain/SharedPreferenceStore")
        ))
    }

    override fun logger() = getPresenter().logger()

    companion object {
        init {
            setDefaultNightMode(MODE_NIGHT_YES)
        }
    }
}