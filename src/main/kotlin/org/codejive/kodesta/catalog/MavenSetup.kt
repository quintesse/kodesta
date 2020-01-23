package org.codejive.kodesta.catalog

import org.codejive.kodesta.core.BaseProperties
import org.codejive.kodesta.core.MavenCoords
import org.codejive.kodesta.core.catalog.BaseGenerator
import org.codejive.kodesta.core.catalog.BaseGeneratorProps
import org.codejive.kodesta.core.Properties
import org.codejive.kodesta.core.catalog.GeneratorContext
import org.codejive.kodesta.core.propsOf
import org.codejive.kodesta.core.resource.Resources

interface MavenSetupProps : BaseGeneratorProps {
    val maven: MavenCoords

    companion object {
        @JvmOverloads fun build(_map: Properties = propsOf(), block: Data.() -> kotlin.Unit = {}) =
            BaseProperties.build(MavenSetupProps::Data, _map, block)
    }

    open class Data(map: Properties = propsOf()) : BaseGeneratorProps.Data(map),
        MavenSetupProps {
        override var maven: MavenCoords by _map

        init {
            ensureObject(::maven, MavenCoords::Data)
        }
    }
}

class MavenSetup(info: GeneratorInfo, ctx: GeneratorContext) : BaseGenerator(info, ctx) {
    override fun apply(resources: Resources, props: Properties, extra: Properties): Resources {
        val msprops = MavenSetupProps.build(props)
        updatePom(msprops.application, msprops.maven.groupId, msprops.maven.artifactId, msprops.maven.version)
        return resources
    }
}
