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

import android.content.SharedPreferences
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import uk.co.glass_software.android.shared_preferences.demo.model.Counter
import uk.co.glass_software.android.shared_preferences.demo.model.LastOpenDate
import uk.co.glass_software.android.shared_preferences.demo.model.Person
import uk.co.glass_software.android.shared_preferences.demo.model.PersonEntry
import uk.co.glass_software.android.shared_preferences.mumbo.MumboEntryFactory
import uk.co.glass_software.android.shared_preferences.mumbo.store.StoreMode
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

internal class ExpandableListAdapter(
        private val presenter: MainMvpContract.MainMvpPresenter,
        private val lastOpenDate: LastOpenDate,
        private val counter: Counter,
        private val inflater: LayoutInflater,
        private val simpleDateFormat: SimpleDateFormat,
        private val plainTextPreferences: SharedPreferences,
        private val encryptedPreferences: SharedPreferences,
        private val storeEntryFactory: MumboEntryFactory
) : BaseExpandableListAdapter() {

    private val headers: LinkedList<String> = LinkedList()
    private val children: LinkedHashMap<String, List<String>> = LinkedHashMap()

    fun showEntries() {
        headers.clear()
        children.clear()

        val lastOpenDate = lastOpenDate.get()
        val formattedDate = lastOpenDate?.let { simpleDateFormat.format(it) }

        addEntries("App opened",
                "Count: " + counter.get() + " time(s)",
                "Last open date: " + (formattedDate ?: "N/A")
        )

        addEntries("Plain text entries", plainTextPreferences.all)

        addEntries("Encrypted entries (as returned by the store)",
                encryptedPreferences.all.keys
                        .associate {
                            Pair(
                                    it,
                                    storeEntryFactory.open(
                                            it,
                                            StoreMode.ENCRYPTED,
                                            getValueClass(it)
                                    ).get()
                            )
                        }
        )

        addEntries("Encrypted entries (as stored on disk)", encryptedPreferences.all)

        notifyDataSetChanged()
    }

    private fun getValueClass(key: String) =
            when (key) {
                Counter.KEY -> Int::class.java
                LastOpenDate.KEY -> Date::class.java
                PersonEntry.KEY -> Person::class.java
                else -> String::class.java
            }

    private fun addEntries(header: String,
                           entries: Map<String, *>) {
        val list = entries.entries
                .map { entry -> presenter.getKey(entry) + " => " + entry.value }

        addEntries(header, *list.toTypedArray())
    }

    private fun addEntries(header: String,
                           vararg subSections: String) {
        val info = ArrayList<String>()
        headers.add(header)

        subSections.map { string -> string.replace("\\n".toRegex(), "") }
                .forEach { info.add(it) }

        children[header] = info
    }

    override fun getChild(groupPosition: Int,
                          childPosition: Int): Any? {
        val key = headers[groupPosition]
        return children[key]?.get(childPosition)
    }

    override fun getChildId(groupPosition: Int,
                            childPosition: Int) = childPosition.toLong()

    override fun getGroupView(groupPosition: Int,
                              isExpanded: Boolean,
                              convertView: View?,
                              parent: ViewGroup): View {
        val view = convertView ?: inflater.inflate(R.layout.list_group, parent, false)
        with(view.findViewById<TextView>(R.id.lblListHeader)) {
            setTypeface(null, Typeface.BOLD)
            text = getGroup(groupPosition)
        }
        return view
    }

    override fun getChildView(groupPosition: Int,
                              childPosition: Int,
                              isLastChild: Boolean,
                              convertView: View?,
                              parent: ViewGroup): View {
        val view = convertView ?: inflater.inflate(R.layout.list_item, parent, false)
        view.findViewById<TextView>(R.id.lblListItem).text = getChild(groupPosition, childPosition) as String
        return view
    }

    override fun getChildrenCount(groupPosition: Int) = children[headers[groupPosition]]?.size ?: 0

    override fun getGroup(groupPosition: Int) = headers[groupPosition]

    override fun getGroupCount() = headers.size

    override fun getGroupId(groupPosition: Int) = groupPosition.toLong()

    override fun hasStableIds() = false

    override fun isChildSelectable(groupPosition: Int,
                                   childPosition: Int) = false
}