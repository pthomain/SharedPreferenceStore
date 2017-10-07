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

package uk.co.glass_software.android.shared_preferences.persistence.preferences;

import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import uk.co.glass_software.android.shared_preferences.Logger;

public class Base64Serialiser {
    
    private final static String delimiter = "_START_DATA_";
    private final static String prefix = "BASE_64_";
    private final Logger logger;
    
    public Base64Serialiser(Logger logger) {
        this.logger = logger;
    }
    
    boolean isBase64(Object value) {
        return value != null
               && value instanceof String
               && ((String) value).startsWith(prefix)
               && ((String) value).contains(delimiter);
    }
    
    @SuppressWarnings("unchecked")
    <O> O deserialise(String objectBase64) {
        if (objectBase64 != null) {
            String read = read(objectBase64);
            if (read != null) {
                byte[] objectBytes = Base64.decode(read, Base64.DEFAULT);
                ByteArrayInputStream bis = new ByteArrayInputStream(objectBytes);
                ObjectInput in = null;
                try {
                    in = new ObjectInputStream(bis);
                    return (O) in.readObject();
                }
                catch (Exception e) {
                    logger.e(this, e, e.getMessage());
                }
                finally {
                    try {
                        if (in != null) {
                            in.close();
                        }
                        bis.close();
                    }
                    catch (IOException e) {
                        logger.e(this, e, "An error occurred while trying to close the input stream");
                    }
                }
            }
        }
        return null;
    }
    
    String serialise(Serializable value) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(bos);
            if (value != null) {
                out.writeObject(value);
                byte[] valueBytes = bos.toByteArray();
                String base64 = new String(Base64.encode(valueBytes, Base64.DEFAULT));
                return format(base64, value.getClass());
            }
        }
        catch (IOException e) {
            logger.e(this, e, e.getMessage());
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
        return null;
    }
    
    private String format(String base64,
                          Class objectClass) {
        return prefix + objectClass.getCanonicalName() + delimiter + base64;
    }
    
    private String read(String base64) {
        if (isBase64(base64)) {
            return base64.substring(base64.indexOf(delimiter) + delimiter.length());
        }
        return null;
    }
}