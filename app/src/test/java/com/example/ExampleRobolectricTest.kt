package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.parser.StoryboardParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Explainer3D", appName)
    }

    @Test
    fun `test fallback storyboard generation`() {
        val storyboard = StoryboardParser.createFallbackStoryboard("Solar System")
        assertEquals("The Solar System", storyboard.title)
        assertTrue(storyboard.scenes.isNotEmpty())
    }
}
