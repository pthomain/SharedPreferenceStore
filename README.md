# SharedPreferenceStore
This library provides an object mapping of entries stored in the Android shared preferences with the option to encrypt the stored values. All primitives are supported along with objects implementing the Serializable interface which are serialised to Base64.

Individual entries are represented as a StoreEntry object which can be injected as a dependency and contains 4 methods: exists(), get(), save() and drop().
