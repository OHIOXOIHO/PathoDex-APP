package com.example

import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.example.util.DeepLinks
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * پوشش نسخه‌ی [android.net.Uri] از مسیریابی پیوند عمیق.
 *
 * منطق خالص در [PathologyParityTest] آزمون می‌شود؛ این کلاس فقط اطمینان می‌دهد
 * که لایه‌ی نازک Uri میزبان و بخش‌های مسیر را درست به همان منطق می‌دهد.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DeepLinksUriTest {

    @Test
    fun `parses a real website disease url`() {
        val link = DeepLinks.parse(
            Uri.parse("https://pathodex.ir/disease/1-%D8%B9%D9%81%D9%88%D9%86%D8%AA-staphylococcal-infections")
        )
        assertEquals("disease", link?.route)
        assertEquals(1, link?.id)
    }

    @Test
    fun `parses a category url with a persian slug`() {
        val link = DeepLinks.parse(Uri.parse("https://pathodex.ir/category/باکتریایی"))
        assertEquals("category", link?.route)
        assertEquals("باکتریایی", link?.slug)
    }

    @Test
    fun `parses a taxonomy url`() {
        assertEquals("taxonomy", DeepLinks.parse(Uri.parse("https://pathodex.ir/taxonomy"))?.route)
    }

    @Test
    fun `rejects a foreign host`() {
        assertNull(DeepLinks.parse(Uri.parse("https://evil.example/disease/1-x")))
    }

    @Test
    fun `rejects a null uri`() {
        assertNull(DeepLinks.parse(null))
    }

    @Test
    fun `context is available so the app can resolve the user link preference`() {
        // سازگاری با محیط Robolectric و اطمینان از دسترسی به Context
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        assertEquals("pathodex.ir", context.packageName)
    }
}
