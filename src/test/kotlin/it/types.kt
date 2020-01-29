package it

import org.codejive.kodesta.core.BaseProperties
import org.codejive.kodesta.core.Properties
import org.codejive.kodesta.core.Runtime
import org.codejive.kodesta.core.propsOf
import org.junit.jupiter.api.DynamicNode

interface IntegrationTests {
    fun integrationTests(): Iterable<DynamicNode>
}

typealias GeneratorOptions = Map<String, List<Properties>>

data class GeneratorOpts(val name: String, val opts: Properties = propsOf())

interface Part : BaseProperties {
    val runtime: Runtime?
    val folder: String?
    val generators: List<GeneratorOpts>

    companion object {
        @JvmOverloads fun build(_map: Properties = propsOf(), block: Data.() -> Unit = {}) =
            BaseProperties.build(::Data, _map, block)
    }

    open class Data(map: Properties = propsOf()) : BaseProperties.Data(map),
        Part {
        override var runtime: Runtime? by _map
        override var folder: String? by _map
        override var generators: List<GeneratorOpts> by _map
    }
}

data class Context(var routeHost: String? = null)