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

package uk.co.glass_software.android.shared_preferences.persistence.serialisation

import android.util.Base64
import uk.co.glass_software.android.boilerplate.core.utils.log.Logger
import java.io.*

class Base64Serialiser(private val logger: Logger,
                       private val base64: CustomBase64) : Serialiser {

    override fun canHandleType(targetClass: Class<*>) =
            targetClass.isPrimitive || Serializable::class.java.isAssignableFrom(targetClass)

    override fun canHandleSerialisedFormat(serialised: String) =
            serialised.startsWith(PREFIX) && serialised.contains(DELIMITER)

    @Throws(Serialiser.SerialisationException::class)
    override fun <O : Any> serialise(deserialised: O): String {
        val targetClass = deserialised::class.java

        require(canHandleType(targetClass)) {
            "Cannot serialise objects of type:$targetClass"
        }

        try {
            ByteArrayOutputStream().use { bos ->
                ObjectOutputStream(bos).use {
                    it.writeObject(deserialised)
                    val valueBytes = bos.toByteArray()
                    return base64.encode(valueBytes, Base64.DEFAULT).let {
                        format(it, targetClass)
                    }
                }
            }
        } catch (e: IOException) {
            logger.e(this, e)
            throw Serialiser.SerialisationException(e)
        }
    }

    @Suppress("UNCHECKED_CAST")
    @Throws(Serialiser.SerialisationException::class)
    override fun <O> deserialise(serialised: String,
                                 targetClass: Class<O>): O {
        try {
            val read = read(serialised)
            if (targetClass != read[0]) {
                throw Serialiser.SerialisationException(
                        "Serialised class didn't match: expected: "
                                + targetClass
                                + "; serialised: "
                                + read[0]
                )
            }

            return base64.decode(read[1] as String, Base64.DEFAULT).let {
                ByteArrayInputStream(it).use { bis ->
                    ObjectInputStream(bis).use {
                        it.readObject()
                    }
                }
            } as O
        } catch (e: Exception) {
            logger.e(this, e)
            throw Serialiser.SerialisationException(e)
        }
    }

    private fun format(base64: String,
                       objectClass: Class<*>) =
            "$PREFIX${objectClass.canonicalName}$DELIMITER$base64"

    @Throws(ClassNotFoundException::class)
    private fun read(base64: String): Array<Any> {
        if (canHandleSerialisedFormat(base64)) {
            val payload = base64.substring(base64.indexOf(DELIMITER) + DELIMITER.length)
            val className = base64.substring(base64.indexOf(PREFIX) + PREFIX.length, base64.indexOf(DELIMITER))
            val targetClass = Class.forName(className)
            return arrayOf(targetClass, payload)
        }
        throw IllegalArgumentException("Not a Base64 string: $base64")
    }

    interface CustomBase64 {
        fun encode(input: ByteArray, flags: Int): String
        fun decode(input: String, flags: Int): ByteArray
    }

    private companion object {
        private const val PREFIX = "BASE_64_"
        private const val DELIMITER = "_START_DATA_"
    }
}