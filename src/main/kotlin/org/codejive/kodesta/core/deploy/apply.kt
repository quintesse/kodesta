package org.codejive.kodesta.core.deploy

import org.codejive.kodesta.catalog.GeneratorRegistry
import org.codejive.kodesta.core.Properties
import org.codejive.kodesta.core.catalog.GeneratorContext
import org.codejive.kodesta.core.catalog.PropertyDef
import org.codejive.kodesta.core.catalog.validate
import org.codejive.kodesta.core.filterObject
import org.codejive.kodesta.core.propsOf
import org.codejive.kodesta.core.resource.readResources
import org.codejive.kodesta.core.resource.writeResources
import java.nio.file.Path

// Returns the name of the resources file in the given directory
fun resourcesFileName(targetDir: Path): Path {
    return targetDir.resolve(".openshiftio").resolve("application.yaml")
}

// Calls `apply()` on the given generator (which allows it to copy, generate
// and change files in the user's project) and adds information about the
// generator to the `deployment.json` in the project's root.
fun applyGenerator(
    module: String,
    appName: String,
    targetDir: Path,
    subFolderName: String?,
    genProps: Properties?,
    registry: GeneratorRegistry
) {
    val genTargetDir = if (subFolderName == null) targetDir else targetDir.resolve(subFolderName)
    val res = readResources(resourcesFileName(genTargetDir))
    val props = propsOf(genProps, "module" to module, "application" to appName)
    if (subFolderName != null) {
        props["subFolderName"] = subFolderName
    }

    // Validate the properties that we get passed are valid
    val genInfo = registry.byName(module)
    val propDefs = genInfo.infoDef.props
    validate(propDefs, registry.enums, props)

    // Apply the generator
    val cap = genInfo.klazz(genInfo, GeneratorContext(genTargetDir, registry))
    val extra = propsOf("category" to genInfo.infoDef.metadata?.category)
    val res2 = cap.apply(res, props, extra)

    // Write everything back to their respective files
    val rf = resourcesFileName(genTargetDir)
    writeResources(rf, res2)
}

private fun definedPropsOnly(propDefs: List<PropertyDef>, props: Properties?): Properties {
    return filterObject(props) { key, _ -> getPropDef(propDefs, key)?.id != null }
}

private fun getPropDef(propDefs: List<PropertyDef>, propId: String): PropertyDef? {
    return propDefs.find { pd -> pd.id == propId }
}
