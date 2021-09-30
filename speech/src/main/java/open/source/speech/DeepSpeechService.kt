package open.source.speech

import android.content.Intent
import android.os.Handler
import android.os.HandlerThread
import android.os.Process
import android.speech.RecognitionService
import android.speech.SpeechRecognizer
import android.util.Log
import org.mozilla.deepspeech.libdeepspeech.DeepSpeechModel
import org.mozilla.deepspeech.libdeepspeech.DeepSpeechStreamingState

class DeepSpeechService : RecognitionService() {

    private enum class ServiceState {
        Initialize,
        SoReady,
        ModelReady,
        Abort,
        Error
    }

    private lateinit var modelLoader: ModelLoader
    private lateinit var executor: Handler
    private lateinit var mainThread: Handler
    private var language: String? = null
    private var model: DeepSpeechModel? = null
    private var streamingState: DeepSpeechStreamingState? = null
    private var callback: Callback? = null
    private var state = ServiceState.Initialize

    override fun onCreate() {
        super.onCreate()
        val thread = HandlerThread(
            "${TAG}Thread",
            Process.THREAD_PRIORITY_AUDIO
        )
        thread.start()
        executor = Handler(thread.looper)
        mainThread = Handler(mainLooper)
        executor.post {
            try {
                Loader.find(this, SoLoader::class.java) { SystemSoLoader() }.load()
                modelLoader = Loader.find(this, ModelLoader::class.java)
                mainThread.post {
                    state = ServiceState.SoReady
                    callback?.readyForSpeech(null)
                }
            } catch (e: Throwable) {
                mainThread.post {
                    state = ServiceState.Abort
                    Log.e(TAG, "SoLoader::load()", e)
                    callback?.error(SpeechRecognizer.ERROR_SERVER)
                    stopSelf()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        executor.looper.quit()
    }

    override fun onStartListening(recognizerIntent: Intent, listener: Callback) {
        val inLanguage = recognizerIntent.extras
            ?.getString(KEY_LANGUAGE)
        if (inLanguage.isNullOrEmpty()) {
            state = ServiceState.Error

        } else if (state == ServiceState.ModelReady && language == inLanguage) {
            listener.readyForSpeech(null)
        } else {
            executor.post {
                val file = modelLoader.loadModel(inLanguage)

            }
        }
        callback = listener
    }

    override fun onStopListening(listener: Callback) {
        callback = listener
    }

    override fun onCancel(listener: Callback) {
        callback = null
    }

    companion object {
        internal const val TAG = "DeepSpeech"
        internal const val PREFERENCES_NAME = "${TAG}Preferences"
        const val KEY_LANGUAGE = "language"
    }
}