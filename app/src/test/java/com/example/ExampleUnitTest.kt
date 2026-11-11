package com.example

import com.example.data.model.Disease
import com.example.data.model.DiseaseImage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testCriticalImageUrlRule() {
        // Critical backend specification: relative paths starting with /uploads/ must be prepended with https://pathodex.ir
        val relativeImage = DiseaseImage(id = 1, type = "pathogen", url = "/uploads/171000-abc.jpg")
        assertEquals("https://pathodex.ir/uploads/171000-abc.jpg", relativeImage.resolvedUrl)

        val fullImage = DiseaseImage(id = 2, type = "symptom", url = "https://example.com/photo.png")
        assertEquals("https://example.com/photo.png", fullImage.resolvedUrl)
    }

    @Test
    fun testDiseaseModelIntegrity() {
        val testDisease = Disease(
            id = 1,
            category = "باکتری‌ها",
            name_fa = "سیاه‌زخم",
            name_en = "Anthrax",
            agent_fa = "باسیلوس آنتراسیس",
            agent_en = "Bacillus anthracis",
            description_fa = "توضیحات بالینی بیماری"
        )
        assertEquals(1, testDisease.id)
        assertEquals("سیاه‌زخم", testDisease.name_fa)
        assertEquals("Anthrax", testDisease.name_en)
        assertTrue(testDisease.description_fa.isNotBlank())
    }

    @Test
    fun testSearchFiltering() {
        val testList = listOf(
            Disease(
                id = 1,
                category = "باکتری‌ها",
                name_fa = "سیاه‌زخم",
                name_en = "Anthrax",
                agent_fa = "باسیلوس آنتراسیس",
                description_fa = "توضیحات"
            )
        )
        val query = "آنتراسیس"
        val matched = testList.filter {
            "${it.name_fa} ${it.name_en.orEmpty()} ${it.agent_fa} ${it.agent_en.orEmpty()}".contains(query)
        }
        assertTrue("Should match Anthrax", matched.any { it.name_en == "Anthrax" })
    }
}
