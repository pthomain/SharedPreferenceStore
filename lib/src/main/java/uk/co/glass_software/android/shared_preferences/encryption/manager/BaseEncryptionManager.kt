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
import uk.co.glass_software.android.boilerplate.utils.log.Logger

internal abstract class BaseEncryptionManager(protected val logger: Logger)
    : EncryptionManager {

    override fun encrypt(toEncrypt: String?,
                         dataTag: String) =
            try {
                toEncrypt?.let { encryptBytes(it.toByteArray(), dataTag) }
                        ?.let { Base64.encodeToString(it, Base64.DEFAULT) }
            } catch (e: Exception) {
                logger.e(this, "Could not encrypt data for tag: $dataTag")
                null
            }

    override fun decrypt(toDecrypt: String?,
                         dataTag: String) =
            try {
                toDecrypt?.let { Base64.decode(it.toByteArray(), Base64.DEFAULT) }
                        ?.let { decryptBytes(it, dataTag) }
                        ?.let { String(it) }
            } catch (e: Exception) {
                logger.e(this, "Could not decrypt data for tag: $dataTag")
                null
            }

}
