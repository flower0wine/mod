package com.example.fabricmod.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;

public class SwordAuraParticleFactory implements ParticleFactory<SwordAuraParticleEffect> {
    private final SpriteProvider spriteProvider;

    public SwordAuraParticleFactory(SpriteProvider spriteProvider) {
        this.spriteProvider = spriteProvider;
    }

    @Override
    public Particle createParticle(SwordAuraParticleEffect effect, ClientWorld world,
                                 double x, double y, double z,
                                 double velocityX, double velocityY, double velocityZ) {
        return new SwordAuraParticle(world, x, y, z, velocityX, velocityY, velocityZ,
                                   this.spriteProvider, effect.getLevel(), effect.getConfigType());
    }
} 