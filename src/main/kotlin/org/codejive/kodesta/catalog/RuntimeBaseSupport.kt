package org.codejive.kodesta.catalog

import org.codejive.kodesta.core.BaseProperties
import org.codejive.kodesta.core.Properties
import org.codejive.kodesta.core.catalog.BaseGenerator
import org.codejive.kodesta.core.catalog.BaseGeneratorProps
import org.codejive.kodesta.core.catalog.GeneratorContext
import org.codejive.kodesta.core.propsOf
import org.codejive.kodesta.core.resource.*
import org.codejive.kodesta.core.template.transformers.cases
import java.nio.file.Paths

interface RuntimeBaseSupportProps : BaseGeneratorProps {
    companion object {
        @JvmOverloads fun build(_map: Properties = propsOf(), block: Data.() -> Unit = {}) =
            BaseProperties.build(RuntimeBaseSupportProps::Data, _map, block)
    }

    open class Data(map: Properties = propsOf()) : BaseGeneratorProps.Data(map),
        RuntimeBaseSupportProps {
    }
}

class RuntimeBaseSupport(info: GeneratorInfo, ctx: GeneratorContext) : BaseGenerator(info, ctx) {
    override fun apply(resources: Resources, props: Properties, extra: Properties): Resources {
        val pbsprops = RuntimeBaseSupportProps.build(props)
        // This is here in case we get applied in a subFolderName of our own
        // (meaning there's no runtime so there's no gap or README)
        val files = Paths.get("files")
        val parent = Paths.get("..")
        if (pbsprops.subFolderName != null && !filesCopied(files, parent)) {
            copy(files, parent)
            transform("gap", cases(pbsprops), parent)
        }
        return resources
    }
}