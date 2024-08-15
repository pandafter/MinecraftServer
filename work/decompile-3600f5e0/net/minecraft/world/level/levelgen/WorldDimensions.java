package net.minecraft.world.level.levelgen;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.IRegistry;
import net.minecraft.core.IRegistryCustom;
import net.minecraft.core.IRegistryWritable;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.RegistryMaterials;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.World;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterLists;
import net.minecraft.world.level.biome.WorldChunkManager;
import net.minecraft.world.level.biome.WorldChunkManagerMultiNoise;
import net.minecraft.world.level.biome.WorldChunkManagerTheEnd;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionManager;
import net.minecraft.world.level.dimension.WorldDimension;
import net.minecraft.world.level.storage.WorldDataServer;

public record WorldDimensions(Map<ResourceKey<WorldDimension>, WorldDimension> dimensions) {

    public static final MapCodec<WorldDimensions> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(Codec.unboundedMap(ResourceKey.codec(Registries.LEVEL_STEM), WorldDimension.CODEC).fieldOf("dimensions").forGetter(WorldDimensions::dimensions)).apply(instance, instance.stable(WorldDimensions::new));
    });
    private static final Set<ResourceKey<WorldDimension>> BUILTIN_ORDER = ImmutableSet.of(WorldDimension.OVERWORLD, WorldDimension.NETHER, WorldDimension.END);
    private static final int VANILLA_DIMENSION_COUNT = WorldDimensions.BUILTIN_ORDER.size();

    public WorldDimensions(Map<ResourceKey<WorldDimension>, WorldDimension> dimensions) {
        WorldDimension worlddimension = (WorldDimension) dimensions.get(WorldDimension.OVERWORLD);

        if (worlddimension == null) {
            throw new IllegalStateException("Overworld settings missing");
        } else {
            this.dimensions = dimensions;
        }
    }

    public WorldDimensions(IRegistry<WorldDimension> iregistry) {
        this((Map) iregistry.holders().collect(Collectors.toMap(Holder.c::key, Holder.c::value)));
    }

    public static Stream<ResourceKey<WorldDimension>> keysInOrder(Stream<ResourceKey<WorldDimension>> stream) {
        return Stream.concat(WorldDimensions.BUILTIN_ORDER.stream(), stream.filter((resourcekey) -> {
            return !WorldDimensions.BUILTIN_ORDER.contains(resourcekey);
        }));
    }

    public WorldDimensions replaceOverworldGenerator(IRegistryCustom iregistrycustom, ChunkGenerator chunkgenerator) {
        IRegistry<DimensionManager> iregistry = iregistrycustom.registryOrThrow(Registries.DIMENSION_TYPE);
        Map<ResourceKey<WorldDimension>, WorldDimension> map = withOverworld(iregistry, this.dimensions, chunkgenerator);

        return new WorldDimensions(map);
    }

    public static Map<ResourceKey<WorldDimension>, WorldDimension> withOverworld(IRegistry<DimensionManager> iregistry, Map<ResourceKey<WorldDimension>, WorldDimension> map, ChunkGenerator chunkgenerator) {
        WorldDimension worlddimension = (WorldDimension) map.get(WorldDimension.OVERWORLD);
        Holder<DimensionManager> holder = worlddimension == null ? iregistry.getHolderOrThrow(BuiltinDimensionTypes.OVERWORLD) : worlddimension.type();

        return withOverworld(map, (Holder) holder, chunkgenerator);
    }

    public static Map<ResourceKey<WorldDimension>, WorldDimension> withOverworld(Map<ResourceKey<WorldDimension>, WorldDimension> map, Holder<DimensionManager> holder, ChunkGenerator chunkgenerator) {
        Builder<ResourceKey<WorldDimension>, WorldDimension> builder = ImmutableMap.builder();

        builder.putAll(map);
        builder.put(WorldDimension.OVERWORLD, new WorldDimension(holder, chunkgenerator));
        return builder.buildKeepingLast();
    }

    public ChunkGenerator overworld() {
        WorldDimension worlddimension = (WorldDimension) this.dimensions.get(WorldDimension.OVERWORLD);

        if (worlddimension == null) {
            throw new IllegalStateException("Overworld settings missing");
        } else {
            return worlddimension.generator();
        }
    }

    public Optional<WorldDimension> get(ResourceKey<WorldDimension> resourcekey) {
        return Optional.ofNullable((WorldDimension) this.dimensions.get(resourcekey));
    }

    public ImmutableSet<ResourceKey<World>> levels() {
        return (ImmutableSet) this.dimensions().keySet().stream().map(Registries::levelStemToLevel).collect(ImmutableSet.toImmutableSet());
    }

    public boolean isDebug() {
        return this.overworld() instanceof ChunkProviderDebug;
    }

    private static WorldDataServer.a specialWorldProperty(IRegistry<WorldDimension> iregistry) {
        return (WorldDataServer.a) iregistry.getOptional(WorldDimension.OVERWORLD).map((worlddimension) -> {
            ChunkGenerator chunkgenerator = worlddimension.generator();

            return chunkgenerator instanceof ChunkProviderDebug ? WorldDataServer.a.DEBUG : (chunkgenerator instanceof ChunkProviderFlat ? WorldDataServer.a.FLAT : WorldDataServer.a.NONE);
        }).orElse(WorldDataServer.a.NONE);
    }

    static Lifecycle checkStability(ResourceKey<WorldDimension> resourcekey, WorldDimension worlddimension) {
        return isVanillaLike(resourcekey, worlddimension) ? Lifecycle.stable() : Lifecycle.experimental();
    }

    private static boolean isVanillaLike(ResourceKey<WorldDimension> resourcekey, WorldDimension worlddimension) {
        return resourcekey == WorldDimension.OVERWORLD ? isStableOverworld(worlddimension) : (resourcekey == WorldDimension.NETHER ? isStableNether(worlddimension) : (resourcekey == WorldDimension.END ? isStableEnd(worlddimension) : false));
    }

    private static boolean isStableOverworld(WorldDimension worlddimension) {
        Holder<DimensionManager> holder = worlddimension.type();

        if (!holder.is(BuiltinDimensionTypes.OVERWORLD) && !holder.is(BuiltinDimensionTypes.OVERWORLD_CAVES)) {
            return false;
        } else {
            WorldChunkManager worldchunkmanager = worlddimension.generator().getBiomeSource();

            if (worldchunkmanager instanceof WorldChunkManagerMultiNoise) {
                WorldChunkManagerMultiNoise worldchunkmanagermultinoise = (WorldChunkManagerMultiNoise) worldchunkmanager;

                if (!worldchunkmanagermultinoise.stable(MultiNoiseBiomeSourceParameterLists.OVERWORLD)) {
                    return false;
                }
            }

            return true;
        }
    }

    private static boolean isStableNether(WorldDimension worlddimension) {
        boolean flag;

        if (worlddimension.type().is(BuiltinDimensionTypes.NETHER)) {
            ChunkGenerator chunkgenerator = worlddimension.generator();

            if (chunkgenerator instanceof ChunkGeneratorAbstract) {
                ChunkGeneratorAbstract chunkgeneratorabstract = (ChunkGeneratorAbstract) chunkgenerator;

                if (chunkgeneratorabstract.stable(GeneratorSettingBase.NETHER)) {
                    WorldChunkManager worldchunkmanager = chunkgeneratorabstract.getBiomeSource();

                    if (worldchunkmanager instanceof WorldChunkManagerMultiNoise) {
                        WorldChunkManagerMultiNoise worldchunkmanagermultinoise = (WorldChunkManagerMultiNoise) worldchunkmanager;

                        if (worldchunkmanagermultinoise.stable(MultiNoiseBiomeSourceParameterLists.NETHER)) {
                            flag = true;
                            return flag;
                        }
                    }
                }
            }
        }

        flag = false;
        return flag;
    }

    private static boolean isStableEnd(WorldDimension worlddimension) {
        boolean flag;

        if (worlddimension.type().is(BuiltinDimensionTypes.END)) {
            ChunkGenerator chunkgenerator = worlddimension.generator();

            if (chunkgenerator instanceof ChunkGeneratorAbstract) {
                ChunkGeneratorAbstract chunkgeneratorabstract = (ChunkGeneratorAbstract) chunkgenerator;

                if (chunkgeneratorabstract.stable(GeneratorSettingBase.END) && chunkgeneratorabstract.getBiomeSource() instanceof WorldChunkManagerTheEnd) {
                    flag = true;
                    return flag;
                }
            }
        }

        flag = false;
        return flag;
    }

    public WorldDimensions.b bake(IRegistry<WorldDimension> iregistry) {
        Stream<ResourceKey<WorldDimension>> stream = Stream.concat(iregistry.registryKeySet().stream(), this.dimensions.keySet().stream()).distinct();
        List<a> list = new ArrayList();

        keysInOrder(stream).forEach((resourcekey) -> {
            iregistry.getOptional(resourcekey).or(() -> {
                return Optional.ofNullable((WorldDimension) this.dimensions.get(resourcekey));
            }).ifPresent((worlddimension) -> {
                record a(ResourceKey<WorldDimension> key, WorldDimension value) {

                    RegistrationInfo registrationInfo() {
                        return new RegistrationInfo(Optional.empty(), WorldDimensions.checkStability(this.key, this.value));
                    }
                }

                list.add(new a(resourcekey, worlddimension));
            });
        });
        Lifecycle lifecycle = list.size() == WorldDimensions.VANILLA_DIMENSION_COUNT ? Lifecycle.stable() : Lifecycle.experimental();
        IRegistryWritable<WorldDimension> iregistrywritable = new RegistryMaterials<>(Registries.LEVEL_STEM, lifecycle);

        list.forEach((a0) -> {
            iregistrywritable.register(a0.key, (Object) a0.value, a0.registrationInfo());
        });
        IRegistry<WorldDimension> iregistry1 = iregistrywritable.freeze();
        WorldDataServer.a worlddataserver_a = specialWorldProperty(iregistry1);

        return new WorldDimensions.b(iregistry1.freeze(), worlddataserver_a);
    }

    public static record b(IRegistry<WorldDimension> dimensions, WorldDataServer.a specialWorldProperty) {

        public Lifecycle lifecycle() {
            return this.dimensions.registryLifecycle();
        }

        public IRegistryCustom.Dimension dimensionsRegistryAccess() {
            return (new IRegistryCustom.c(List.of(this.dimensions))).freeze();
        }
    }
}
