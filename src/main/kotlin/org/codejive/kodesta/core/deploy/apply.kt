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

// Creates the code for a part by calling `applyCapability()` on all the
// capabilities in the given part descriptor
private fun applyPart(targetDir: Path, appName: String, part: PartDescriptor, registry: GeneratorRegistry = GeneratorRegistry.defaultRegistry) {
    val genTargetDir = if (part.subFolderName == null) targetDir else targetDir.resolve(part.subFolderName)
    val res = readResources(resourcesFileName(genTargetDir))
    part.capabilities.forEach { applyCapability(res, targetDir, appName, part.subFolderName, part.shared, it, registry) }
}

// Calls `apply()` on the given capability (which allows it to copy, generate
// and change files in the user's project) and adds information about the
// capability to the `deployment.json` in the project's root.
private fun applyCapability(
    res: Resources,
    targetDir: Path,
    appName: String,
    subFolderName: String?,
    shared: Properties?,
    capability: CapabilityDescriptor,
    registry: GeneratorRegistry = GeneratorRegistry.defaultRegistry
): DeploymentDescriptor {
    val module = capability.module
    val props = propsOf(capability.props, "module" to module, "application" to appName)
    if (subFolderName != null) {
        props["subFolderName"] = subFolderName
    }

    // Validate the properties that we get passed are valid
    val capTargetDir = if (subFolderName == null) targetDir else targetDir.resolve(subFolderName)
    val capInfo = registry.capability(module)
    val propDefs = capInfo.infoDef.props
    val allprops = propsOf(props, definedPropsOnly(propDefs, shared))
    validate(propDefs, registry.enums, allprops)

    // Read the deployment descriptor and validate if we can safely add this capability
    val rf = resourcesFileName(capTargetDir)
    val df = deploymentFileName(targetDir)
    val deployment = readDeployment(df)
    validateAddCapability(deployment, allprops)

    // Apply the capability
    val cap = capInfo.klazz(capInfo, GeneratorContext(capTargetDir, registry))
    val extra = propsOf("category" to capInfo.infoDef.metadata?.category)
    val res2 = cap.apply(res, allprops, extra)

    // Add the capability's state to the deployment descriptor
    addCapability(deployment, createCapState(propDefs, allprops, extra))

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

// Validates that the given capability can be added to the given deployment
fun validateAddCapability(deployment: DeploymentDescriptor, props: Properties) {
    val app = deployment.applications.find { item -> item.application == props["application"] }
    if (app != null) {
        val part = app.parts.find { t -> t.subFolderName == props["subFolderName"] }
        if (part != null) {
            val rtapp = part.pathGet<String>("shared.runtime.name")
            val rtcap = props.pathGet<String>("runtime.name")
            if (rtapp != null && rtcap != null && rtapp != rtcap) {
                throw Exception(
                        "Trying to add capability with incompatible 'runtime' (is '$rtcap', should be '$rtapp')")
            }
        }
        if (app.parts[0].subFolderName == null && props["subFolderName"] != null || app.parts[0].subFolderName != null && props["subFolderName"] == null) {
            throw Exception("Can't mix capabilities in the root folder and in sub folders")
        }
    }
}

// Adds the given capability to the given deployment
fun addCapability(deployment: DeploymentDescriptor, capState: Properties) {
    val capProps = propsOf(capState)
    capProps.remove("application")
    capProps.remove("subFolderName")
    capProps.remove("shared")
    capProps.remove("sharedExtra")
    val cap = CapabilityDescriptor.build(capProps)
    var app = deployment.applications.find { item -> item.application == capState["application"] }
    if (app == null) {
        app = ApplicationDescriptor.build {
            application = capState["application"] as String
        }
        deployment.applications.add(app)
    }
    var part = app.parts.find { p -> p.subFolderName == capState["subFolderName"] }
    if (part == null) {
        part = PartDescriptor.build {
            shared = propsOf()
            extra = propsOf()
            capState["subFolderName"]?.let {
                subFolderName = it as String
            }
        }
        app.parts.add(part)
    }
    part.capabilities.add(cap)
    val shared = capState["shared"]
    if (shared != null) {
        part.shared?.putAll(shared as Properties)
    }
    val sharedExtra = capState["sharedExtra"]
    if (sharedExtra != null) {
        part.extra?.putAll(sharedExtra as Properties)
    }
    val overall = overallCategory(part.capabilities)
    part.extra?.putAll(overall)
}

fun overallCategory(capabilities: List<CapabilityDescriptor>): Properties {
    return if (capabilities.isNotEmpty()) {
        var categories = capabilities.mapNotNull { c -> c.extra?.get("category") as String? }.distinct()
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
        for (cap in part.capabilities) {
            try {
                val capInfo = registry.capability(cap.module)
                val capTargetDir = if (part.subFolderName == null) targetDir else targetDir.resolve(part.subFolderName)
                val capinst = capInfo.klazz(capInfo, GeneratorContext(capTargetDir, registry))
                val props = propsOf(
                        part.shared,
                        cap.props,
                        "module" to cap.module,
                        "application" to app.application,
                        "subFolderName" to part.subFolderName
                )
                capinst.postApply(res, props, deployment)
            } catch (ex: Exception) {
                println("Capability ${cap.module} wasn't found for post-apply, skipping.")
            }
        }
    }
    return res
}

