package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.actions.SystemAction
import com.example.ai.CommandParser
import com.example.apps.AppManager
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CommandParserTest {

    private lateinit var commandParser: CommandParser

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appManager = AppManager(context)
        commandParser = CommandParser(appManager)
    }

    @Test
    fun testSettingsCommands() {
        val resultEn = commandParser.parse("open settings")
        assertNotNull(resultEn)
        assertTrue(resultEn?.action is SystemAction.OpenSettings)

        val resultHi = commandParser.parse("सेटिंग्स खोलो")
        assertNotNull(resultHi)
        assertTrue(resultHi?.action is SystemAction.OpenSettings)
    }

    @Test
    fun testWifiCommands() {
        val result = commandParser.parse("open wifi")
        assertNotNull(result)
        assertTrue(result?.action is SystemAction.OpenWifiSettings)
    }

    @Test
    fun testCameraCommands() {
        val result = commandParser.parse("कैमरा खोलो")
        assertNotNull(result)
        assertTrue(result?.action is SystemAction.OpenCamera)
    }
}
