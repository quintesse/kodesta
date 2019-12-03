package org.codejive.kodesta.catalog

import org.codejive.kodesta.core.*
import org.codejive.kodesta.core.catalog.BaseGenerator
import org.codejive.kodesta.core.catalog.BaseGeneratorProps
import org.codejive.kodesta.core.catalog.GeneratorContext
import org.codejive.kodesta.core.data.objectFromString
import org.codejive.kodesta.core.data.yamlIo
import org.codejive.kodesta.core.resource.*
import org.codejive.kodesta.core.template.transformers.cases

interface AppImagesProps : BaseGeneratorProps {
    val image: String

    companion object {
        @JvmOverloads
        fun build(_map: Properties = propsOf(), block: Data.() -> kotlin.Unit = {}) =
            BaseProperties.build(AppImagesProps::Data, _map, block)
    }

    open class Data(map: Properties = propsOf()) : BaseGeneratorProps.Data(map),
        AppImagesProps {
        override var image: String by _map
    }
}

class AppImages(info: GeneratorInfo, ctx: GeneratorContext) : BaseGenerator(info, ctx) {
    override fun apply(resources: Resources, props: Properties, extra: Properties): Resources {
        val biprops = AppImagesProps.build(props)
        val tplpath = templatePath(biprops.image)
        if (!existsFromPath(tplpath)) {
            throw IllegalArgumentException("Application image file not found: $tplpath")
        }
        val text = streamFromPath(tplpath).bufferedReader().readText()
        val exptext = org.codejive.kodesta.core.template.transform(text, cases(props, "#"))
        val yaml = yamlIo.objectFromString(exptext)
        val res = Resources(yaml.deepClone())
        if (res.isEmpty) {
            throw IllegalArgumentException("Image '${biprops.image}' not found")
        }
        setBuildContextDir(res, biprops.subFolderName)
        resources.add(res)
        if (biprops.routeName != null) {
            newRoute(resources, biprops.routeName!!, biprops.application, biprops.serviceName)
        }
        return resources
    }
}
