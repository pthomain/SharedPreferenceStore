SharedPreferenceStore  
=====================

Simple access to the Android shared preferences via object mapping with support for encryption when supported by the device (supports API level 16+).

<a href="https://play.google.com/store/apps/details?id=uk.co.glass_software.android.shared_preferences.demo"><img src="https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png" width="250"/></a>

TL;DR
-----

Encapsulate your SharedPreferences value in a DAO and use it as a dependency:

```java
@Inject
StoreEntry<String> address;

private void updateAddress() {
    address.exists();               // true or false whether a value exists in SharedPreferences
    address.get();                  // gets the saved value or null if none present 
    address.get("default address"); // gets the saved value or "default address" if not present
    address.save("my new address"); // updates/saves a new value to the SharedPreferences
    address.drop();                 // deletes the saved value
}  
```

Define your dependencies in your Dagger modules:

```java
@Provides
StoreEntryFactory provideStoreEntryFactory(Context context){
    return new StoreEntryFactory(context.getApplicationContext());
}
      
@Provides
StoreEntry<String> provideAddress(StoreEntryFactory storeEntryFactory){
    return storeEntryFactory.open(Keys.ADDRESS); // or openEncrypted(Keys.ADDRESS);
}
```

or use on the fly if you don't use dependency injection:

```java
StoreEntry<String> address = new StoreEntryFactory(context).open(Keys.ADDRESS);
```

Use ``StoreEntryFactory.open()`` to store in plain-text and ``StoreEntryFactory.openEncrypted()`` to store encrypted values (if supported by the device). The encryption is done using AES, following the method described here: https://medium.com/@ericfu/securely-storing-secrets-in-an-android-application-501f030ae5a3#.qcgaaeaso

Define your entries in an enum:

```java
enum Keys implements StoreEntry.UniqueKeyProvider, StoreEntry.ValueClassProvider {  
    FIRST_NAME(String.class),
    LAST_NAME(String.class),
    EMAIL(String.class),
    AGE(Integer.class),
    JOIN_DATE(Date.class),
    ADDRESS(String.class);
        
    private final String prefix = getClass().getSimpleName();
    private final Class<?> valueClass;
        
    Keys(Class valueClass) {
        this.valueClass = valueClass;
    }
        
    @Override
    public String getUniqueKey() {
        return prefix + "." + this; 
    }
        
    @Override
    public Class getValueClass() {
        return valueClass;
    }
}
```

Overview
--------

To encrypt the stored value, call ``openEncrypted("AGE", Integer.class)``.
**Make sure to call ``StoreEntryFactory.isEncryptionSupported()`` first to check otherwise a runtime exception will be thrown.**
Only Strings are supported for encryption, other entry types must be serialised / deserialised beforehand manually. 

Individual entries are represented as a ``StoreEntry`` object which can be used as a normal dependency and contains 4 methods: ``exists()``, ``get()``, ``save()`` and ``drop()``. This simplifies mocking in unit tests.

Alternatively, the ``StoreEntryFactory`` object provides 2 getters for a plain-text and an encrypted ``SharedPreferenceStore`` which provide access to all the values by key rather than to an individual ``StoreEntry``.

Values stored in the ``SharedPreferenceStore`` are cached in memory to improve performance, especially needed for the encrypted store. Because of this and because the cached is warmed up upon instantiation, it is recommended to instantiate the factory in the Application context and to use it as a Singleton.

All primitives are supported along with objects implementing the Serializable interface which are serialised to Base64.
Value udpates are logged in debug mode by default, the output is disabled in production (checking BuildConfig.DEBUG).

Regarding keys
--------------

All values saved to the store must contain valid ``String`` keys following the Android resource name convention.
In order to preserve the uniqueness of the keys, it is recommended to use an enum implementing the ``StoreEntry.UniqueKeyProvider`` and ``StoreEntry.ValueClassProvider`` interfaces as such:

```java
enum Keys implements StoreEntry.UniqueKeyProvider, StoreEntry.ValueClassProvider {   
    AGE(Integer.class)
    
    private final String prefix = getClass().getSimpleName();
    private final Class<?> valueClass;
    
    Keys(Class valueClass) {
        this.valueClass = valueClass;
    }
    
    @Override
    public String getUniqueKey() {
        return prefix + "." + this;
    }
    
    @Override
    public Class getValueClass() {
        return valueClass;
    }
}
```

This way, ``open("AGE", Integer.class)`` can be replaced with ``open(Keys.AGE)``. It also prevents the use of magic strings for the keys and the risk of collisions if all keys are stored in the same enum.

However, if you do use such an approach, be aware that refactoring the enum's name could break the store's behaviour.
This is also why it is recommended to use ``getClass().getSimpleName()`` rather than ``getClass().getName()`` as the latter is susceptible to break during a move of the class to a different package. One way to prevent this entirely is to use an arbitrary final value for the ``prefix``.

Adding the dependency
---------------------

To add the library to your project, add the following block to your root gradle file:

```
allprojects {
 repositories {
    jcenter()
    maven { url "https://jitpack.io" }
 }
}
 ```
 
 Then add the following dependency to your module:
 
 ```
 dependencies {
    compile 'com.github.pthomain:SharedPreferenceStore:1.0.0'
}
```
