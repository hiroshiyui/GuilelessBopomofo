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
        chewing_ctx?.let {
            Log.d(LOGTAG, it.toString())
        }
        chewingDelete(chewing_ctx)
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    external fun chewingNew(dataPath: String): Long
    external fun chewingDelete(chewingCtx: Long)

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
        val dataZipInputStream = ZipInputStream(assetManager.open(String.format("%s/%s", chewingAssetsPath, "data.zip")))
        var nextEntry = dataZipInputStream.nextEntry

        while (nextEntry != null) {
            try {
                Log.d(LOGTAG, nextEntry.name)
                val target = File(String.format("%s/%s", chewingDataDir.absolutePath, nextEntry.name))
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
