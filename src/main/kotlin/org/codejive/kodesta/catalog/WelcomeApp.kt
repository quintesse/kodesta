package org.codejive.kodesta.catalog

import org.codejive.kodesta.catalog.GeneratorInfo.*
import org.codejive.kodesta.core.BaseProperties
import org.codejive.kodesta.core.Properties
import org.codejive.kodesta.core.catalog.BaseGenerator
import org.codejive.kodesta.core.catalog.BaseGeneratorProps
import org.codejive.kodesta.core.catalog.GeneratorContext
import org.codejive.kodesta.core.data.jsonIo
import org.codejive.kodesta.core.data.objectToString
import org.codejive.kodesta.core.deploy.DeploymentDescriptor
import org.codejive.kodesta.core.propsOf
import org.codejive.kodesta.core.resource.Resources
import org.codejive.kodesta.core.resource.readResources
import org.codejive.kodesta.core.resource.writeResources
import org.codejive.kodesta.core.template.transformers.cases
import java.nio.file.Files

interface WelcomeAppProps : BaseGeneratorProps {
    val deployment: DeploymentDescriptor

    companion object {
        @JvmOverloads fun build(_map: Properties = propsOf(), block: Data.() -> Unit = {}) =
            BaseProperties.build(WelcomeAppProps::Data, _map, block)
    }

    open class Data(map: Properties = propsOf()) : BaseGeneratorProps.Data(map),
        WelcomeAppProps {
        override var deployment: DeploymentDescriptor by _map
    }
}

class WelcomeApp(info: GeneratorInfo, ctx: GeneratorContext) : BaseGenerator(info, ctx) {
    override fun apply(resources: Resources, props: Properties, extra: Properties): Resources {
        // We're not really a runtime, but the setup it does for multi-part applications is useful to us
        generator("runtime-base-support").apply(resources, props, extra)

        // This is here in case we get applied in a subFolderName of our own
        // (meaning there's no runtime so there's no gap or README)
        if (!filesCopied()) {
            copy()
            transform("gap", cases(props))
        }

        // Check if the Welcome App service already exists, so we don't create it twice
        val appName = props["application"] as String
        val appDesc = (props["deployment"] as DeploymentDescriptor).applications[0]
        val fileName = targetDir.resolve(".openshiftio/service.welcome.yaml")
        val res = if (!Files.exists(fileName)) {
            val template = sourceDir.resolve("templates/welcome-app.yaml")
            val tpl = readResources(template)
            tpl.setParam("APP_NAME", appName)
            tpl
        } else {
            readResources(fileName)
        }

        res.setParam("FRONTEND_SERVICE_NAME",
            if (props["subFolderName"] == null) appName else name(appName, "frontend"))
        res.setParam("BACKEND_SERVICE_NAME",
            if (props["subFolderName"] == null) appName else name(appName, "backend"))
        res.setParam("WELCOME_IMAGE_NAME",
            System.getenv("WELCOME_IMAGE_NAME") ?: "fabric8/launcher-creator-welcome-app")
        res.setParam("WELCOME_IMAGE_TAG", System.getenv("WELCOME_IMAGE_TAG") ?: "latest")
        res.setParam("WELCOME_APP_CONFIG", jsonIo.objectToString(appDesc))

        writeResources(fileName, res)
        return resources
    }
}
