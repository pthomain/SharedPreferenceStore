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

internal object TypeUtils {

    fun isString(value: Any) = isStringClass(value.javaClass)

    fun isInt(value: Any) = isIntClass(value.javaClass)

    fun isLong(value: Any) = isLongClass(value.javaClass)

    fun isFloat(value: Any) = isFloatClass(value.javaClass)

    fun isBoolean(value: Any) = isBooleanClass(value.javaClass)

    fun isStringClass(valueClass: Class<*>) = String::class.java.isAssignableFrom(valueClass)

    fun isIntClass(valueClass: Class<*>) = (
            Int::class.java.isAssignableFrom(valueClass)
                    || Integer::class.java.isAssignableFrom(valueClass)
                    || Int::class.javaPrimitiveType!!.isAssignableFrom(valueClass)
            )

    fun isLongClass(valueClass: Class<*>) = (Long::class.java.isAssignableFrom(valueClass)
            || Long::class.javaPrimitiveType!!.isAssignableFrom(valueClass))

    fun isFloatClass(valueClass: Class<*>) = (Float::class.java.isAssignableFrom(valueClass)
            || Float::class.javaPrimitiveType!!.isAssignableFrom(valueClass))

    fun isBooleanClass(valueClass: Class<*>) = (Boolean::class.java.isAssignableFrom(valueClass)
            || Boolean::class.javaPrimitiveType!!.isAssignableFrom(valueClass))

    fun isHandled(it: Any) = isHandledClass(it.javaClass)

    fun isHandledClass(it: Class<*>) = isBooleanClass(it)
            || isFloatClass(it)
            || isLongClass(it)
            || isIntClass(it)
            || isStringClass(it)
}
