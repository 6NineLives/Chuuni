package me.abhigya.chuunicore.commands

import me.abhigya.chuunicore.debug
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.plugin.Plugin
import revxrsal.commands.CommandHandler
import revxrsal.commands.bukkit.bukkitCommandHandler
import toothpick.InjectConstructor
import toothpick.Scope
import toothpick.ktp.binding.bind
import toothpick.ktp.binding.module
import toothpick.ktp.extension.getInstance
import java.util.logging.Logger
import javax.inject.Singleton

@Singleton
@InjectConstructor
class Commands(
    plugin: Plugin,
    scope: Scope,
    private val logger: Logger
) {

    init {
        scope.installModules(
            module {
                bind<CommandHandler>().toInstance(plugin.bukkitCommandHandler {
                    registerBrigadier()
                    enableAdventure()
//                    registerPermissionReader {
//                        it.getAnnotation<Permission>()?.let {
//                            CommandPermission { actor -> actor.sender.hasPermission(it.value) }
//                        }
//                    }
//                    translator.add(LanguageReader())
//                    autoCompleter.registerSuggestionFactory {
//                        if (it.type != String::class.java) return@registerSuggestionFactory null
//                        if (!it.hasAnnotation(GameConfigCompletion::class.java)) return@registerSuggestionFactory null
//                        SuggestionProvider { args, _, _ ->
//                            if (args.last().lowercase() == "game") {
//                                scope.getInstance<Configs>().gameMapConfigs.keys().sorted()
//                            } else {
//                                emptyList()
//                            }
//                        }
//                    }
                    setHelpWriter { cmd, _ ->
                        Component.text(cmd.path.toRealString(), NamedTextColor.AQUA)
                            .append(Component.text(" - ", NamedTextColor.DARK_GRAY))
                            .append(Component.text(cmd.usage, NamedTextColor.YELLOW))
                            .append(Component.text(" > ", NamedTextColor.DARK_GRAY))
                            .append(Component.text(cmd.description ?: "", NamedTextColor.GRAY))
                    }
                })
            }
        )
    }

    val scope: Scope = scope.openSubScope(this)

    fun registerCommands() {
        logger.debug("Registering commands")
        val commandHandler: CommandHandler = scope.getInstance()
        commandHandler.register(scope.getInstance<Test>())
    }

}