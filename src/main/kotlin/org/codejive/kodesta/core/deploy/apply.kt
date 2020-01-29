package org.codejive.kodesta.core.deploy

import org.codejive.kodesta.catalog.GeneratorRegistry
import org.codejive.kodesta.core.Properties
import org.codejive.kodesta.core.catalog.GeneratorContext
import org.codejive.kodesta.core.catalog.PropertyDef
import org.codejive.kodesta.core.catalog.validate
import org.codejive.kodesta.core.filterObject
import org.codejive.kodesta.core.pathGet
import org.codejive.kodesta.core.propsOf
import org.codejive.kodesta.core.resource.Resources
import org.codejive.kodesta.core.resource.readResources
import org.codejive.kodesta.core.resource.writeResources
import java.nio.file.Files
import java.nio.file.Path

// Creates the code for an entire deployment within a temporary folder and then
// executes the given code block with the path to that folder. After execution
// returns the temporary folder will be removed completely. The return value
// of this function is whatever was returned from the executed code block.
fun <T> withDeployment(deployment: DeploymentDescriptor, registry: GeneratorRegistry = GeneratorRegistry.defaultRegistry, block: Path.() -> T): T {
    // Create temp dir
    val td = Files.createTempDirectory("creator")
    try {
        // Apply the deployment
        applyDeployment(td, deployment, registry)
        // Now execute the given code block
        return block.invoke(td)
    } finally {
        // In the end clean everything up again
        td.toFile().deleteRecursively()
    }
}

// Creates the code for one or more applications by calling `applyApplication()` on all the
// applications in the given deployment descriptor
fun applyDeployment(targetDir: Path, deployment: DeploymentDescriptor, registry: GeneratorRegistry = GeneratorRegistry.defaultRegistry) {
    deployment.applications.forEach { applyApplication(targetDir, it, registry) }
}

// Creates the code for an application by calling `applyPart()` on all the parts
// in the given application descriptor
fun applyApplication(targetDir: Path, application: ApplicationDescriptor, registry: GeneratorRegistry = GeneratorRegistry.defaultRegistry) {
    application.parts.forEach { applyPart(targetDir, application.application, it, registry) }
}

// Creates the code for a part by calling `applyGenerator()` on all the
// generators in the given part descriptor
private fun applyPart(targetDir: Path, appName: String, part: PartDescriptor, registry: GeneratorRegistry = GeneratorRegistry.defaultRegistry) {
    val genTargetDir = if (part.subFolderName == null) targetDir else targetDir.resolve(part.subFolderName)
    val res = readResources(resourcesFileName(genTargetDir))
    part.generators.forEach { applyGenerator(res, targetDir, appName, part.subFolderName, part.shared, it, registry) }
}

// Calls `apply()` on the given generator (which allows it to copy, generate
// and change files in the user's project) and adds information about the
// generator to the `deployment.json` in the project's root.
private fun applyGenerator(
    res: Resources,
    targetDir: Path,
    appName: String,
    subFolderName: String?,
    shared: Properties?,
    generator: GeneratorDescriptor,
    registry: GeneratorRegistry = GeneratorRegistry.defaultRegistry
): DeploymentDescriptor {
    val module = generator.module
    val props = propsOf(generator.props, "module" to module, "application" to appName)
    if (subFolderName != null) {
        props["subFolderName"] = subFolderName
    }

    // Validate the properties that we get passed are valid
    val genTargetDir = if (subFolderName == null) targetDir else targetDir.resolve(subFolderName)
    val genInfo = registry.byName(module)
    val propDefs = genInfo.infoDef.props
    val allprops = propsOf(props, definedPropsOnly(propDefs, shared))
    validate(propDefs, registry.enums, allprops)

    // Read the deployment descriptor and validate if we can safely add this generator
    val rf = resourcesFileName(genTargetDir)
    val df = deploymentFileName(targetDir)
    val deployment = readDeployment(df)
    validateAddGenerator(deployment, allprops)

    // Apply the generator
    val cap = genInfo.klazz(genInfo, GeneratorContext(genTargetDir, registry))
    val extra = propsOf("category" to genInfo.infoDef.metadata?.category)
    val res2 = cap.apply(res, allprops, extra)

    // Add the generator's state to the deployment descriptor
    addGenerator(deployment, createCapState(propDefs, allprops, extra))

    // Execute any post-apply generators
    val res3 = postApply(res2, targetDir, deployment, registry)

    // Write everything back to their respective files
    writeResources(rf, res3)
    writeDeployment(df, deployment)

    return deployment
}

fun definedPropsOnly(propDefs: List<PropertyDef>, props: Properties?): Properties {
    return filterObject(props) { key, _ -> getPropDef(propDefs, key)?.id != null }
}

fun getPropDef(propDefs: List<PropertyDef>, propId: String): PropertyDef? {
    return propDefs.find { pd -> pd.id == propId }
}

// Validates that the given generator can be added to the given deployment
fun validateAddGenerator(deployment: DeploymentDescriptor, props: Properties) {
    val app = deployment.applications.find { item -> item.application == props["application"] }
    if (app != null) {
        val part = app.parts.find { t -> t.subFolderName == props["subFolderName"] }
        if (part != null) {
            val rtapp = part.pathGet<String>("shared.runtime.name")
            val rtcap = props.pathGet<String>("runtime.name")
            if (rtapp != null && rtcap != null && rtapp != rtcap) {
                throw Exception(
                        "Trying to add generator with incompatible 'runtime' (is '$rtcap', should be '$rtapp')")
            }
        }
        if (app.parts[0].subFolderName == null && props["subFolderName"] != null || app.parts[0].subFolderName != null && props["subFolderName"] == null) {
            throw Exception("Can't mix generators in the root folder and in sub folders")
        }
    }
}

// Adds the given generator to the given deployment
fun addGenerator(deployment: DeploymentDescriptor, genState: Properties) {
    val capProps = propsOf(genState)
    capProps.remove("application")
    capProps.remove("subFolderName")
    capProps.remove("shared")
    capProps.remove("sharedExtra")
    val cap = GeneratorDescriptor.build(capProps)
    var app = deployment.applications.find { item -> item.application == genState["application"] }
    if (app == null) {
        app = ApplicationDescriptor.build {
            application = genState["application"] as String
        }
        deployment.applications.add(app)
    }
    var part = app.parts.find { p -> p.subFolderName == genState["subFolderName"] }
    if (part == null) {
        part = PartDescriptor.build {
            shared = propsOf()
            extra = propsOf()
            genState["subFolderName"]?.let {
                subFolderName = it as String
            }
        }
        app.parts.add(part)
    }
    part.generators.add(cap)
    val shared = genState["shared"]
    if (shared != null) {
        part.shared?.putAll(shared as Properties)
    }
    val sharedExtra = genState["sharedExtra"]
    if (sharedExtra != null) {
        part.extra?.putAll(sharedExtra as Properties)
    }
    val overall = overallCategory(part.generators)
    part.extra?.putAll(overall)
}

fun overallCategory(generators: List<GeneratorDescriptor>): Properties {
    return if (generators.isNotEmpty()) {
        var categories = generators.mapNotNull { c -> c.extra?.get("category") as String? }.distinct()
        if (categories.size > 1) {
            // This is a bit of a hack, we're purposely removing "support"
            // because we know we're not really interested in that one
            categories = categories.filter { c -> c != "support" }
        }
        propsOf("category" to categories[0])
    } else {
        propsOf()
    }
}

fun createCapState(propDefs: List<PropertyDef>, props: Properties, extra: Properties): Properties {
    val props2 = filterObject(props) { key, _ -> getPropDef(propDefs, key)?.shared == null }
    val shared = filterObject(props) { key, _ -> getPropDef(propDefs, key)?.shared != null }
    val sharedExtra = extra["shared"]
    val extra2 = propsOf(extra)
    extra2.remove("shared")
    props2.remove("module")
    props2.remove("application")
    props2.remove("subFolderName")
    return propsOf(
        "module" to props["module"],
        "application" to props["application"],
        "subFolderName" to props["subFolderName"],
        "props" to props2,
        "shared" to shared,
        "sharedExtra" to sharedExtra,
        "extra" to extra2
    )
}

fun postApply(res: Resources, targetDir: Path, deployment: DeploymentDescriptor, registry: GeneratorRegistry = GeneratorRegistry.defaultRegistry): Resources {
    val app = deployment.applications[0]
    for (part in app.parts) {
        for (gen in part.generators) {
            try {
                val genInfo = registry.byName(gen.module)
                val genTargetDir = if (part.subFolderName == null) targetDir else targetDir.resolve(part.subFolderName)
                val genInst = genInfo.klazz(genInfo, GeneratorContext(genTargetDir, registry))
                val props = propsOf(
                        part.shared,
                        gen.props,
                        "module" to gen.module,
                        "application" to app.application,
                        "subFolderName" to part.subFolderName
                )
                genInst.postApply(res, props, deployment)
            } catch (ex: Exception) {
                println("Generator ${gen.module} wasn't found for post-apply, skipping.")
            }
        }
    }
    return res
}

