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

package uk.co.glass_software.android.shared_preferences.demo;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.util.Date;
import java.util.Map;

import uk.co.glass_software.android.shared_preferences.utils.SimpleLogger;
import uk.co.glass_software.android.shared_preferences.StoreEntryFactory;
import uk.co.glass_software.android.shared_preferences.demo.model.Counter;
import uk.co.glass_software.android.shared_preferences.demo.model.LastOpenDate;
import uk.co.glass_software.android.shared_preferences.demo.model.Person;
import uk.co.glass_software.android.shared_preferences.demo.model.PersonEntry;
import uk.co.glass_software.android.shared_preferences.persistence.base.KeyValueStore;
import uk.co.glass_software.android.shared_preferences.persistence.preferences.StoreModule;

import static uk.co.glass_software.android.shared_preferences.StoreEntryFactory.DEFAULT_ENCRYPTED_PREFERENCE_NAME;


class MainPresenter {
    
    private final SharedPreferences encryptedPreferences;
    private final KeyValueStore plainTextStore;
    private final KeyValueStore encryptedStore;
    private final StoreEntryFactory storeEntryFactory;
    private final Counter counter;
    private final LastOpenDate lastOpenDate;
    private final PersonEntry personEntry;
    
    MainPresenter(Context context) {
        //used only to display encrypted values as stored on disk, should not be used directly in practice
        encryptedPreferences = StoreModule.openSharedPreferences(context, DEFAULT_ENCRYPTED_PREFERENCE_NAME);
        
        storeEntryFactory = StoreEntryFactory.builder(context)
                                             .logger(new SimpleLogger())
                                             .customSerialiser(new GsonSerialiser(new Gson()))
                                             .build();
       
        plainTextStore = storeEntryFactory.getStore();
        encryptedStore = storeEntryFactory.getEncryptedStore();
        
        counter = new Counter(plainTextStore);
        lastOpenDate = new LastOpenDate(encryptedStore);
        personEntry = new PersonEntry(encryptedStore);
        createOrUpdatePerson();
    }
    
    private void createOrUpdatePerson() {
        Date lastSeenDate = new Date();
        Person person = personEntry.get();
        
        if (person == null) {
            person = new Person();
            person.setAge(30);
            person.setFirstName("John");
            person.setName("Smith");
        }
        
        person.setLastSeenDate(lastSeenDate);
        personEntry.save(person);
    }
    
    void onPause() {
        counter.save(counter.get(1) + 1);
        lastOpenDate.save(new Date());
        createOrUpdatePerson();
    }
    
    StoreEntryFactory storeEntryFactory() {
        return storeEntryFactory;
    }
    
    Counter counter() {
        return counter;
    }
    
    LastOpenDate lastOpenDate() {
        return lastOpenDate;
    }
    
    KeyValueStore store() {
        return plainTextStore;
    }
    
    KeyValueStore encryptedStore() {
        return encryptedStore;
    }
    
    SharedPreferences encryptedPreferences() {
        return encryptedPreferences;
    }
    
    public String getKey(Map.Entry<String, ?> entry) {
        return entry.getKey();
    }
}
