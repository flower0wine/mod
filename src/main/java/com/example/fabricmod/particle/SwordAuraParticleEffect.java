package com.example.fabricmod.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;

public class SwordAuraParticleEffect implements ParticleEffect {
    private final ParticleType<SwordAuraParticleEffect> type;
    private final float velocityX, velocityY, velocityZ;
    private final float targetOffsetX, targetOffsetY, targetOffsetZ;
    private final int level;
    private final String configType;
    
    public static final Codec<SwordAuraParticleEffect> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.FLOAT.fieldOf("velocityX").forGetter(effect -> effect.velocityX),
            Codec.FLOAT.fieldOf("velocityY").forGetter(effect -> effect.velocityY),
            Codec.FLOAT.fieldOf("velocityZ").forGetter(effect -> effect.velocityZ),
            Codec.FLOAT.fieldOf("targetOffsetX").forGetter(effect -> effect.targetOffsetX),
            Codec.FLOAT.fieldOf("targetOffsetY").forGetter(effect -> effect.targetOffsetY),
            Codec.FLOAT.fieldOf("targetOffsetZ").forGetter(effect -> effect.targetOffsetZ),
            Codec.INT.fieldOf("level").forGetter(effect -> effect.level),
            Codec.STRING.fieldOf("configType").forGetter(effect -> effect.configType)
        ).apply(instance, (vx, vy, vz, tx, ty, tz, level, configType) -> 
            new SwordAuraParticleEffect(SwordAuraParticleType.SWORD_AURA, vx, vy, vz, tx, ty, tz, level, configType))
    );

    public static final ParticleEffect.Factory<SwordAuraParticleEffect> PARAMETERS_FACTORY = 
        new ParticleEffect.Factory<SwordAuraParticleEffect>() {
            public SwordAuraParticleEffect read(ParticleType<SwordAuraParticleEffect> type, 
                StringReader reader) {
                return new SwordAuraParticleEffect(type);
            }

            public SwordAuraParticleEffect read(ParticleType<SwordAuraParticleEffect> type, 
                PacketByteBuf buf) {
                return new SwordAuraParticleEffect(type,
                    buf.readFloat(), buf.readFloat(), buf.readFloat(),
                    buf.readFloat(), buf.readFloat(), buf.readFloat(),
                    buf.readInt(), buf.readString());
            }
    };

    public SwordAuraParticleEffect(ParticleType<SwordAuraParticleEffect> type) {
        this(type, 0, 0, 0, 0, 0, 0, 0, "normal");
    }

    public SwordAuraParticleEffect(ParticleType<SwordAuraParticleEffect> type,
                                  float velocityX, float velocityY, float velocityZ,
                                  float targetOffsetX, float targetOffsetY, float targetOffsetZ,
                                  int level, String configType) {
        this.type = type;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.velocityZ = velocityZ;
        this.targetOffsetX = targetOffsetX;
        this.targetOffsetY = targetOffsetY;
        this.targetOffsetZ = targetOffsetZ;
        this.level = level;
        this.configType = configType;
    }

    @Override
    public ParticleType<?> getType() {
        return this.type;
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeFloat(this.velocityX);
        buf.writeFloat(this.velocityY);
        buf.writeFloat(this.velocityZ);
        buf.writeFloat(this.targetOffsetX);
        buf.writeFloat(this.targetOffsetY);
        buf.writeFloat(this.targetOffsetZ);
        buf.writeInt(this.level);
        buf.writeString(this.configType);
    }

    @Override
    public String asString() {
        return Registries.PARTICLE_TYPE.getId(this.type).toString();
    }

    // Getters
    public float getVelocityX() { return velocityX; }
    public float getVelocityY() { return velocityY; }
    public float getVelocityZ() { return velocityZ; }
    public float getTargetOffsetX() { return targetOffsetX; }
    public float getTargetOffsetY() { return targetOffsetY; }
    public float getTargetOffsetZ() { return targetOffsetZ; }
    public int getLevel() { return level; }
    public String getConfigType() { return configType; }
}