SharedPreferenceStore  
=====================

DAO object mapping of the Android SharedPreferences with support for encryption when available on device (API level 16+).

TL;DR
-----

Encapsulate your SharedPreferences value in a DAO and use it as a dependency:

```java
@Inject
KeyValueEntry<Address> addressEntry;

private void updateAddress() {
    addressEntry.exists();               // true or false whether a value exists in SharedPreferences
    addressEntry.get();                  // gets the saved value or null if none present 
    addressEntry.get("default address"); // gets the saved value or "default address" if not present
    addressEntry.save("my new address"); // updates/saves a new value to the SharedPreferences
    addressEntry.drop();                 // deletes the saved value
}  
```

Define your dependencies in your Dagger modules:

```java
@Provides
@Singleton
StoreEntryFactory provideStoreEntryFactory(Context context){
    return StoreEntryFactory.buildDefault(context);
}
     
@Provides
@Singleton
KeyValueEntry<String> provideAddressEntry(StoreEntryFactory storeEntryFactory){
    return storeEntryFactory.open(
        "address_key",
        Address.class,
        StoreMode.PLAIN_TEXT    //or StoreMode.ENCRYPTED
    );
}
```

If you don't use dependency injection:

```java
KeyValueEntry<Address> address = StoreEntryFactory.buildDefault(context)
                                                  .open("address_key", 
                                                        Address.class, 
                                                        StoreMode.PLAIN_TEXT);
```

Injecting `KeyValueEntry<T>` dependencies this way is faster but means that you will have to use the `@Named` annotation to differentiate them. A better solution is to use a specific type for your entry. See the [Unique entry types](#unique-entry-types) section for how to do this.

Encryption is available using Facebook's Conceal API (https://github.com/facebook/conceal). 
**Make sure to call ``StoreEntryFactory.isEncryptionSupported()`` first to check otherwise a runtime exception will be thrown.** 

See [Supported modes](#supported-modes) for storage options. 

Adding the dependency [![](https://jitpack.io/v/pthomain/SharedPreferenceStore.svg)](https://jitpack.io/#pthomain/SharedPreferenceStore)
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
    compile 'com.github.pthomain:SharedPreferenceStore:1.0.9'
}
```

To see the library in action, download the app.

<a href="https://play.google.com/store/apps/details?id=uk.co.glass_software.android.shared_preferences.demo"><img src="https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png" width="200"/></a>

Overview
--------

Individual entries are represented as a ``KeyValueEntry<T>`` object which can be used as a normal dependency and contains 4 methods: ``exists()``, ``get()``, ``save()`` and ``drop()``. This provides a strongly typed way to access your shared preferences. 

Alternatively, the ``StoreEntryFactory`` object provides 2 getters for a plain-text and encrypted ``SharedPreferenceStore`` which provide access to all the values by key.

Another getter on ``StoreEntryFactory`` provide access to the ``EncryptionManager`` which exposes methods for encryption on the fly of ``String`` and ``byte[]`` arguments.

Values returned by ``StoreEntryFactory`` are cached in memory to improve performance (lazy-loaded). Because of this, it is recommended to instantiate the factory in the Application context and to use it as a Singleton.

All the values handled by ``SharedPreferences`` are supported by default along with objects implementing the ``Serializable`` interface which are serialised to Base64 using the default Java mechanism.

The builder takes an optional ``CustomSerialiser`` object to handle serialisation of custom types. For instance, one can provide a custom serialiser using ``Gson`` to serialise to JSON. See ``GsonSerialiser`` in the app module for an example implementation. The provided custom serialiser takes precedence over the default provided serialisation mechanism.

Value udpates are logged in the console in debug mode by default, the output is disabled in production (checking ``BuildConfig.DEBUG``).

Supported modes
---------------

There are 4 options for storing values represented by the ``StoreMode`` enum:

* ``PLAIN_TEXT``: stores values as is or simply serialised for custom types
* ``ENCRYPTED``: stores values encrypted using the Conceal lib, throws an exception if the values can't be encrypted
* ``LENIENT``: attempts to encrypt the values before storing them but falls back to plain-text silently if encryption fails
* ``FORGETFUL``: attempts to encrypt the values before storing them but won't store them if encryption fails

``StoreEntryFactory.isEncryptionSupported()`` indicates whether or not encryption is supported on the device. In practice, encryption is supported down to API 16 but some devices might not support it.

Regarding keys
--------------

All values saved to the store must contain valid ``String`` keys following the Android resource name convention.
``StoreEntryFactory`` can open any stored entry using 3 arguments:

* a ``String`` key
* a ``StoreMode`` representing whether the value is encrypted or not (see [Supported modes](#supported-modes))
* a ``Class<T>`` representing the type of the stored value

As such the call to open the address entry defined earlier is ``storeEntryFactory.open("address", StoreMode.PLAIN_TEXT, Address.class)``.

For convenience and to ensure that no attempt is made to read entries with either the wrong type or mode, which could result in an exception being thrown, a ``StoreKey`` object can be used. It encapsulates those 3 values and can be associated with the entry via an enum.

```java
public enum Keys {

    ADDRESS(StoreMode.PLAIN_TEXT, Address.class);
    
    public final StoreKey key;
    
    Keys(StoreMode mode, Class valueClass) {
        key = new StoreKey(this, mode, valueClass);
    } 
    
}
```

The call can then be replaced with ``storeEntryFactory.open(Keys.Address.key)``. 
``StoreKey`` automatically generates ``String`` keys based on the name of the enum. This approach also prevents the use of magic strings for the keys and the risk of accidental collisions.

Unique entry types
------------------

Rather than injecting a `KeyValueEntry` as:

```java
@Inject 
@Name("addressEntry") 
KeyValueEntry<Address> addressEntry
```
you might want to create an `AddressEntry` object extending from `StoreEntry` and thus inject it as

```java
@Inject 
AddressEntry addressEntry
```

To do so, you can declare `AddressEntry` as:

```java
public class AddressEntry extends StoreEntry<Address> {
    public AddressEntry(KeyValueStore store) {
        super(store, StoreKey.ADDRESS);
    }
}
```

and set your injection up as such:

```java
public class PersistenceModule {
    private final Context context;
    
    public PersistenceModule(Context context) {
        this.context = context.getApplicationContext();
    }
    
    @Provides
    StoreEntryFactory provideStoreEntryFactory() {
        return StoreEntryFactory.buildDefault(context);
    }
    
    @Provides
    SharedPreferenceStore provideSharedPreferenceStore(StoreEntryFactory factory) {
        return factory.getStore();
    }
    
    @Provides
    EncryptedSharedPreferenceStore provideEncryptedSharedPreferenceStore(StoreEntryFactory factory) {
        return factory.getEncryptedStore();
    }
    
    @Provides
    AddressEntry provideAddressEntry(SharedPreferenceStore store) {
        return new AddressEntry(store);
    } 
}
```

If you want to have an encrypted entry, use `EncryptedSharedPreferenceStore` returned by `StoreEntryFactory.getEncryptedStore()` instead of `SharedPreferenceStore` as a the `KeyValueStore` constructor parameter for your `StoreEntry`.

Using this library with existing SharedPreferences
--------------------------------------------------

This library can be used on top of an existing implementation of `SharedPreferences`, simply provide your existing `SharedPreferences` file to the builder as such:

```java
    StoreEntryFactory factory = StoreEntryFactory.builder(context)
                                                 .plainTextPreferences(yourExistingPreferenceFile)
                                                 .build();
                                                 
    KeyValueEntry<Integer> oldEntry = factory.open("old_entry_key", StoreMode.PLAIN_TEXT, Integer.class);                                             
```

If you prefer using `StoreKey`:

```java
public class OldStoreKey extends StoreKey {
    
    private final String oldKey;
    
    private OldStoreKey(String oldKey,
                        Class valueClass) {
        super(null, StoreMode.PLAIN_TEXT, valueClass);
        this.oldKey = oldKey;
    }
    
    @Override
    public String getUniqueKey() {
        return oldKey;
    }
    
    public enum Values {
        
        OLD_ENTRY("old_entry_key", Integer.class);
        
        public final OldStoreKey key;
    
        Values(String stringKey,
               Class valueClass) {
            key = new OldStoreKey(stringKey, valueClass);
        }
        
    }
}
```

Then:

```java
    KeyValueEntry<Integer> oldEntry = factory.open(OLD_ENTRY.key);
```

Using this library with a different persistence model
-----------------------------------------------------

This library provides support for `SharedPreferences` but is agnostic as to the preferred persistence solution used and could be used with a any implementation of the `KeyValueStore` interface.
For instance, a separate implementation could be provided using a file storage persistence mechanism. The choice and implementation of this alternate model is left to the developer.
