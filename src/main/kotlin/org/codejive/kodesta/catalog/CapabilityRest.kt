package org.codejive.kodesta.catalog

import org.codejive.kodesta.core.*
import org.codejive.kodesta.core.catalog.BaseGenerator
import org.codejive.kodesta.core.catalog.GeneratorContext
import org.codejive.kodesta.core.resource.Resources

// Returns the corresponding runtime generator name depending on the given runtime type
private fun runtimeByType(rt: Runtime): String {
    return "rest-${rt.name}"
}

class CapabilityRest(info: GeneratorInfo, ctx: GeneratorContext) : BaseGenerator(info, ctx) {
    override fun apply(resources: Resources, props: Properties, extra: Properties): Resources {
        val appName = name(props["application"] as String, props["subFolderName"] as String?)
        val rtServiceName = appName
        val rtRouteName = appName
        val rt = props["runtime"].let { Runtime.build(it as Properties) }
        val rtprops = propsOf(
            "application" to props["application"],
            "subFolderName" to props["subFolderName"],
            "serviceName" to rtServiceName,
            "routeName" to rtRouteName,
            "runtime" to rt,
            "maven" to props["maven"]?.let { MavenCoords.build(it as Properties) },
            "nodejs" to props["nodejs"]?.let { NodejsCoords.build(it as Properties) },
            "dotnet" to props["dotnet"]?.let { DotnetCoords.build(it as Properties) }
        )
        return generator(runtimeByType(rt)).apply(resources, rtprops, extra)
    }
}
