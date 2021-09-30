package open.source.speech

import androidx.annotation.WorkerThread

abstract class SoLoader(priority: Int) : Loader(priority) {
    @WorkerThread
    abstract fun load()
}