package com.example.fabricmod;

import com.example.fabricmod.audio.AudioCapture;
import com.example.fabricmod.audio.AudioDevice;
import com.example.fabricmod.audio.AudioVisualizer;
import com.example.fabricmod.client.render.MagicWandRenderer;
import com.example.fabricmod.entity.ModEntities;
import com.example.fabricmod.keybinding.KeyBindings;
import com.example.fabricmod.particle.*;
import com.example.fabricmod.render.WeaponDisplayEntityRenderer;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import com.example.fabricmod.effects.SwordAuraEffectClient;
import com.example.fabricmod.item.MagicWandItem;
import com.example.fabricmod.item.GamblerCardItem;
import net.minecraft.sound.SoundEvents;
import com.example.fabricmod.audio.VoiceJumpController;
import com.example.fabricmod.render.MeteorEntityRenderer;
import com.example.fabricmod.particle.*;

import java.util.List;

public class ExampleModClient implements ClientModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger(ExampleMod.MOD_ID);

	@Override
	public void onInitializeClient() {
		KeyBindings.register();

		// 注册物品展示实体渲染器
		EntityRendererRegistry.register(ModEntities.WEAPON_DISPLAY, WeaponDisplayEntityRenderer::new);
		EntityRendererRegistry.register(ModEntities.METEOR, MeteorEntityRenderer::new);

		// 注册粒子工厂
		ParticleFactoryRegistry.getInstance()
			.register(SwordAuraParticleType.SWORD_AURA, SwordAuraParticleFactory::new);

		// 注册魔法棒的粒子效果
        WorldRenderEvents.END.register(MagicWandRenderer::renderWandEffects);

		// 注册命令
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(ClientCommandManager.literal("audiovis")
				.then(ClientCommandManager.literal("start")
					.executes(context -> {
						AudioVisualizer.start(0); // 使用默认设备（序号0）
						context.getSource().sendFeedback(Text.literal("音频可视化已启动（使用默认设备）"));
						return 1;
					})
					.then(ClientCommandManager.argument("index", IntegerArgumentType.integer(0))
						.executes(context -> {
							int index = IntegerArgumentType.getInteger(context, "index");
							AudioVisualizer.start(index);
							context.getSource().sendFeedback(Text.literal("音频可视化已启动（使用设备 " + index + "）"));
							return 1;
						})))
				.then(ClientCommandManager.literal("list")
					.executes(context -> {
						List<AudioDevice> devices = AudioCapture.getAvailableInputs();
						StringBuilder message = new StringBuilder("可用的音频输入设备：\n");
						for (AudioDevice device : devices) {
							message.append(String.format("[%d] %s\n", device.index(), device.name()));
						}
						context.getSource().sendFeedback(Text.literal(message.toString()));
						return 1;
					}))
				.then(ClientCommandManager.literal("stop")
					.executes(context -> {
						AudioVisualizer.stop();
						context.getSource().sendFeedback(Text.literal("音频可视化已停止"));
						return 1;
					}))
			);
		});

		ClientPlayNetworking.registerGlobalReceiver(
			MagicWandItem.MAGIC_WAND_STRIKE,
			(client, handler, buf, responseSender) -> {
				double x = buf.readDouble();
				double y = buf.readDouble();
				double z = buf.readDouble();
				int sharpnessLevel = buf.readInt();

				Vec3d pos = new Vec3d(x, y, z);
				
				// 在主线程执行粒子效果生成
				client.execute(() -> {
					MagicWandParticles.spawnLightningParticles(client.world, pos, sharpnessLevel);
				});
			}
		);

		// 注册陨石撞击效果处理器
        ClientPlayNetworking.registerGlobalReceiver(
            new Identifier("fabricmod", "meteor_impact"),
            (client, handler, buf, responseSender) -> {
                double x = buf.readDouble();
                double y = buf.readDouble();
                double z = buf.readDouble();
                
                // 在主线程执行粒子效果
                client.execute(() -> {
                    MeteorImpactHandler.createImpactWave(client.world, x, y, z);
                });
            }
        );

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player != null && client.player.isOnGround() && 
				(client.player.getVelocity().x != 0 || client.player.getVelocity().z != 0) &&
				client.world != null) {
				
				double x = client.player.getX();
				double y = client.player.getY();
				double z = client.player.getZ();

				client.world.addParticle(
					ParticleTypes.END_ROD,
					x,
					y + 0.1,
					z,
					0,
					0,
					0
				);
			}
		});

		ClientPlayNetworking.registerGlobalReceiver(
			new Identifier(ExampleMod.MOD_ID, "sword_aura"),
			(client, handler, buf, responseSender) -> {
				double x = buf.readDouble();
				double y = buf.readDouble();
				double z = buf.readDouble();
				double dirX = buf.readDouble();
				double dirY = buf.readDouble();
				double dirZ = buf.readDouble();
				int level = buf.readInt();
				double scaleMultiplier = buf.readDouble();  // 读取缩放参数
				
				Vec3d position = new Vec3d(x, y, z);
				Vec3d direction = new Vec3d(dirX, dirY, dirZ);
				
				client.execute(() -> {
					SwordAuraEffectClient.createAura(client.world, position, direction, level, scaleMultiplier);
				});
			}
		);

		ClientPlayNetworking.registerGlobalReceiver(
            new Identifier(ExampleMod.MOD_ID, "sword_cross_aura"),
            (client, handler, buf, responseSender) -> {
                double x = buf.readDouble();
                double y = buf.readDouble();
                double z = buf.readDouble();
                double dirX = buf.readDouble();
                double dirY = buf.readDouble();
                double dirZ = buf.readDouble();
                int level = buf.readInt();
                double rotationAngle = buf.readDouble();
                double scaleMultiplier = buf.readDouble();  // 读取缩放参数
                
                Vec3d position = new Vec3d(x, y, z);
                Vec3d direction = new Vec3d(dirX, dirY, dirZ);
                
                client.execute(() -> {
                    SwordAuraEffectClient.createCrossAura(
                        client.world,
                        position,
                        direction,
                        level,
                        rotationAngle,
                        scaleMultiplier
                    );
                });
            }
        );

		ClientPlayNetworking.registerGlobalReceiver(GamblerCardItem.GAMBLER_CARD_ANIMATION,
			(client, handler, buf, responseSender) -> {
				client.execute(() -> {
					ClientPlayerEntity player = client.player;
					if (player != null && client.world != null) {
						// 获取玩家手中的魔法棒
						ItemStack wand = player.getMainHandStack();
						if (wand.getItem() instanceof GamblerCardItem) {
							// 添加粒子效果
							client.particleManager.addEmitter(player, ParticleTypes.TOTEM_OF_UNDYING, 30);
							
							// 添加音效
							client.world.playSound(
								player.getX(), 
								player.getY(), 
								player.getZ(), 
								SoundEvents.ITEM_TOTEM_USE, 
								player.getSoundCategory(), 
								1.0F, 
								1.0F, 
								false
							);

							// 显示浮动物品
							client.gameRenderer.showFloatingItem(wand);
						}
					}
				});
			});

		MouseStateHandler.init();
		VoiceJumpController.init();
		AudioVisualizer.init();
	}
}