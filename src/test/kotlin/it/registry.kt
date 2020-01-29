package it

import org.codejive.kodesta.catalog.GeneratorRegistry

typealias GeneratorTestConstructor = (context: Context) -> IntegrationTests

class GeneratorTestRegistry() {
    private val _tests: MutableMap<String, GeneratorTest> = mutableMapOf()

    val tests get() = _tests.values

    fun byName(name: String) = tests.find { it.name == name }
        ?: throw IllegalArgumentException("Undefined generator test: '$name', should be one of: $tests")

    fun add(name: String, testsProvider: GeneratorTestConstructor): GeneratorTestRegistry {
        if (_tests.containsKey(name)) {
            _tests[name] = GeneratorTest(name, testsProvider)
        }
        return this
    }

    companion object {
        @JvmStatic
        val defaultRegistry: GeneratorTestRegistry by lazy {
            GeneratorTestRegistry()
//                .add("capability-database", ::DatabaseTests)
//                .add("capability-health", ::HealthTests)
//                .add("capability-rest", ::RestTests)
//                .add("capability-web-app", ::WebAppTests)
        }
    }
}

data class GeneratorTest(val name: String, val testsProvider: GeneratorTestConstructor) {
    val capInfo by lazy { GeneratorRegistry.defaultRegistry.byName(name) }
}
