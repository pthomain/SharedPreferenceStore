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

package uk.co.glass_software.android.shared_preferences.utils;


import android.util.Log;

import java.util.MissingFormatArgumentException;

import io.reactivex.android.BuildConfig;


public class SimpleLogger implements Logger {
    
    private static final int MESSAGE_LENGTH_LIMIT = 4000;
    private static boolean FORCE_STACK_TRACE_OUTPUT = false;
    private final static int STACK_TRACE_DESCRIPTION_LENGTH = 4;
    
    private final Printer printer;
    
    public interface Printer {
        void print(int priority,
                   String tag,
                   String message);
    }
    
    public SimpleLogger() {
        this(Log::println);
    }
    
    public SimpleLogger(Printer printer) {
        this.printer = printer;
    }
    
    private String getTag(Object caller) {
        if (caller instanceof String) {
            return (String) caller;
        }
        Class aClass = caller instanceof Class ? (Class) caller : caller.getClass();
        return aClass.getName();
    }
    
    @Override
    public void e(Object caller,
                  Throwable t,
                  String message) {
        e(getTag(caller), t, message, true);
    }
    
    
    public void e(Object caller,
                  Throwable t,
                  String message,
                  boolean forceOutput) {
        e(getTag(caller), t, message, forceOutput);
    }
    
    
    @Override
    public void e(Object caller,
                  String message) {
        try {
            throw new LogException(message);
        }
        catch (LogException e) {
            e(getTag(caller), e, message);
        }
    }
    
    
    @Override
    public void d(Object caller,
                  String message) {
        if (FORCE_STACK_TRACE_OUTPUT) {
            e(caller, message);
        }
        else {
            d(caller, message, false);
        }
    }
    
    
    public void d(Object caller,
                  String message,
                  boolean forceOutput) {
        d(getTag(caller), message, forceOutput);
    }
    
    
    private void e(String tag,
                   Throwable t,
                   String message) {
        e(tag, t, message, true);
    }
    
    
    private void e(String tag,
                   Throwable t,
                   String message,
                   boolean forceOutput) {
        log(Log.ERROR, tag, message, t, forceOutput);
    }
    
    
    private void d(String tag,
                   String message,
                   boolean forceOutput) {
        log(Log.DEBUG, tag, message, null, forceOutput);
    }
    
    
    private void log(int priority,
                     String tag,
                     String message,
                     Throwable throwable,
                     boolean forceOutput) {
        if (BuildConfig.DEBUG || forceOutput) {
            try {
                String file = null;
                Integer line = null;
                try {
                    throw new Exception();
                }
                catch (Exception e) {
                    StackTraceElement[] stackTrace = e.getStackTrace();
                    if (stackTrace.length > STACK_TRACE_DESCRIPTION_LENGTH) {
                        line = stackTrace[STACK_TRACE_DESCRIPTION_LENGTH].getLineNumber();
                        file = stackTrace[STACK_TRACE_DESCRIPTION_LENGTH].getFileName();
                    }
                }
                
                logInternal(priority,
                            tag,
                            " (" + file + ":" + line + ") " + message,
                            throwable
                );
            }
            catch (MissingFormatArgumentException e) {
                e(SimpleLogger.class, e, e.getMessage());
            }
        }
    }
    
    private void logInternal(int priority,
                             String tag,
                             String message,
                             Throwable throwable) {
        if (message.length() > MESSAGE_LENGTH_LIMIT) {
            printer.print(priority, tag, message.substring(0, MESSAGE_LENGTH_LIMIT));
            logInternal(priority,
                        tag,
                        message.substring(MESSAGE_LENGTH_LIMIT),
                        throwable
            );
        }
        else {
            if (throwable != null) {
                throwable.printStackTrace();
            }
        }
    }
}