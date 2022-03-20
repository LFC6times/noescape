package ml.ikwid.noescape.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import ml.ikwid.noescape.Noescape;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class CommandRegistry {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralCommandNode<ServerCommandSource> noescape = CommandManager
                .literal("noescape")
                .requires(s -> s.hasPermissionLevel(4))
                .build();
        LiteralCommandNode<ServerCommandSource> alias = CommandManager
                .literal("ne")
                .redirect(noescape)
                .build();
        LiteralCommandNode<ServerCommandSource> create = CommandManager
                .literal("create")
                .then(
                        CommandManager.argument("x1", IntegerArgumentType.integer())

                    .then(
                            CommandManager.argument("z1", IntegerArgumentType.integer())

                        .then(
                                CommandManager.argument("x2", IntegerArgumentType.integer())

                            .then(
                                    CommandManager.argument("z2", IntegerArgumentType.integer())
                                            .executes(Noescape::create)
                ))))
                .build();
        LiteralCommandNode<ServerCommandSource> remove = CommandManager
                .literal("remove")
                .executes(Noescape::remove)
                .build();
        LiteralCommandNode<ServerCommandSource> reload = CommandManager
                .literal("reload")
                .executes(Noescape::reloadConfig)
                .build();
        LiteralCommandNode<ServerCommandSource> toggleSetting = CommandManager
                .literal("setting")
                        .then(
                                CommandManager.argument("setting", StringArgumentType.string())
                                        .executes(Noescape::toggle)
                                        .then(
                                                CommandManager.argument("dmg", DoubleArgumentType.doubleArg())
                                                        .executes(Noescape::setWBDmg)
                                        )
                        )
                .build();

        dispatcher.getRoot().addChild(noescape);
        dispatcher.getRoot().addChild(alias);

        noescape.addChild(create);
        noescape.addChild(reload);
        noescape.addChild(remove);
        noescape.addChild(toggleSetting);
    }
}
