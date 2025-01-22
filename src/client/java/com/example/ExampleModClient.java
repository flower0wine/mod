package com.example;

import com.example.fabricmod.particle.SwordAuraParticleFactory;
import com.example.fabricmod.particle.SwordAuraParticleType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.particle.ParticleTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.network.PacketByteBuf;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import com.example.fabricmod.client.MouseStateHandler;
import com.example.fabricmod.effects.SwordAuraEffectClient;

public class ExampleModClient implements ClientModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger("fabricmod");

	@Override
	public void onInitializeClient() {
		LOGGER.info("初始化客户端粒子效果系统");
		
		// 注册粒子工厂
		ParticleFactoryRegistry.getInstance()
			.register(SwordAuraParticleType.SWORD_AURA, SwordAuraParticleFactory::new);

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