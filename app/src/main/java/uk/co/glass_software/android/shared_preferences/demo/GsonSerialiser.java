package uk.co.glass_software.android.shared_preferences.demo;

import android.support.annotation.NonNull;

import com.google.gson.Gson;

import uk.co.glass_software.android.shared_preferences.persistence.preferences.Serialiser;

class GsonSerialiser implements Serialiser {
    
    private final Gson gson;
    
    GsonSerialiser(Gson gson) {
        this.gson = gson;
    }
    
    @Override
    public boolean canHandleType(@NonNull Class<?> targetClass) {
        return true;
    }
    
    @Override
    public boolean canHandleSerialisedFormat(@NonNull String serialised) {
        return true;
    }
    
    @Override
    public <O> String serialise(@NonNull O deserialised) throws SerialisationException {
        return gson.toJson(deserialised);
    }
    
    @Override
    public <O> O deserialise(@NonNull String serialised, Class<O> targetClass) throws SerialisationException {
        return gson.fromJson(serialised, targetClass);
    }
}
