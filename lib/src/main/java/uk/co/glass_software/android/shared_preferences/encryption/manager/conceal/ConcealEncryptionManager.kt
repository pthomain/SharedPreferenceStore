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

package uk.co.glass_software.android.shared_preferences.encryption.manager.conceal


import android.content.Context

import com.facebook.android.crypto.keychain.AndroidConceal
import com.facebook.android.crypto.keychain.SharedPrefsBackedKeyChain
import com.facebook.crypto.Crypto
import com.facebook.crypto.Entity
import com.facebook.soloader.SoLoader

import uk.co.glass_software.android.boilerplate.log.Logger
import uk.co.glass_software.android.shared_preferences.encryption.manager.BaseEncryptionManager

internal class ConcealEncryptionManager(context: Context,
                                        logger: Logger,
                                        keyChain: SharedPrefsBackedKeyChain,
                                        androidConceal: AndroidConceal)
    : BaseEncryptionManager(logger) {

    override val isEncryptionSupported: Boolean by lazy { isAvailable }

    private var isAvailable: Boolean = false
    private var crypto: Crypto? = null

    init {
        try {
            SoLoader.init(context, false)

            // Creates a new Crypto object with default implementations of a key chain
            crypto = androidConceal.createDefaultCrypto(keyChain)

            // Check for whether the crypto functionality is available
            // This might fail if Android does not load libraries correctly.
            isAvailable = crypto?.isAvailable ?: false
        } catch (e: Exception) {
            isAvailable = false
        }

        logger.d("Conceal is" + (if (isAvailable) "" else " NOT") + " available")
    }

    override fun encryptBytes(toEncrypt: ByteArray?,
                              dataTag: String): ByteArray? {
        if (toEncrypt == null || !isAvailable) {
            return null
        }

        return try {
            crypto!!.encrypt(toEncrypt, Entity.create(dataTag))
        } catch (e: Exception) {
            logger.e(e, "Could not encrypt the given bytes")
            null
        }
    }

    override fun decryptBytes(toDecrypt: ByteArray?,
                              dataTag: String): ByteArray? {
        if (toDecrypt == null || !isAvailable) {
            return null
        }

        return try {
            crypto!!.decrypt(toDecrypt, Entity.create(dataTag))
        } catch (e: Exception) {
            logger.e(e, "Could not decrypt the given bytes")
            null
        }
    }

}
