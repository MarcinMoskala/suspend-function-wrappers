package de.jensklingenberg

import com.google.auto.service.AutoService
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.config.kotlinSourceRoots
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.copy
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.declarations.FirDeclarationStatus
import org.jetbrains.kotlin.fir.declarations.FirRegularClass
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter
import org.jetbrains.kotlin.fir.extensions.FirStatusTransformerExtension
import org.jetbrains.kotlin.fir.extensions.transform

@AutoService(CompilerPluginRegistrar::class)
class CommonComponentRegistrar : CompilerPluginRegistrar() {

    override val supportsK2: Boolean
        get() = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        if (configuration[KEY_ENABLED] == false) {
            return
        }
        val messageCollector: MessageCollector = configuration.get(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)
        FirExtensionRegistrarAdapter
            .registerExtension(FirAllOpenExtensionRegistrar(messageCollector))

        configuration.kotlinSourceRoots.forEach {
            messageCollector.report(
                CompilerMessageSeverity.WARNING,
                "*** Hello from CommonComponentRegistrar *** " + it.path
            )
        }
    }
}

class FirAllOpenExtensionRegistrar(
    private val messageCollector: MessageCollector
) : FirExtensionRegistrar() {
    override fun ExtensionRegistrarContext.configurePlugin() {
        +makeFirAllOpenExtensionRegistrar(messageCollector)
    }
}

fun makeFirAllOpenExtensionRegistrar(
    messageCollector: MessageCollector
): (FirSession) -> FirAllOpenStatusTransformer = { session ->
    messageCollector.report(
        CompilerMessageSeverity.WARNING,
            "Registering transformer for session $session"
    )
    FirAllOpenStatusTransformer(messageCollector, session)
}

class FirAllOpenStatusTransformer(
    private val messageCollector: MessageCollector,
    session: FirSession
) : FirStatusTransformerExtension(session) {
    override fun needTransformStatus(declaration: FirDeclaration): Boolean {
        messageCollector.report(
            CompilerMessageSeverity.WARNING,
            "Considering class ${declaration.origin}"
        )
        return declaration is FirRegularClass && declaration.classKind == ClassKind.CLASS
    }

    override fun transformStatus(status: FirDeclarationStatus, declaration: FirDeclaration): FirDeclarationStatus =
        status.copy(modality = Modality.OPEN)
}