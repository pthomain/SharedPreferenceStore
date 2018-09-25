package uk.co.glass_software.android.shared_preferences.persistence.preferences

import android.content.Context
import uk.co.glass_software.android.boilerplate.utils.preferences.Prefs

object StoreUtils {

    fun openSharedPreferences(context: Context,
                              name: String) =
            getSharedPreferenceFactory(context)(name)

    private fun getSharedPreferenceFactory(context: Context): (String) -> Prefs = {
        Prefs.with(getStoreName(context, it))
    }

    private fun getStoreName(context: Context,
                             name: String): String {
        val availableLength = StoreModule.MAX_FILE_NAME_LENGTH - name.length
        var packageName = context.packageName

        if (packageName.length > availableLength) {
            packageName = packageName.substring(
                    packageName.length - availableLength - 1,
                    packageName.length
            )
        }

        return "$packageName$$name"
    }

}