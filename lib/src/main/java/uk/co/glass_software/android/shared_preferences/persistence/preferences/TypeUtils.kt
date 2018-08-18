package uk.co.glass_software.android.shared_preferences.persistence.preferences

internal object TypeUtils {

    fun isString(value: Any) = isStringClass(value.javaClass)

    fun isInt(value: Any) = isIntClass(value.javaClass)

    fun isLong(value: Any) = isLongClass(value.javaClass)

    fun isFloat(value: Any) = isFloatClass(value.javaClass)

    fun isBoolean(value: Any) = isBooleanClass(value.javaClass)

    fun isStringClass(valueClass: Class<*>) = String::class.java.isAssignableFrom(valueClass)

    fun isIntClass(valueClass: Class<*>) = (Int::class.java.isAssignableFrom(valueClass)
            || Int::class.javaPrimitiveType!!.isAssignableFrom(valueClass))

    fun isLongClass(valueClass: Class<*>) = (Long::class.java.isAssignableFrom(valueClass)
            || Long::class.javaPrimitiveType!!.isAssignableFrom(valueClass))

    fun isFloatClass(valueClass: Class<*>) = (Float::class.java.isAssignableFrom(valueClass)
            || Float::class.javaPrimitiveType!!.isAssignableFrom(valueClass))

    fun isBooleanClass(valueClass: Class<*>) = (Boolean::class.java.isAssignableFrom(valueClass)
            || Boolean::class.javaPrimitiveType!!.isAssignableFrom(valueClass))

    fun isHandled(it: Any) = isBoolean(it.javaClass)
            || isFloat(it.javaClass)
            || isLong(it.javaClass)
            || isInt(it.javaClass)
            || isString(it.javaClass)
}
