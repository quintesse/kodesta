package org.codejive.kodesta.catalog

import org.codejive.kodesta.catalog.GeneratorInfo.*
import org.codejive.kodesta.core.*
import org.codejive.kodesta.core.catalog.BaseGenerator
import org.codejive.kodesta.core.catalog.GeneratorContext
import org.codejive.kodesta.core.deploy.DeploymentDescriptor
import org.codejive.kodesta.core.resource.Resources

class CapabilityWelcome(info: GeneratorInfo, ctx: GeneratorContext) : BaseGenerator(info, ctx) {
    override fun apply(resources: Resources, props: Properties, extra: Properties): Resources {
        return resources
    }

    override fun postApply(resources: Resources, props: Properties, deployment: DeploymentDescriptor): Resources {
        val appName = name(props["application"] as String, props["subFolderName"] as String?)
        val rtServiceName = appName
        val waprops = propsOf(
            "application" to props["application"],
            "subFolderName" to props["subFolderName"],
            "serviceName" to rtServiceName,
            "routeName" to "welcome",
            "deployment" to deployment
        )
        return generator("welcome-app").apply(resources, waprops, propsOf())
    }

}
