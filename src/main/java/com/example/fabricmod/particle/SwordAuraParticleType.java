package com.example.fabricmod.particle;

import com.mojang.serialization.Codec;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class SwordAuraParticleType extends ParticleType<SwordAuraParticleEffect> {
    public static final SwordAuraParticleType SWORD_AURA = new SwordAuraParticleType();
    
    public SwordAuraParticleType() {
        super(true, SwordAuraParticleEffect.PARAMETERS_FACTORY);
    }

    public static void register() {
        Registry.register(Registries.PARTICLE_TYPE, 
            new Identifier("fabricmod", "sword_aura"), SWORD_AURA);
    }

    @Override
    public Codec<SwordAuraParticleEffect> getCodec() {
        return SwordAuraParticleEffect.CODEC;
    }
}