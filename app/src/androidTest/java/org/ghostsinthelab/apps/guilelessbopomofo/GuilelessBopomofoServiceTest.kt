/*
 * Guileless Bopomofo
 * Copyright (C) 2021 YOU, HUI-HONG
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.ghostsinthelab.apps.guilelessbopomofo

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ServiceTestRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito

private const val PACKAGE_NAME = "org.ghostsinthelab.apps.guilelessbopomofo"
private const val LAUNCH_TIMEOUT = 5000L

@RunWith(AndroidJUnit4::class)
@LargeTest
class GuilelessBopomofoServiceTest {

    @get:Rule
    val serviceRule = ServiceTestRule()

    private lateinit var uiDevice: UiDevice
    private lateinit var service: GuilelessBopomofoService // Instance of your service
    private lateinit var originalImeId: String

    @Before
    fun setUp() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        uiDevice = UiDevice.getInstance(instrumentation)
        val context = ApplicationProvider.getApplicationContext<Context>()

        // 1. Store the original IME and switch to your IME
        originalImeId =
            Settings.Secure.getString(context.contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)
        val myImeId =
            "${context.packageName}/.GuilelessBopomofoService" // Construct your IME's component name
        setSystemIme(myImeId)

        // 2. Start and bind to the service
        // This allows you to call methods on your service instance directly
        val intent = Intent(context, GuilelessBopomofoService::class.java)
        val binder = serviceRule.bindService(intent) as GuilelessBopomofoService.LocalBinder
        // You might need to cast the binder to your service's specific binder if you have one,
        // or find a way to get the service instance.
        // A common pattern is for the service's onBind to return a Binder that has a getService() method.
        // For simplicity here, let's assume you have a way to get it (e.g., serviceRule.service after binding,
        // or through a static method if absolutely necessary - though not ideal).
        // This part can be tricky and depends on how your service is structured for binding.
        // Often for IMEs, direct binding for calls is less common than UI Automator interaction.

        // For now, let's assume we can get it or we'll focus on UI Automator interactions.
        service = (binder as? GuilelessBopomofoService.LocalBinder)?.getService()
            ?: throw IllegalStateException("Could not get service instance")
        // If your service doesn't use a local binder for testing, you might need to rely on EventBus or other mechanisms
        // to verify internal states, or focus on black-box UI testing.

        // 3. Ensure an input field is focused (e.g., open a test app or a system app with an EditText)
        // For simplicity, this example doesn't launch a specific app, but a real test would.
        uiDevice.pressHome()
        val launcherPackage = uiDevice.launcherPackageName
        uiDevice.wait(Until.hasObject(By.pkg(launcherPackage).depth(0)), 5000)
        // // Launch a simple app with an EditText or find one on screen
    }

    private fun setSystemIme(imeId: String) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        // This command requires shell permissions, which instrumentation tests usually have.
        instrumentation.uiAutomation.executeShellCommand("ime set $imeId")
        Thread.sleep(1000) // Give the system a moment to switch
    }

    @Test
    fun testServiceStartsAndKeyboardAppears() {
        // This test is highly dependent on having an EditText focused.
        // For a real test, you'd launch an activity with an EditText.
        // Example:
        // launchTestActivityWithEditText() // A helper to start an activity

        // Bring up an EditText (e.g., in a settings search bar, or your own test app)
        // This is a placeholder for actual UI interaction
        uiDevice.pressSearch() // Example: Opens a search bar in some launchers
        Thread.sleep(2000) // Wait for UI

        // Check if your keyboard view is visible using UI Automator
        // You'll need resource IDs or descriptive text for your keyboard elements
        val keyboardView = uiDevice.wait(
            Until.hasObject(
                By.res(
                    ApplicationProvider.getApplicationContext<Context>().packageName,
                    "keyboard_panel_root_id"
                )
            ), 5000
        ) // Replace with your actual root ID
        assert(keyboardView != null) { "Keyboard panel did not appear." }

        // You can also try to get an instance of your service if bound:
        // This assumes serviceRule.service provides the instance after bindService is called successfully
        // and if your service's onBind returns a binder that allows access to the service instance.
        // val boundService = serviceRule.service as? GuilelessBopomofoService
        // assert(boundService != null)
        // if (boundService != null) {
        //     // You could check properties of the boundService here
        // }
    }

    @Test
    fun testCharacterInput() {
        // 1. Focus an EditText (as above)
        // 2. Mock an InputConnection
        val mockInputConnection = Mockito.mock(InputConnection::class.java)

        // 3. Simulate calling onStartInputView and pass the mockInputConnection
        // This is tricky without direct access to the service instance and its methods in a controlled way.
        // If you can get the service instance:
        service.onStartInput(EditorInfo(), true) // With a dummy EditorInfo
//        service.currentInputConnection = mockInputConnection // If you can set it for test

        // OR, use UI Automator to tap keys on your soft keyboard
        val keyA = uiDevice.wait(
            Until.hasObject(By.desc("a_key_content_description")),
            5000
        ) // Use content description or resource ID
//        keyA?.click()

        // 4. Verify that the correct text was committed to the InputConnection
        // If using mockInputConnection directly:
        // Mockito.verify(mockInputConnection).commitText(Mockito.eq("a"), Mockito.eq(1))
        // If using UI Automator to check an actual EditText:
        // val editText = uiDevice.findObject(By.clazz("android.widget.EditText"))
        // assertEquals("a", editText.text)
    }

    // More tests:
    // - Test switching layouts (e.g., to symbols, to English)
    // - Test candidate selection
    // - Test preference changes (e.g., toggling compact layout via Escape+Alt)
    // - Test physical keyboard interactions if possible (though harder to simulate exact physical events)

    @After
    fun tearDown() {
        // Restore original IME
        setSystemIme(originalImeId)
        // serviceRule.unbindService() // ServiceTestRule usually handles unbinding
    }
}