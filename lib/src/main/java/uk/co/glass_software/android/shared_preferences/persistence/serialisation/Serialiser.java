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

package uk.co.glass_software.android.shared_preferences.persistence.serialisation;

import android.support.annotation.NonNull;

public interface Serialiser {
    
    boolean canHandleType(@NonNull Class<?> targetClass);
    
    boolean canHandleSerialisedFormat(@NonNull String serialised);
    
    <O> String serialise(@NonNull O deserialised) throws SerialisationException;
    
    <O> O deserialise(@NonNull String serialised,
                      @NonNull Class<O> targetClass) throws SerialisationException;
    
    class SerialisationException extends Exception {
        SerialisationException(String message) {
            super(message);
        }
        
        SerialisationException(Throwable cause) {
            initCause(cause);
        }
    }
}