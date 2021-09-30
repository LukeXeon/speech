package open.source.speech

import android.content.Context
import androidx.annotation.RestrictTo
import androidx.annotation.WorkerThread
import java.util.*

@RestrictTo(RestrictTo.Scope.LIBRARY)
abstract class Loader(val priority: Int) {

    companion object {

        @WorkerThread
        internal fun <T : Loader> find(
            context: Context,
            type: Class<T>,
            default: () -> T = { throw ClassNotFoundException() }
        ): T {
            var loader: T? = null
            val preferences = context.getSharedPreferences(
                DeepSpeechService.PREFERENCES_NAME,
                Context.MODE_PRIVATE
            )
            if (preferences.contains(type.name)) {
                loader = runCatching {
                    @Suppress("UNCHECKED_CAST")
                    Class.forName(
                        requireNotNull(
                            preferences.getString(
                                type.name,
                                null
                            )
                        )
                    ).newInstance() as T
                }.getOrNull()
            }
            if (loader == null) {
                loader = ServiceLoader.load(type)
                    .iterator()
                    .asSequence()
                    .maxByOrNull { it.priority }
                    ?: default()
                preferences.edit()
                    .putString(
                        type.name,
                        loader.javaClass.name
                    ).apply()
            }
            return loader
        }
    }
}