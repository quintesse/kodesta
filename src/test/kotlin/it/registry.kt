package it

import org.codejive.kodesta.catalog.GeneratorRegistry

typealias CapabilityTestConstructor = (context: Context) -> IntegrationTests

class CapabilityTestRegistry() {
    private val _tests: MutableMap<String, CapabilityTest> = mutableMapOf()

    val tests get() = _tests.values

    fun byName(name: String) = tests.find { it.name == name }
        ?: throw IllegalArgumentException("Undefined capability test: '$name', should be one of: $tests")

    fun add(name: String, testsProvider: CapabilityTestConstructor): CapabilityTestRegistry {
        if (_tests.containsKey(name)) {
            _tests[name] = CapabilityTest(name, testsProvider)
        }
        return this
    }

    companion object {
        @JvmStatic
        val defaultRegistry: CapabilityTestRegistry by lazy {
            CapabilityTestRegistry()
//                .add("capability-database", ::DatabaseTests)
//                .add("capability-health", ::HealthTests)
//                .add("capability-rest", ::RestTests)
//                .add("capability-web-app", ::WebAppTests)
        }
    }
}

data class CapabilityTest(val name: String, val testsProvider: CapabilityTestConstructor) {
    val capInfo by lazy { GeneratorRegistry.defaultRegistry.byName(name) }
}
