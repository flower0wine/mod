package com.example.fabricmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class DropMultiplierCommand {
    private static boolean isMultiplierEnabled = false;

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("dropmultiplier")
            .requires(source -> source.hasPermissionLevel(2))
            .then(argument("enabled", BoolArgumentType.bool())
                .executes(context -> {
                    boolean enabled = BoolArgumentType.getBool(context, "enabled");
                    setMultiplierEnabled(enabled);
                    
                    context.getSource().sendMessage(Text.literal(
                        enabled ? "掉落物倍数已开启 (300倍)" : "掉落物倍数已关闭"
                    ).formatted(enabled ? Formatting.GREEN : Formatting.RED));
                    
                    return 1;
                }))
        );
    }

    public static boolean isMultiplierEnabled() {
        return isMultiplierEnabled;
    }

    public static void setMultiplierEnabled(boolean enabled) {
        isMultiplierEnabled = enabled;
    }
} 