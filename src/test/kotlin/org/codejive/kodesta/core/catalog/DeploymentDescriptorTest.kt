package org.codejive.kodesta.core.catalog

import org.codejive.kodesta.core.deploy.*
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class DeploymentDescriptorTest {

    @Test
    fun `apply capabilities`() {
        val deployment = DeploymentDescriptor.build {
            applications = mutableListOf(ApplicationDescriptor.build {
                application = "dummy-app"
                parts = mutableListOf(PartDescriptor.build {
                    capabilities = mutableListOf(
                        CapabilityDescriptor.build {
                            module = "dummy"
                        }
                    )
                })
            })
        }
        withDeployment(deployment, testRegistry) {
            Assertions.assertThat(resolve("test.file")).exists()
        }
    }
}