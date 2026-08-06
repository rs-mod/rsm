import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration


class RegisterProcessor (
    private val codeGenerator: CodeGenerator
) : SymbolProcessor {
    private var invoked = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (invoked) return emptyList()
        val classes = resolver.getSymbolsWithAnnotation("com.ricedotwho.rsm.event.api.Register")
            .filterIsInstance<KSClassDeclaration>()
            .toList()
        val names = classes.mapNotNull { it.qualifiedName?.asString() }
        if (names.isEmpty()) return emptyList()
        invoked = true
        val sourceFiles = classes.mapNotNull { it.containingFile }.toTypedArray()

        val file = codeGenerator.createNewFile(
            Dependencies(aggregating = true, *sourceFiles),
            "com.ricedotwho.rsm.event.api",
            "GeneratedRegistrationList",
            "kt"
        )
        val imports = names.joinToString("\n") { "import $it" }
        val registrationList = names.joinToString(",") { "${it.substringAfterLast('.')}::class.java" }
        val javaFile = """
package com.ricedotwho.rsm.event.api

$imports

public object GeneratedRegistrationList {
    public val registrationList: List<Class<*>> = listOf(
        $registrationList
    )
}
        """.trimIndent()
        file.write(javaFile.toByteArray())

        return emptyList()
    }
}