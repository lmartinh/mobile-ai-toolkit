package dev.mobileai.toolkit.report

import kotlin.test.Test
import kotlin.test.assertEquals

class GuardrailFindingTest {
    @Test
    fun `severity parsing falls back to info`() {
        assertEquals(FindingSeverity.ERROR, FindingSeverity.from("error"))
        assertEquals(FindingSeverity.WARNING, FindingSeverity.from("warning"))
        assertEquals(FindingSeverity.INFO, FindingSeverity.from("info"))
        assertEquals(FindingSeverity.INFO, FindingSeverity.from("unknown"))
    }
}
