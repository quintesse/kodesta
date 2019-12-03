package org.codejive.kodesta.catalog

import org.codejive.kodesta.catalog.GeneratorInfo.*
import org.codejive.kodesta.core.*
import org.codejive.kodesta.core.catalog.BaseGenerator
import org.codejive.kodesta.core.catalog.GeneratorContext
import org.codejive.kodesta.core.resource.Resources

// Returns the corresponding database generator depending on the given database type
private fun databaseByType(databaseType: String): String {
    return "database-$databaseType"
}

// Returns the corresponding runtime generator depending on the given runtime type
private fun runtimeByType(rt: Runtime): String {
    return "database-crud-${rt.name}"
}

class CapabilityDatabase(info: GeneratorInfo, ctx: GeneratorContext) : BaseGenerator(info, ctx) {
    override fun apply(resources: Resources, props: Properties, extra: Properties): Resources {
        val appName = name(props["application"], props["subFolderName"])
        val dbServiceName = name(appName, "database")
        val dbprops = propsOf(
            "application" to props["application"],
            "subFolderName" to props["subFolderName"],
            "serviceName" to dbServiceName,
            "databaseUri" to name(props["application"], props["subFolderName"], "database"),
            "databaseName" to "my_data",
            "secretName" to name(props["application"], props["subFolderName"], "database-bind")
        )
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
            "dotnet" to props["dotnet"]?.let { DotnetCoords.build(it as Properties) },
            "databaseType" to props["databaseType"],
            "secretName" to name(props["application"], props["subFolderName"], "database-bind")
        )
        generator("database-secret").apply(resources, dbprops, extra);
        generator(databaseByType(props["databaseType"] as String)).apply(resources, dbprops, extra);
        return generator(runtimeByType(rt)).apply(resources, rtprops, extra)
    }
}
