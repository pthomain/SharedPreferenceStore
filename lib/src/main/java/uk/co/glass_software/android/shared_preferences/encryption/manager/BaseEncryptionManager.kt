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

package uk.co.glass_software.android.shared_preferences.encryption.manager

import android.util.Base64

import uk.co.glass_software.android.boilerplate.log.Logger

internal abstract class BaseEncryptionManager protected constructor(protected val logger: Logger)
    : EncryptionManager {

    override fun encrypt(toEncrypt: String?,
                         dataTag: String): String? {
        if (toEncrypt == null) {
            return null
        }

        return try {
            val input = encryptBytes(toEncrypt.toByteArray(), dataTag)
            if (input == null) null else Base64.encodeToString(input, Base64.DEFAULT)
        } catch (e: Exception) {
            logger.e(this, "Could not encrypt data for tag: " + dataTag)
            null
        }
    }

    override fun decrypt(toDecrypt: String?,
                         dataTag: String): String? {
        if (toDecrypt == null) {
            return null
        }

        return try {
            val decode = Base64.decode(toDecrypt.toByteArray(), Base64.DEFAULT)
            val bytes = decryptBytes(decode, dataTag)
            if (bytes == null) null else String(bytes)
        } catch (e: Exception) {
            logger.e(this, "Could not decrypt data for tag: " + dataTag)
            null
        }
    }

}
