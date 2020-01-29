package org.codejive.kodesta.core.catalog

import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test

class InfoTest {
    @Test
    fun `have generators`() {
        assertThat(testRegistry.generators).isNotEmpty
    }

    @Test
    fun `generators have info`() {
        assertThat(
            testRegistry
                .generators
                .map { it.infoDef }
                .filter { it.type != null}).isNotEmpty
    }
}
