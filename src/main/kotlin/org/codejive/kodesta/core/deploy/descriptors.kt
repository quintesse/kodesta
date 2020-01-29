package org.codejive.kodesta.core.deploy

import org.codejive.kodesta.core.BaseProperties
import org.codejive.kodesta.core.Properties
import org.codejive.kodesta.core.propsOf

interface GeneratorDescriptor : BaseProperties {
    val module: String                  // The name of the applied generator
    val props: Properties?              // The properties to pass to the generator
    val extra: Properties?              // Any properties the generator might return

    companion object {
        @JvmOverloads fun build(_map: Properties = propsOf(), block: Data.() -> kotlin.Unit = {}) =
            BaseProperties.build(::Data, _map, block)
    }

    class Data(map: Properties = propsOf()) : BaseProperties.Data(map), GeneratorDescriptor {
        override var module: String by _map                  // The name of the applied generator
        override var props: Properties? by _map              // The properties to pass to the generator
        override var extra: Properties? by _map              // Any properties the generator might return
    }
}

interface PartDescriptor : BaseProperties {
    val subFolderName: String?          // The name of the subFolderName
    val shared: Properties?             // Any shared properties that will be passed to all generators
    val extra: Properties?              // Any shared properties returned by generators
    var generators: MutableList<GeneratorDescriptor>   // All generators that are part of the subFolderName

    companion object {
        @JvmOverloads fun build(_map: Properties = propsOf(), block: Data.() -> kotlin.Unit = {}) =
            BaseProperties.build(::Data, _map, block)
    }

    class Data(map: Properties = propsOf()) : BaseProperties.Data(map), PartDescriptor {
        override var subFolderName: String? by _map      // The name of the subFolderName
        override var shared: Properties? by _map         // Any shared properties that will be passed to all generators
        override var extra: Properties? by _map          // Any shared properties returned by generators
        override var generators: MutableList<GeneratorDescriptor> by _map   // All generators that are part of the subFolderName

        init {
            ensureList(::generators, GeneratorDescriptor::Data)
        }
    }
}

interface ApplicationDescriptor : BaseProperties {
    val application: String             // The name of the application
    val extra: Properties?              // Any application properties unused by the creator itself
    var parts: MutableList<PartDescriptor>     // Parts are groups of generators that make up the application

    companion object {
        @JvmOverloads fun build(_map: Properties = propsOf(), block: Data.() -> kotlin.Unit = {}) =
            BaseProperties.build(::Data, _map, block)
    }

    class Data(map: Properties = propsOf()) : BaseProperties.Data(map), ApplicationDescriptor {
        override var application: String by _map             // The name of the application
        override var extra: Properties? by _map              // Any application properties unused by the creator itself
        override var parts: MutableList<PartDescriptor> by _map     // Parts are groups of generators that make up the application

        init {
            ensureList(::parts, PartDescriptor::Data)
        }
    }
}

interface DeploymentDescriptor : BaseProperties {
    var applications: MutableList<ApplicationDescriptor>   // All applications that are part of the deployment

    companion object {
        @JvmOverloads fun build(_map: Properties = propsOf(), block: Data.() -> kotlin.Unit = {}) =
            BaseProperties.build(::Data, _map, block)
    }

    class Data(map: Properties = propsOf()) : BaseProperties.Data(map), DeploymentDescriptor {
        override var applications: MutableList<ApplicationDescriptor> by _map   // All applications that are part of the deployment

        init {
            ensureList(::applications, ApplicationDescriptor::Data)
        }
    }
}
