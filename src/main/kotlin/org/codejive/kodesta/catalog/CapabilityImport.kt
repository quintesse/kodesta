package org.codejive.kodesta.catalog

import org.codejive.kodesta.catalog.GeneratorInfo.*
import org.codejive.kodesta.core.*
import org.codejive.kodesta.core.catalog.BaseGenerator
import org.codejive.kodesta.core.catalog.GeneratorContext
import org.codejive.kodesta.core.resource.Resources

class CapabilityImport(info: GeneratorInfo, ctx: GeneratorContext) : BaseGenerator(info, ctx) {
    override fun apply(resources: Resources, props: Properties, extra: Properties): Resources {
        val appName = name(props["application"] as String, props["subFolderName"] as String?)
        val rtServiceName = appName
        val rtRouteName = appName
        val rtprops = propsOf(
            "application" to props["application"],
            "subFolderName" to props["subFolderName"],
            "serviceName" to rtServiceName,
            "routeName" to rtRouteName,
            "maven" to props["maven"]?.let { MavenCoords.build(it as Properties) },
            "nodejs" to props["nodejs"]?.let { NodejsCoords.build(it as Properties) },
            "dotnet" to props["dotnet"]?.let { DotnetCoords.build(it as Properties) },
            "gitImportUrl" to props["gitImportUrl"],
            "gitImportBranch" to props["gitImportBranch"],
            "builderImage" to props["builderImage"],
            "builderLanguage" to props["builderLanguage"],
            "env" to props["env"],
            "overlayOnly" to props["overlayOnly"],
            "keepGitFolder" to props["keepGitFolder"]
        )
        return generator("import-codebase").apply(resources, rtprops, extra)
    }
}
