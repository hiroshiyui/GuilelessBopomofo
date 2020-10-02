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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Example of a call to a native method
        sample_text.text = stringFromJNI()
        initChewingData()
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {
        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }

    private fun initChewingData() {
        // Get app data directory
        val applicationInfo = this.getApplicationInfo()
        val chewingDataDir = File(applicationInfo.dataDir)

        // Get app assets (bundled in APK file)
        val assetManager = this.assets
        val chewingAssetsPath = String.format("%s/%s", "chewing", getAbi())

        // Extract data.zip to data directory
        val dataZipInputStream = ZipInputStream(assetManager.open(String.format("%s/%s", chewingAssetsPath, "data.zip")))
        var nextEntry = dataZipInputStream.nextEntry

        while (nextEntry != null) {
            try {
                Log.d("extract-zip", nextEntry.name)
                val target = File(String.format("%s/%s", chewingDataDir.absolutePath, nextEntry.name))
                val outputStream = FileOutputStream(target)
                dataZipInputStream.copyTo(outputStream)
                outputStream.close()
                nextEntry = dataZipInputStream.nextEntry
            } catch (e: java.lang.Exception) {
                Log.e("extract-zip", e.message)
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
