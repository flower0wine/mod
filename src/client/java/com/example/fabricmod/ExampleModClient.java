package com.example.fabricmod;

import com.example.fabricmod.client.render.MagicWandRenderer;
import com.example.fabricmod.particle.MagicWandParticles;
import com.example.fabricmod.particle.SwordAuraParticleFactory;
import com.example.fabricmod.particle.SwordAuraParticleType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.particle.ParticleTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import com.example.fabricmod.ExampleMod;
import com.example.fabricmod.client.MouseStateHandler;
import com.example.fabricmod.effects.SwordAuraEffectClient;
import com.example.fabricmod.item.MagicWandItem;

public class ExampleModClient implements ClientModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger(ExampleMod.MOD_ID);

	@Override
	public void onInitializeClient() {
		// 注册粒子工厂
		ParticleFactoryRegistry.getInstance()
			.register(SwordAuraParticleType.SWORD_AURA, SwordAuraParticleFactory::new);

		// 注册魔法棒的粒子效果
        WorldRenderEvents.END.register(MagicWandRenderer::renderWandEffects);

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

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player != null && client.player.isOnGround() && 
				(client.player.getVelocity().x != 0 || client.player.getVelocity().z != 0)) {
				
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
			new Identifier("fabricmod", "sword_aura"),
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
            new Identifier("fabricmod", "sword_cross_aura"),
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

		MouseStateHandler.init();
	}
}