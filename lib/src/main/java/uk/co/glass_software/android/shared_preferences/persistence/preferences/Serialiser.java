package uk.co.glass_software.android.shared_preferences.persistence.preferences;

import android.support.annotation.NonNull;

public interface Serialiser {
    
    boolean canHandleType(@NonNull Class<?> targetClass);
    
    boolean canHandleSerialisedFormat(@NonNull String serialised);
    
    <O> String serialise(@NonNull O deserialised) throws SerialisationException;
    
    <O> O deserialise(@NonNull String serialised,
                      Class<O> targetClass) throws SerialisationException;
    
    class SerialisationException extends Exception {
        SerialisationException(Throwable cause) {
            initCause(cause);
        }
    }
}