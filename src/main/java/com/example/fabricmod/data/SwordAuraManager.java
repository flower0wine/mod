package com.example.fabricmod.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.io.InputStream;
import java.io.InputStreamReader;
import com.mojang.serialization.MapCodec;
import net.minecraft.registry.SimpleRegistry;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.List;

public class SwordAuraManager {
    private static final Gson GSON = new Gson();
    public static final RegistryKey<Registry<SwordAuraData>> SWORD_AURA_KEY = 
        RegistryKey.ofRegistry(new Identifier("fabricmod", "sword_aura"));

    public static final SwordAuraData NORMAL_CONFIG = new SwordAuraData(
        // 伤害配置
        new DamageConfig(5.0f, 2.0f, 0.8f, 1.0f),
        // 移动配置
        new MovementConfig(15.0f, 5.0f, 0.2f, 1.2f),
        // 形状配置
        new ShapeConfig(2.0f, 0.5f, 1.885f, 0.314f, 0.7f),
        // 粒子配置
        new ParticleConfig(
            100, 50, 0.2f, 0.1f,
            Arrays.asList(0.5f, 0.8f, 1.0f),  // 基础颜色：淡蓝色
            Arrays.asList(
                Arrays.asList(0.5f, 0.8f, 1.0f),  // 1级：淡蓝色
                Arrays.asList(0.8f, 0.4f, 1.0f),  // 2级：紫色
                Arrays.asList(1.0f, 0.4f, 0.4f)   // 3级：红色
            )
        ),
        // 特殊效果配置
        new EffectConfig(0.0f, 20, 1.0f, 1.0f)
    );
    
    public static final SwordAuraData CROSS_CONFIG = new SwordAuraData(
        // 伤害配置
        new DamageConfig(300.0f, 3.0f, 0.7f, 1.0f),
        // 移动配置
        new MovementConfig(20.0f, 7.0f, 0.25f, 1.3f),
        // 形状配置
        new ShapeConfig(2.5f, 0.7f, 2.199f, 0.393f, 0.6f),
        // 粒子配置
        new ParticleConfig(150, 75, 0.25f, 0.15f,
            Arrays.asList(0.8f, 0.4f, 1.0f),  // 基础颜色：紫色
            Arrays.asList(
                Arrays.asList(0.8f, 0.4f, 1.0f),  // 1级：紫色
                Arrays.asList(1.0f, 0.4f, 0.4f),  // 2级：红色
                Arrays.asList(1.0f, 0.2f, 0.2f)   // 3级：深红色
            )
        ),
        // 特殊效果配置（包含交叉角度）
        new EffectConfig(0.785398f, 20, 0.8f, 1.2f)
    );

    // 伤害相关数据
    public record DamageConfig(
        float baseDamage,
        float damageMultiplier,
        float damageDistanceMultiplier,
        float sharpnessMultiplier  // 新增锋利伤害倍率
    ) {
        public static final MapCodec<DamageConfig> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                Codec.FLOAT.fieldOf("base_damage").forGetter(DamageConfig::baseDamage),
                Codec.FLOAT.fieldOf("damage_multiplier").forGetter(DamageConfig::damageMultiplier),
                Codec.FLOAT.fieldOf("damage_distance_multiplier").forGetter(DamageConfig::damageDistanceMultiplier),
                Codec.FLOAT.fieldOf("sharpness_multiplier").forGetter(DamageConfig::sharpnessMultiplier)
            ).apply(instance, DamageConfig::new)
        );
    }

    // 移动相关数据
    public record MovementConfig(
        float maxDistance,
        float distancePerLevel,
        float moveSpeed,
        float speedMultiplier
    ) {
        public static final MapCodec<MovementConfig> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                Codec.FLOAT.fieldOf("max_distance").forGetter(MovementConfig::maxDistance),
                Codec.FLOAT.fieldOf("distance_per_level").forGetter(MovementConfig::distancePerLevel),
                Codec.FLOAT.fieldOf("move_speed").forGetter(MovementConfig::moveSpeed),
                Codec.FLOAT.fieldOf("speed_multiplier").forGetter(MovementConfig::speedMultiplier)
            ).apply(instance, MovementConfig::new)
        );
    }

    // 形状相关数据
    public record ShapeConfig(
        float arcRadius,
        float radiusPerLevel,
        float arcAngle,
        float arcAnglePerLevel,
        float innerRadiusRatio
    ) {
        public static final MapCodec<ShapeConfig> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                Codec.FLOAT.fieldOf("arc_radius").forGetter(ShapeConfig::arcRadius),
                Codec.FLOAT.fieldOf("radius_per_level").forGetter(ShapeConfig::radiusPerLevel),
                Codec.FLOAT.fieldOf("arc_angle").forGetter(ShapeConfig::arcAngle),
                Codec.FLOAT.fieldOf("arc_angle_per_level").forGetter(ShapeConfig::arcAnglePerLevel),
                Codec.FLOAT.fieldOf("inner_radius_ratio").forGetter(ShapeConfig::innerRadiusRatio)
            ).apply(instance, ShapeConfig::new)
        );
    }

    // 粒子效果数据
    public record ParticleConfig(
        int particleCount,
        int particlesPerLevel,
        float particleScale,
        float particleScalePerLevel,
        List<Float> baseColor,      // [r, g, b]
        List<List<Float>> levelColors   // 每个等级的颜色 [[r,g,b], [r,g,b], [r,g,b]]
    ) {
        public static final MapCodec<ParticleConfig> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                Codec.INT.fieldOf("particle_count").forGetter(ParticleConfig::particleCount),
                Codec.INT.fieldOf("particles_per_level").forGetter(ParticleConfig::particlesPerLevel),
                Codec.FLOAT.fieldOf("particle_scale").forGetter(ParticleConfig::particleScale),
                Codec.FLOAT.fieldOf("particle_scale_per_level").forGetter(ParticleConfig::particleScalePerLevel),
                Codec.FLOAT.listOf().fieldOf("base_color").forGetter(ParticleConfig::baseColor),
                Codec.FLOAT.listOf().listOf().fieldOf("level_colors").forGetter(ParticleConfig::levelColors)
            ).apply(instance, ParticleConfig::new)
        );
    }

    // 特殊效果数据
    public record EffectConfig(
        float rotationAngle,
        int chargeTime,
        float soundPitch,
        float soundVolume
    ) {
        public static final MapCodec<EffectConfig> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                Codec.FLOAT.optionalFieldOf("rotation_angle", 0.0f).forGetter(EffectConfig::rotationAngle),
                Codec.INT.optionalFieldOf("charge_time", 20).forGetter(EffectConfig::chargeTime),
                Codec.FLOAT.optionalFieldOf("sound_pitch", 1.0f).forGetter(EffectConfig::soundPitch),
                Codec.FLOAT.optionalFieldOf("sound_volume", 1.0f).forGetter(EffectConfig::soundVolume)
            ).apply(instance, EffectConfig::new)
        );
    }

    // 主配置类
    public record SwordAuraData(
        DamageConfig damage,
        MovementConfig movement,
        ShapeConfig shape,
        ParticleConfig particle,
        EffectConfig effect
    ) {
        public static final Codec<SwordAuraData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                DamageConfig.CODEC.forGetter(SwordAuraData::damage),
                MovementConfig.CODEC.forGetter(SwordAuraData::movement),
                ShapeConfig.CODEC.forGetter(SwordAuraData::shape),
                ParticleConfig.CODEC.forGetter(SwordAuraData::particle),
                EffectConfig.CODEC.forGetter(SwordAuraData::effect)
            ).apply(instance, SwordAuraData::new)
        );
    }

    private static Registry<SwordAuraData> registry;

    public static void register() {
        // 在初始化时创建注册表并注册默认值
        registry = FabricRegistryBuilder
            .createSimple(SWORD_AURA_KEY)
            .buildAndRegister();
            
        // 注册默认值
        Registry.register(registry, 
            new Identifier("fabricmod", "normal"), 
            NORMAL_CONFIG);
            
        Registry.register(registry, 
            new Identifier("fabricmod", "cross"), 
            CROSS_CONFIG);

        // 注册资源重载监听器，只用于更新配置
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(
            new SimpleSynchronousResourceReloadListener() {
                @Override
                public Identifier getFabricId() {
                    return new Identifier("fabricmod", "sword_aura_loader");
                }

                @Override
                public void reload(ResourceManager manager) {
                    // 加载普通剑气配置
                    manager.getResource(new Identifier("fabricmod", "sword_aura/normal.json"))
                        .ifPresent(resource -> {
                            try (InputStream stream = resource.getInputStream()) {
                                JsonElement json = GSON.fromJson(
                                    new InputStreamReader(stream), 
                                    JsonElement.class
                                );
                                SwordAuraData normalConfig = SwordAuraData.CODEC
                                    .parse(JsonOps.INSTANCE, json)
                                    .result()
                                    .orElse(NORMAL_CONFIG);
                                
                                // 更新注册表中的值
                                if (registry instanceof SimpleRegistry) {
                                    RegistryKey<SwordAuraData> key = RegistryKey.of(SWORD_AURA_KEY, new Identifier("fabricmod", "normal"));
                                    ((SimpleRegistry<SwordAuraData>)registry).set(
                                        registry.getRawId(registry.get(key)),
                                        key,
                                        normalConfig,
                                        registry.getLifecycle()
                                    );
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });

                    // 加载交叉剑气配置
                    manager.getResource(new Identifier("fabricmod", "sword_aura/cross.json"))
                        .ifPresent(resource -> {
                            try (InputStream stream = resource.getInputStream()) {
                                JsonElement json = GSON.fromJson(
                                    new InputStreamReader(stream), 
                                    JsonElement.class
                                );
                                SwordAuraData crossConfig = SwordAuraData.CODEC
                                    .parse(JsonOps.INSTANCE, json)
                                    .result()
                                    .orElse(CROSS_CONFIG);
                                
                                // 更新注册表中的值
                                if (registry instanceof SimpleRegistry) {
                                    RegistryKey<SwordAuraData> key = RegistryKey.of(SWORD_AURA_KEY, new Identifier("fabricmod", "cross"));
                                    ((SimpleRegistry<SwordAuraData>)registry).set(
                                        registry.getRawId(registry.get(key)),
                                        key,
                                        crossConfig,
                                        registry.getLifecycle()
                                    );
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                }
            }
        );
    }
} 