package org.codejive.kodesta.catalog

import org.codejive.kodesta.core.*
import org.codejive.kodesta.core.catalog.BaseGenerator
import org.codejive.kodesta.core.catalog.GeneratorContext
import org.codejive.kodesta.core.resource.Resources

class CapabilityComponent(info: GeneratorInfo, ctx: GeneratorContext) : BaseGenerator(info, ctx) {
    override fun apply(resources: Resources, props: Properties, extra: Properties): Resources {
        val appName = name(props["application"] as String, props["subFolderName"] as String?)
        val rtServiceName = appName
        val rtRouteName = appName
        val gprops = propsOf(
            props,
            "serviceName" to rtServiceName,
            "routeName" to rtRouteName,
            "runtime" to props["runtime"]?.let { Runtime.build(it as Properties) }
        )
        return generator(props["generator"] as String).apply(resources, gprops, extra)
    }
}
