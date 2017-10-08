SharedPreferenceStore
=====================

Simple access to the Android shared preferences via object mapping with support for encryption when supported by the device (supports API level 16+).

The following logic:

```java
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPref = getPreferences(Context.MODE_PRIVATE);
        defaultScore = getResources().getInteger(R.string.saved_high_score_default);
    }
    
    private void saveHighScore(int newHighScore) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(getString(R.string.saved_high_score), newHighScore);
        editor.commit();
    }
    
    private int getHighScore() {
        return sharedPref.getInt(getString(R.string.saved_high_score), defaultScore);
    }
```

can be replaced with:

```java    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        highScoreEntry = new StoreEntryFactory(this).open(getString(R.string.saved_high_score), Integer.class);
        defaultScore = getResources().getInteger(R.string.saved_high_score_default);
    }
    
    private void saveHighScore(int newHighScore) {
        highScoreEntry.save(newHighScore);
    }
    
    private int getHighScore() {
        return highScoreEntry.get(defaultScore);
    }
```

To encrypt the stored value, call ``openEncrypted(getString(R.string.saved_high_score), Integer.class)`` instead.
Make sure to call ``StoreEntryFactory.isEncryptionSupported()`` first to check.

Individual entries are represented as a ``StoreEntry`` object which can be used as a normal dependency and contains 4 methods: ``exists()``, ``get()``, ``save()`` and ``drop()``. This simplifies mocking in unit tests.

Alternatively, the ``StoreEntryFactory`` object provides 2 getters for a plain-text and an encrypted ``SharedPreferenceStore`` which provides access to all the values rather than to an individual ``StoreEntry``.

Values stored in the ``SharedPreferenceStore`` are cached in memory to improve performance, especially for the encrypted store.

All primitives are supported along with objects implementing the Serializable interface which are serialised to Base64.

Regarding keys
--------------

All values saved to the store must contain valid ``String`` keys following the Android resource name convention.
In order to preserve the uniqueness of the keys, it is recommended to use an enum implementing the ``StoreEntry.UniqueKeyProvider`` and ``StoreEntry.ValueClassProvider`` interfaces as such:

```java
enum Keys implements StoreEntry.UniqueKeyProvider, StoreEntry.ValueClassProvider {   
    HIGH_SCORE(String.class)
    
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
This way, ``openEncrypted(getString(R.string.saved_high_score), Integer.class)`` can be replaced with ``storeEntryFactory.openEncrypted(Keys.HIGH_SCORE)``.

However, if you do use such an approach, be aware that refactoring the enum's name could break the store's behaviour.
This is also why it is recommended to use ``getClass().getSimpleName()`` rather than ``getClass().getName()`` as the latter is susceptible to break during a move of the class to a different package. One way to prevent this entirely is to use an arbitrary final value for the ``prefix``.

