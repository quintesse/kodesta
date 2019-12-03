package org.codejive.kodesta.core.catalog

import org.codejive.kodesta.catalog.GeneratorInfo
import org.codejive.kodesta.catalog.GeneratorRegistry
import org.codejive.kodesta.core.Properties
import org.codejive.kodesta.core.propsOf
import org.codejive.kodesta.core.resource.Resources

val testRegistry by lazy {
    GeneratorRegistry()
        .add("capability-dummy", ::CapabilityDummy)
        .add("dummy")
}

class CapabilityDummy(info: GeneratorInfo, ctx: GeneratorContext) : BaseGenerator(info, ctx) {
    override fun apply(resources: Resources, props: Properties, extra: Properties): Resources {
        val appName = name(props["application"] as String, props["subFolderName"] as String?)
        val gprops = propsOf(
            props,
            "serviceName" to appName,
            "routeName" to appName
        )
        return generator("dummy").apply(resources, gprops, extra)
    }
}
