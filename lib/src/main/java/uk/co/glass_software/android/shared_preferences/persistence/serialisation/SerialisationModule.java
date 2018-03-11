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

import android.support.annotation.Nullable;
import android.util.Base64;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import uk.co.glass_software.android.shared_preferences.Logger;
import uk.co.glass_software.android.shared_preferences.persistence.serialisation.Base64Serialiser.CustomBase64;

@Module
public class SerialisationModule {
    
    public final static String BASE_64 = "base_64";
    public final static String CUSTOM = "custom";
    
    @Nullable
    private final Serialiser customSerialiser;
    
    public SerialisationModule(@Nullable Serialiser customSerialiser) {
        this.customSerialiser = customSerialiser;
    }
    
    @Provides
    @Singleton
    @Named(BASE_64)
    Serialiser provideBase64Serialiser(Logger logger) {
        return new Base64Serialiser(
                logger,
                new CustomBase64() {
                    @Override
                    public String encode(byte[] input, int flags) {
                        return Base64.encodeToString(input, flags);
                    }
                    
                    @Override
                    public byte[] decode(String input, int flags) {
                        return Base64.decode(input, flags);
                    }
                }
        );
    }
    
    @Provides
    @Singleton
    @Named(CUSTOM)
    @Nullable
    Serialiser provideCustomSerialiser() {
        return customSerialiser;
    }
    
}
