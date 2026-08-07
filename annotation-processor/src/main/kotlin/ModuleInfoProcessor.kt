import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration


class ModuleInfoProcessor (
    private val codeGenerator: CodeGenerator
) : SymbolProcessor {
    private var invoked = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (invoked) return emptyList()
        val classes = resolver.getSymbolsWithAnnotation("com.ricedotwho.rsm.module.api.ModuleInfo")
            .filterIsInstance<KSClassDeclaration>()
            .toList()
        val names = classes.mapNotNull { it.qualifiedName?.asString() }
        if (names.isEmpty()) return emptyList()
        invoked = true
        val sourceFiles = classes.mapNotNull { it.containingFile }.toTypedArray()

        val file = codeGenerator.createNewFile(
            Dependencies(aggregating = true, *sourceFiles),
            "com.ricedotwho.rsm.module.api",
            "GeneratedModuleList",
            "java"
        )


        val imports = names.joinToString("\n") { "import $it;" }
        val registrationList = names.joinToString(",\n") { "        ${it.substringAfterLast('.')}.class" }
        val javaFile = """
package com.ricedotwho.rsm.module.api;

import java.util.List;
$imports

public class GeneratedModuleList {
    public static final List<Class<?>> modules = List.of(
$registrationList
    );
}
        """.trimIndent()
        file.write(javaFile.toByteArray())

        return emptyList()
    }
}