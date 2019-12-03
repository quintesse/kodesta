package org.codejive.kodesta.core.template.transformers

import org.codejive.kodesta.core.template.Transformer

/**
 * A simple no-op transformer
 */
fun id(): Transformer {
    return { lines -> lines }
}
