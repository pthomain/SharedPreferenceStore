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
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import uk.co.glass_software.android.shared_preferences.Logger;

public class Base64Serialiser implements Serialiser {
    
    private final static String PREFIX = "BASE_64_";
    private final static String DELIMITER = "_START_DATA_";
    private final Logger logger;
    private final CustomBase64 base64;
    
    Base64Serialiser(Logger logger,
                     CustomBase64 base64) {
        this.logger = logger;
        this.base64 = base64;
    }
    
    @Override
    public boolean canHandleType(@NonNull Class<?> targetClass) {
        return Serializable.class.isAssignableFrom(targetClass);
    }
    
    @Override
    public boolean canHandleSerialisedFormat(@NonNull String serialised) {
        return serialised.startsWith(PREFIX) && serialised.contains(DELIMITER);
    }
    
    @Override
    public <O> String serialise(@NonNull O deserialised) throws SerialisationException {
        if (!canHandleType(deserialised.getClass())) {
            throw new IllegalArgumentException("Cannot serialise objects of type:" + deserialised.getClass());
        }
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(deserialised);
            byte[] valueBytes = bos.toByteArray();
            String base64 = this.base64.encode(valueBytes, Base64.DEFAULT);
            return format(base64, deserialised.getClass());
        }
        catch (IOException e) {
            logger.e(this, e, e.getMessage());
            throw new SerialisationException(e);
        }
        finally {
            if (out != null) {
                try {
                    out.close();
                    bos.close();
                }
                catch (IOException e) {
                    logger.e(this, e, "An error occurred while trying to close the output stream");
                }
            }
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <O> O deserialise(@NonNull String objectBase64,
                             @NonNull Class<O> targetClass) throws SerialisationException {
        ByteArrayInputStream bis = null;
        ObjectInput in = null;
        
        try {
            Object[] read = read(objectBase64);
            
            if (!targetClass.equals(read[0])) {
                throw new SerialisationException(
                        "Serialised class didn't match: expected: "
                        + targetClass
                        + "; serialised: "
                        + read[0]
                );
            }
            
            byte[] objectBytes = base64.decode((String) read[1], Base64.DEFAULT);
            bis = new ByteArrayInputStream(objectBytes);
            in = new ObjectInputStream(bis);
            return (O) in.readObject();
        }
        catch (Exception e) {
            logger.e(this, e, e.getMessage());
            throw new SerialisationException(e);
        }
        finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (bis != null) {
                    bis.close();
                }
            }
            catch (IOException e) {
                logger.e(this, e, "An error occurred while trying to close the input stream");
            }
        }
    }
    
    private String format(String base64,
                          Class objectClass) {
        return PREFIX + objectClass.getCanonicalName() + DELIMITER + base64;
    }
    
    private Object[] read(@NonNull String base64) throws ClassNotFoundException {
        if (canHandleSerialisedFormat(base64)) {
            String payload = base64.substring(base64.indexOf(DELIMITER) + DELIMITER.length());
            String className = base64.substring(base64.indexOf(PREFIX) + PREFIX.length(), base64.indexOf(DELIMITER));
            Class<?> targetClass = Class.forName(className);
            return new Object[]{targetClass, payload};
        }
        throw new IllegalArgumentException("Not a Base64 string: " + base64);
    }
    
    interface CustomBase64 {
        
        String encode(byte[] input, int flags);
        
        byte[] decode(String str, int flags);
        
    }
}