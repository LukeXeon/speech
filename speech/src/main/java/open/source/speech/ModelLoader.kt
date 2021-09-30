package open.source.speech

import androidx.annotation.WorkerThread
import java.io.File
import java.util.*

abstract class ModelLoader(priority: Int) : Loader(priority) {
    @WorkerThread
    protected abstract fun loadFiles(language: String): File

    internal fun loadModel(language: String): ModelFile {
        return ModelFile(loadFiles(language))
    }
}