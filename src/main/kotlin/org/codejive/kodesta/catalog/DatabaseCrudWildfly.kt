package org.codejive.kodesta.catalog

import org.codejive.kodesta.catalog.GeneratorInfo.*
import org.codejive.kodesta.core.*
import org.codejive.kodesta.core.catalog.BaseGenerator
import org.codejive.kodesta.core.catalog.BaseLanguageProps
import org.codejive.kodesta.core.catalog.GeneratorContext
import org.codejive.kodesta.core.resource.*
import org.codejive.kodesta.core.template.transformers.cases

interface DatabaseCrudWildflyProps : BaseLanguageProps {
    val databaseType: String
    val secretName: String

    companion object {
        @JvmOverloads fun build(_map: Properties = propsOf(), block: Data.() -> Unit = {}) =
            BaseProperties.build(DatabaseCrudWildflyProps::Data, _map, block)
    }

    open class Data(map: Properties = propsOf()) : BaseLanguageProps.Data(map),
        DatabaseCrudWildflyProps {
        override var databaseType: String by _map
        override var secretName: String by _map
    }
}

class DatabaseCrudWildfly(info: GeneratorInfo, ctx: GeneratorContext) : BaseGenerator(info, ctx) {
    override fun apply(resources: Resources, props: Properties, extra: Properties): Resources {
        val dcwprops = DatabaseCrudWildflyProps.build(props)
        // Check if the generator was already applied, so we don"t do it twice
        if (!filesCopied()) {
            val pprops = if ("mysql" == dcwprops.databaseType) {
                propsOf(
                    props,
                    "env" to dbEnv(dcwprops, "MYSQL", 3306)
                )
            } else {
                propsOf(
                    props,
                    "env" to dbEnv(dcwprops, "POSTGRESQL", 5432)
                )
            }

            generator("runtime-wildfly").apply(resources, pprops, extra)
            copy()
            mergePoms()
            transform("src/**/*.java", cases(props))
        }
        extra["sourceMapping"] = propsOf(
            "dbEndpoint" to join(
                dcwprops.subFolderName,
                "src/main/java/io/openshift/booster/database/FruitResource.java"
            )
        )
        return resources
    }

    private fun dbEnv(dcwprops: DatabaseCrudWildflyProps, prefix: String, port: Int): Environment {
        return envOf(
            "${prefix}_DATABASE" to "my_data",
            "${prefix}_SERVICE_HOST" to envOf(
                "secret" to dcwprops.secretName,
                "key" to "uri"
            ),
            "${prefix}_SERVICE_PORT" to port,
            "${prefix}_DATASOURCE" to "MyDS",
            "${prefix}_USER" to envOf(
                "secret" to dcwprops.secretName,
                "key" to "user"
            ),
            "${prefix}_PASSWORD" to envOf(
                "secret" to dcwprops.secretName,
                "key" to "password"
            ),
            "GC_MAX_METASPACE_SIZE" to "150",
            "KUBERNETES_NAMESPACE" to envOf(
                "field" to "metadata.namespace"
            )
        )
    }
}
