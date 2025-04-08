package com.example.fabricmod.command;

import com.example.fabricmod.enums.EntityFreezeTypeEnum;
import com.example.fabricmod.enums.VisionModeEnum;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import com.example.fabricmod.manager.FreezeLookManager;

import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandManager.argument;

public class FreezeLookCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("freezelook")
            .requires(source -> source.hasPermissionLevel(4))
            .then(literal("enable")
                .executes(context -> {
                    FreezeLookManager.INSTANCE.setEnabled(true, EntityFreezeTypeEnum.ALL, VisionModeEnum.VISION);
                    
                    context.getSource().getServer().getPlayerManager().broadcast(
                        createEnableMessage(EntityFreezeTypeEnum.ALL.getDisplayName(), VisionModeEnum.VISION.getDisplayName()),
                        false);
                    return 1;
                })
                .then(argument("type", StringArgumentType.word())
                    .suggests((context, builder) -> {
                        for (EntityFreezeTypeEnum type : EntityFreezeTypeEnum.values()) {
                            builder.suggest(type.getId());
                        }
                        return builder.buildFuture();
                    })
                    .executes(context -> {
                        String typeId = StringArgumentType.getString(context, "type");
                        EntityFreezeTypeEnum type = EntityFreezeTypeEnum.fromId(typeId);
                        
                        FreezeLookManager.INSTANCE.setEnabled(true, type, VisionModeEnum.VISION);
                        
                        context.getSource().getServer().getPlayerManager().broadcast(
                            createEnableMessage(type.getDisplayName(), VisionModeEnum.VISION.getDisplayName()),
                            false);
                        return 1;
                    })
                    .then(argument("mode", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            for (VisionModeEnum mode : VisionModeEnum.values()) {
                                builder.suggest(mode.getId());
                            }
                            return builder.buildFuture();
                        })
                        .executes(context -> {
                            String typeId = StringArgumentType.getString(context, "type");
                            String modeId = StringArgumentType.getString(context, "mode");
                            EntityFreezeTypeEnum type = EntityFreezeTypeEnum.fromId(typeId);
                            VisionModeEnum mode = VisionModeEnum.fromId(modeId);
                            
                            FreezeLookManager.INSTANCE.setEnabled(true, type, mode);
                            
                            context.getSource().getServer().getPlayerManager().broadcast(
                                createEnableMessage(type.getDisplayName(), mode.getDisplayName()),
                                false);
                            return 1;
                        }))))
            .then(literal("disable")
                .executes(context -> {
                    FreezeLookManager.INSTANCE.setEnabled(false, null, null);
                    
                    context.getSource().getServer().getPlayerManager().broadcast(
                        createDisableMessage(),
                        false);
                    return 1;
                })));
    }

    private static Text createEnableMessage(String type, String mode) {
        return Text.empty()
            .append(Text.literal("【123木头人】")
                .setStyle(Style.EMPTY
                    .withColor(Formatting.GOLD)
                    .withBold(true)))
            .append(Text.literal(" 效果已启用")
                .setStyle(Style.EMPTY
                    .withColor(Formatting.GREEN)))
            .append(Text.literal("\n▶ 目标类型: ")
                .setStyle(Style.EMPTY
                    .withColor(Formatting.GRAY)))
            .append(Text.literal(type)
                .setStyle(Style.EMPTY
                    .withColor(Formatting.AQUA)))
            .append(Text.literal("\n▶ 视野模式: ")
                .setStyle(Style.EMPTY
                    .withColor(Formatting.GRAY)))
            .append(Text.literal(mode)
                .setStyle(Style.EMPTY
                    .withColor(Formatting.YELLOW)));
    }

    private static Text createDisableMessage() {
        return Text.empty()
            .append(Text.literal("【123木头人】")
                .setStyle(Style.EMPTY
                    .withColor(Formatting.GOLD)
                    .withBold(true)))
            .append(Text.literal(" 效果已禁用")
                .setStyle(Style.EMPTY
                    .withColor(Formatting.RED)));
    }
}