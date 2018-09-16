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
import android.content.SharedPreferences
import android.graphics.Typeface
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import ix.Ix
import uk.co.glass_software.android.shared_preferences.StoreEntryFactory.Companion.DEFAULT_ENCRYPTED_PREFERENCE_NAME
import uk.co.glass_software.android.shared_preferences.StoreEntryFactory.Companion.DEFAULT_PLAIN_TEXT_PREFERENCE_NAME
import uk.co.glass_software.android.shared_preferences.demo.model.Keys
import uk.co.glass_software.android.shared_preferences.persistence.base.KeyValueEntry
import uk.co.glass_software.android.shared_preferences.persistence.preferences.StoreUtils.openSharedPreferences
import uk.co.glass_software.android.shared_preferences.utils.StoreKey
import uk.co.glass_software.android.shared_preferences.utils.StoreMode
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

internal class ExpandableListAdapter(context: Context,
                                     private val presenter: MainPresenter)
    : BaseExpandableListAdapter() {

    private val headers: LinkedList<String> = LinkedList()
    private val children: LinkedHashMap<String, List<String>> = LinkedHashMap()
    private val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val simpleDateFormat = SimpleDateFormat("hh:mm:ss")
    private val plainTextPreferences: SharedPreferences = openSharedPreferences(context, DEFAULT_PLAIN_TEXT_PREFERENCE_NAME).file

    //used only to display values as stored on disk, should not be used directly in practice
    private val encryptedPreferences: SharedPreferences = openSharedPreferences(context, DEFAULT_ENCRYPTED_PREFERENCE_NAME).file

    fun showEntries() {
        headers.clear()
        children.clear()

        val lastOpenDate = presenter.lastOpenDate().get()
        val formattedDate = lastOpenDate?.let { simpleDateFormat.format(it) }

        addEntries("App opened",
                "Count: " + presenter.counter().get() + " time(s)",
                "Last open date: " + (formattedDate ?: "N/A")
        )

        addEntries("Plain text entries", plainTextPreferences.all)

        addEntries("Encrypted entries (as returned by the store)",
                Ix.from(encryptedPreferences.all.keys)
                        .map<Pair<String, KeyValueEntry<Any>>> { key ->
                            Pair.create<String, KeyValueEntry<Any>>(
                                    key,
                                    presenter.storeEntryFactory.open(
                                            key,
                                            StoreMode.ENCRYPTED,
                                            getValueClass(key)
                                    )
                            )
                        }
                        .toMap<String, String>({ pair -> pair.first }) { pair -> pair.second.get("[error]").toString() }
        )

        addEntries("Encrypted entries (as stored on disk)", encryptedPreferences.all)

        notifyDataSetChanged()
    }

    @Suppress("UNCHECKED_CAST")
    private fun <C> getValueClass(key: String) =
            Ix.from(Arrays.asList(*Keys.values()))
                    .map { keys -> keys.key }
                    .filter { storeKey -> key == storeKey.uniqueKey }
                    .map<Class<*>>(StoreKey::valueClass)
                    .defaultIfEmpty(String::class.java)
                    .first() as Class<C>

    private fun addEntries(header: String,
                           entries: Map<String, *>) {
        val list = Ix.from<Map.Entry<String, *>>(entries.entries)
                .map { entry -> presenter.getKey(entry) + " => " + entry.value }
                .toList()

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
        val headerTitle = getGroup(groupPosition)
        val view = convertView ?: inflater.inflate(R.layout.list_group, parent, false)
        val lblListHeader = view.findViewById<TextView>(R.id.lblListHeader)
        lblListHeader.setTypeface(null, Typeface.BOLD)
        lblListHeader.text = headerTitle
        return view
    }

    override fun getChildView(groupPosition: Int,
                              childPosition: Int,
                              isLastChild: Boolean,
                              convertView: View?,
                              parent: ViewGroup): View {
        val childText = getChild(groupPosition, childPosition) as String
        val view = convertView ?: inflater.inflate(R.layout.list_item, parent, false)
        val txtListChild = view.findViewById<TextView>(R.id.lblListItem)
        txtListChild.text = childText
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