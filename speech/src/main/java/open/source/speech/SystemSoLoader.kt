package open.source.speech

internal class SystemSoLoader : SoLoader(Int.MIN_VALUE) {
    override fun load() {
        System.loadLibrary("libdeepspeech_vad_jni")
        System.loadLibrary("libdeepspeech")
        System.loadLibrary("libdeepspeech_jni")
    }
}