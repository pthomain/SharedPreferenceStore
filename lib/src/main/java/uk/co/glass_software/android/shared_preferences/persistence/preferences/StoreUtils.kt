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

package uk.co.glass_software.android.shared_preferences.persistence.preferences

import android.content.Context
import uk.co.glass_software.android.boilerplate.core.utils.delegates.Prefs
import uk.co.glass_software.android.boilerplate.core.utils.delegates.Prefs.Companion.prefs

object StoreUtils {

    fun openSharedPreferences(context: Context,
                              name: String) =
            getSharedPreferenceFactory(context)(name)

    private fun getSharedPreferenceFactory(context: Context): (String) -> Prefs = {
        context.prefs(getStoreName(context, it))
    }

    private fun getStoreName(context: Context,
                             name: String): String {
        val availableLength = StoreModule.MAX_FILE_NAME_LENGTH - name.length
        var packageName = context.packageName

        if (packageName.length > availableLength) {
            packageName = packageName.substring(
                    packageName.length - availableLength - 1,
                    packageName.length
            )
        }

        return "$packageName$$name"
    }

}