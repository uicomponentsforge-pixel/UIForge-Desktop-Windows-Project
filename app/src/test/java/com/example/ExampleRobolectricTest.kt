package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.BuiltInAppId
import com.example.viewmodel.DesktopViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `app_name string matches UIForge Desktop`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("UIForge Desktop", appName)
    }

    @Test
    fun `window management supports opening, focusing, minimizing, maximizing, and closing`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = DesktopViewModel(app)

        vm.openApp(BuiltInAppId.CALCULATOR)
        vm.openApp(BuiltInAppId.TEXT_EDITOR)
        assertEquals(2, vm.windows.value.size)

        val calcWin = vm.windows.value.first { it.appId == BuiltInAppId.CALCULATOR }
        val editorWin = vm.windows.value.first { it.appId == BuiltInAppId.TEXT_EDITOR }
        assertEquals(editorWin.instanceId, vm.activeWindowId.value)

        // Focus calculator brings it to front
        vm.focusWindow(calcWin.instanceId)
        assertEquals(calcWin.instanceId, vm.activeWindowId.value)

        // Minimize calculator switches focus back to editor
        vm.minimizeWindow(calcWin.instanceId)
        assertTrue(vm.windows.value.first { it.appId == BuiltInAppId.CALCULATOR }.isMinimized)
        assertEquals(editorWin.instanceId, vm.activeWindowId.value)

        // Maximize editor
        vm.toggleMaximizeWindow(editorWin.instanceId)
        assertTrue(vm.windows.value.first { it.appId == BuiltInAppId.TEXT_EDITOR }.isMaximized)

        // Minimize all shows desktop
        vm.minimizeAllWindows()
        assertTrue(vm.windows.value.all { it.isMinimized })
        assertNull(vm.activeWindowId.value)

        // Close calculator
        vm.requestCloseWindow(calcWin.instanceId)
        assertEquals(1, vm.windows.value.size)
        assertFalse(vm.windows.value.any { it.appId == BuiltInAppId.CALCULATOR })
    }
}
