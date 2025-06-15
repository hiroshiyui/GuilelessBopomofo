package org.ghostsinthelab.apps.guilelessbopomofo.keys.physical

import android.content.Context
import android.view.KeyEvent
import android.widget.Toast
import org.ghostsinthelab.apps.guilelessbopomofo.R

class CapsLock : PhysicalKeyHandler {
    override fun onKeyDown(
        context: Context, keyCode: Int, event: KeyEvent?
    ): Boolean {
        return true
    }

    override fun onKeyUp(context: Context, keyCode: Int, event: KeyEvent?): Boolean {
        event?.apply {
            if (this.isCapsLockOn) {
                Toast.makeText(context, R.string.capsLockIsOn, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, R.string.capsLockIsOff, Toast.LENGTH_SHORT).show()
            }
        }
        return true
    }
}