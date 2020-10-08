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
        val chewing_ctx = chewingNew(this.applicationInfo.dataDir)
        chewing_ctx.let {
            Log.d(LOGTAG, it.toString())
        }

        val chewing_chi_mode = chewingGetChiEngMode(chewing_ctx)
        chewing_chi_mode.let {
            Log.d(LOGTAG, "Chinese/English mode: ${it.toString()}")
        }

        val selKeys = arrayOf('1', '2', '3', '4', '5', '6', '7', '8', '9').map { it.toInt() }
        chewingSetSelKey(chewing_ctx, selKeys, 9)

        val listKeys = chewingGetSelKey(chewing_ctx)
        listKeys.let {
            Log.d(LOGTAG, "Get select key (ptr): ${it.toString()}")
        }
        chewingFree(listKeys)

        chewingSetMaxChiSymbolLen(chewing_ctx, 10)
        chewingSetCandPerPage(chewing_ctx, 9)
        // set frontward phrase choice
        chewingSetPhraseChoiceRearward(chewing_ctx, false)

        // 綠茶
        val keys = arrayOf('x', 'm', '4', 't', '8', '6')
        for (key in keys) {
            chewingHandleDefault(chewing_ctx, key)
        }
        // move cursor to front
        chewingHandleLeft(chewing_ctx)
        chewingHandleLeft(chewing_ctx)
        chewingCandOpen(chewing_ctx)
        chewingCandTotalChoice(chewing_ctx)
        chewingCandChooseByIndex(chewing_ctx, 0)
        chewingCommitPreeditBuf(chewing_ctx)

        val commitString = chewingCommitString(chewing_ctx);
        Log.d(LOGTAG, "Commit string: ${commitString}")

        sample_text.text = commitString

        // ㄓ
        chewingHandleDefault(chewing_ctx, '5')
        chewingHandleSpace(chewing_ctx)

        chewingCandOpen(chewing_ctx)
        chewingCandTotalChoice(chewing_ctx)
        chewingCandChooseByIndex(chewing_ctx, 12)
        chewingCommitPreeditBuf(chewing_ctx)
        val selectedCandidate = chewingCommitString(chewing_ctx);
        Log.d(LOGTAG, "Commit string: ${selectedCandidate}")

        // 密封膠帶 蜜蜂 交代 交待 蜂膠
        // ㄇ一ˋ
        chewingHandleDefault(chewing_ctx, 'a')
        chewingHandleDefault(chewing_ctx, 'u')
        chewingHandleDefault(chewing_ctx, '4')
        // ㄈㄥ
        chewingHandleDefault(chewing_ctx, 'z')
        chewingHandleDefault(chewing_ctx, '/')
        chewingHandleSpace(chewing_ctx)
        // ㄐㄧㄠ
        chewingHandleDefault(chewing_ctx, 'r')
        chewingHandleDefault(chewing_ctx, 'u')
        chewingHandleDefault(chewing_ctx, 'l')
        chewingHandleSpace(chewing_ctx)
        // ㄉㄞˋ
        chewingHandleDefault(chewing_ctx, '2')
        chewingHandleDefault(chewing_ctx, '9')
        chewingHandleDefault(chewing_ctx, '4')

        chewingCommitPreeditBuf(chewing_ctx)
        val multiplePhrasesString = chewingCommitString(chewing_ctx)
        Log.d(LOGTAG, "Commit string: ${multiplePhrasesString}")

        chewingDelete(chewing_ctx)
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    external fun chewingNew(dataPath: String): Long
    external fun chewingDelete(chewingCtx: Long)
    external fun chewingFree(resourcePtr: Long)
    external fun chewingGetChiEngMode(chewingCtx: Long): Int
    external fun chewingSetSelKey(chewingCtx: Long, selKeys: List<Int>, length: Int)
    external fun chewingGetSelKey(chewingCtx: Long): Long
    external fun chewingSetMaxChiSymbolLen(chewingCtx: Long, length: Int)
    external fun chewingSetCandPerPage(chewingCtx: Long, candidates: Int)
    external fun chewingSetPhraseChoiceRearward(chewingCtx: Long, boolean: Boolean)
    external fun chewingHandleDefault(chewingCtx: Long, key: Char)
    external fun chewingHandleEnter(chewingCtx: Long)
    external fun chewingHandleSpace(chewingCtx: Long)
    external fun chewingHandleLeft(chewingCtx: Long)
    external fun chewingHandleRight(chewingCtx: Long)
    external fun chewingCommitString(chewingCtx: Long): String
    external fun chewingCommitPreeditBuf(chewingCtx: Long): Int
    external fun chewingCandOpen(chewingCtx: Long): Int
    external fun chewingCandTotalChoice(chewingCtx: Long): Int
    external fun chewingCandChooseByIndex(chewingCtx: Long, index: Int): Int


    companion object {
        // Used to load the 'native-lib' library on application startup.
        init {
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
