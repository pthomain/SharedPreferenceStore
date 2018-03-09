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

package uk.co.glass_software.android.shared_preferences.encryption.manager;

import android.support.annotation.Nullable;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import uk.co.glass_software.android.shared_preferences.Logger;
import uk.co.glass_software.android.shared_preferences.encryption.manager.conceal.ConcealEncryptionManager;
import uk.co.glass_software.android.shared_preferences.encryption.manager.conceal.ConcealModule;
import uk.co.glass_software.android.shared_preferences.encryption.manager.custom.CustomModule;
import uk.co.glass_software.android.shared_preferences.encryption.manager.custom.PostMEncryptionManager;
import uk.co.glass_software.android.shared_preferences.encryption.manager.custom.PreMEncryptionManager;

@Module(includes = {
        ConcealModule.class,
        CustomModule.class
})
public class EncryptionManagerModule {
    
    private final boolean fallbackToCustomEncryption;
    
    public EncryptionManagerModule(boolean fallbackToCustomEncryption) {
        this.fallbackToCustomEncryption = fallbackToCustomEncryption;
    }
    
    @Provides
    @Singleton
    @Nullable
    EncryptionManager provideDefaultEncryptionManager(@Nullable PreMEncryptionManager preMEncryptionManager,
                                                      @Nullable PostMEncryptionManager postMEncryptionManager,
                                                      ConcealEncryptionManager concealEncryptionManager,
                                                      Logger logger) {
        boolean isEncryptionSupported = concealEncryptionManager.isEncryptionSupported();
        
        if (isEncryptionSupported || !fallbackToCustomEncryption) {
            if (isEncryptionSupported) {
                logger.d(this, "Using Conceal encryption manager");
            }
            else {
                logger.e(this, "Encryption is NOT supported: Conceal is not available and there is no fallback to custom encryption as per config");
            }
            return concealEncryptionManager;
        }
        else if (postMEncryptionManager != null) {
            logger.d(this, "Using post-M encryption manager");
            return postMEncryptionManager;
        }
        else {
            logger.d(this, "Using pre-M encryption manager");
            return preMEncryptionManager;
        }
    }
    
}
