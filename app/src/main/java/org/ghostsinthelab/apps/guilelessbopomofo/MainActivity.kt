package org.ghostsinthelab.apps.guilelessbopomofo

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipInputStream

class MainActivity : AppCompatActivity() {
    val LOGTAG = "GBMainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Example of a call to a native method
        sample_text.text = stringFromJNI()

        initChewingData()
        val chewing_engine = ChewingEngine(this.applicationInfo.dataDir)

        chewing_engine.context.let {
            Log.d(LOGTAG, "Chewing context ptr: ${it.toString()}")
        }

        val chewing_chi_mode = chewing_engine.getChiEngMode()
        chewing_chi_mode.let {
            Log.d(LOGTAG, "Chinese/English mode: ${it.toString()}")
        }

        val selKeys = arrayOf('1', '2', '3', '4', '5', '6', '7', '8', '9').map { it.toInt() }
        chewing_engine.setSelKey(selKeys, 9)

        val listKeys = chewing_engine.getSelKey()
        listKeys.let {
            Log.d(LOGTAG, "Get select key (ptr): ${it.toString()}")
        }
        chewing_engine.free(listKeys)

        chewing_engine.setMaxChiSymbolLen(10)
        chewing_engine.setCandPerPage(9)
        // set frontward phrase choice
        chewing_engine.setPhraseChoiceRearward(false)

        // 綠茶
        val keys = arrayOf('x', 'm', '4', 't', '8', '6')
        for (key in keys) {
            chewing_engine.handleDefault(key)
        }
        // move cursor to front
        chewing_engine.handleLeft()
        chewing_engine.handleLeft()
        chewing_engine.candOpen()
        chewing_engine.candTotalChoice()
        chewing_engine.candChooseByIndex(0)
        chewing_engine.commitPreeditBuf()

        val commitString = chewing_engine.commitString();
        Log.d(LOGTAG, "Commit string: ${commitString}")

        sample_text.text = commitString

        // ㄓ
        chewing_engine.handleDefault('5')
        chewing_engine.handleSpace()

        chewing_engine.candOpen()
        chewing_engine.candTotalChoice()
        chewing_engine.candChooseByIndex(12)
        chewing_engine.commitPreeditBuf()
        val selectedCandidate = chewing_engine.commitString();
        Log.d(LOGTAG, "Commit string: ${selectedCandidate}")

        // 密封膠帶 蜜蜂 交代 交待 蜂膠
        // ㄇ一ˋ
        chewing_engine.handleDefault('a')
        chewing_engine.handleDefault('u')
        chewing_engine.handleDefault('4')
        // ㄈㄥ
        chewing_engine.handleDefault('z')
        chewing_engine.handleDefault('/')
        chewing_engine.handleSpace()
        // ㄐㄧㄠ
        chewing_engine.handleDefault('r')
        chewing_engine.handleDefault('u')
        chewing_engine.handleDefault('l')
        chewing_engine.handleSpace()
        // ㄉㄞˋ
        chewing_engine.handleDefault('2')
        chewing_engine.handleDefault('9')
        chewing_engine.handleDefault('4')

        // 蜂膠
        chewing_engine.handleLeft()
        chewing_engine.handleLeft()
        chewing_engine.handleLeft()
        chewing_engine.candOpen()
        chewing_engine.candChooseByIndex(0)

        // 切換候選列表
//        chewing_engine.candListHasNext()
//        chewing_engine.candListNext()

        chewing_engine.commitPreeditBuf()
        val multiplePhrasesString = chewing_engine.commitString()
        Log.d(LOGTAG, "Commit string: ${multiplePhrasesString}")

        chewing_engine.delete()
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {
        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("chewing")
            System.loadLibrary("native-lib")
        }
    }

    private fun initChewingData() {
        // Get app data directory
        val chewingDataDir = File(this.applicationInfo.dataDir)

        // Get app assets (bundled in APK file)
        val assetManager = this.assets
        val chewingAssetsPath = String.format("%s/%s", "chewing", getAbi())

        // Extract data.zip to data directory
        val dataZipInputStream =
            ZipInputStream(assetManager.open(String.format("%s/%s", chewingAssetsPath, "data.zip")))
        var nextEntry = dataZipInputStream.nextEntry

        while (nextEntry != null) {
            try {
                Log.d(LOGTAG, nextEntry.name)
                val target =
                    File(String.format("%s/%s", chewingDataDir.absolutePath, nextEntry.name))
                val outputStream = FileOutputStream(target)
                dataZipInputStream.copyTo(outputStream)
                outputStream.close()
                nextEntry = dataZipInputStream.nextEntry
            } catch (e: java.lang.Exception) {
                e.message?.let {
                    Log.e(LOGTAG, it)
                }
            }
        }
        dataZipInputStream.close()
    }

    private fun getAbi(): String? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // on newer Android versions, we'll return only the most important Abi version
            Build.SUPPORTED_ABIS[0]
        } else {
            // on pre-Lollip versions, we got only one Abi
            Build.CPU_ABI
        }
    }
}
