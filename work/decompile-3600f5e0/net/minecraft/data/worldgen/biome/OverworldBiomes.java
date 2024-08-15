package net.minecraft.data.worldgen.biome;

import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.worldgen.BiomeSettings;
import net.minecraft.data.worldgen.WorldGenCarvers;
import net.minecraft.data.worldgen.placement.AquaticPlacements;
import net.minecraft.data.worldgen.placement.MiscOverworldPlacements;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.Musics;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.util.MathHelper;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EnumCreatureType;
import net.minecraft.world.level.biome.BiomeBase;
import net.minecraft.world.level.biome.BiomeFog;
import net.minecraft.world.level.biome.BiomeSettingsGeneration;
import net.minecraft.world.level.biome.BiomeSettingsMobs;
import net.minecraft.world.level.biome.CaveSoundSettings;
import net.minecraft.world.level.levelgen.WorldGenStage;
import net.minecraft.world.level.levelgen.carver.WorldGenCarverWrapper;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class OverworldBiomes {

    protected static final int NORMAL_WATER_COLOR = 4159204;
    protected static final int NORMAL_WATER_FOG_COLOR = 329011;
    private static final int OVERWORLD_FOG_COLOR = 12638463;
    @Nullable
    private static final Music NORMAL_MUSIC = null;

    public OverworldBiomes() {}

    protected static int calculateSkyColor(float f) {
        float f1 = f / 3.0F;

        f1 = MathHelper.clamp(f1, -1.0F, 1.0F);
        return MathHelper.hsvToRgb(0.62222224F - f1 * 0.05F, 0.5F + f1 * 0.1F, 1.0F);
    }

    private static BiomeBase biome(boolean flag, float f, float f1, BiomeSettingsMobs.a biomesettingsmobs_a, BiomeSettingsGeneration.a biomesettingsgeneration_a, @Nullable Music music) {
        return biome(flag, f, f1, 4159204, 329011, (Integer) null, (Integer) null, biomesettingsmobs_a, biomesettingsgeneration_a, music);
    }

    private static BiomeBase biome(boolean flag, float f, float f1, int i, int j, @Nullable Integer integer, @Nullable Integer integer1, BiomeSettingsMobs.a biomesettingsmobs_a, BiomeSettingsGeneration.a biomesettingsgeneration_a, @Nullable Music music) {
        BiomeFog.a biomefog_a = (new BiomeFog.a()).waterColor(i).waterFogColor(j).fogColor(12638463).skyColor(calculateSkyColor(f)).ambientMoodSound(CaveSoundSettings.LEGACY_CAVE_SETTINGS).backgroundMusic(music);

        if (integer != null) {
            biomefog_a.grassColorOverride(integer);
        }

        if (integer1 != null) {
            biomefog_a.foliageColorOverride(integer1);
        }

        return (new BiomeBase.a()).hasPrecipitation(flag).temperature(f).downfall(f1).specialEffects(biomefog_a.build()).mobSpawnSettings(biomesettingsmobs_a.build()).generationSettings(biomesettingsgeneration_a.build()).build();
    }

    private static void globalOverworldGeneration(BiomeSettingsGeneration.a biomesettingsgeneration_a) {
        BiomeSettings.addDefaultCarversAndLakes(biomesettingsgeneration_a);
        BiomeSettings.addDefaultCrystalFormations(biomesettingsgeneration_a);
        BiomeSettings.addDefaultMonsterRoom(biomesettingsgeneration_a);
        BiomeSettings.addDefaultUndergroundVariety(biomesettingsgeneration_a);
        BiomeSettings.addDefaultSprings(biomesettingsgeneration_a);
        BiomeSettings.addSurfaceFreezing(biomesettingsgeneration_a);
    }

    public static BiomeBase oldGrowthTaiga(HolderGetter<PlacedFeature> holdergetter, HolderGetter<WorldGenCarverWrapper<?>> holdergetter1, boolean flag) {
        BiomeSettingsMobs.a biomesettingsmobs_a = new BiomeSettingsMobs.a();

        BiomeSettings.farmAnimals(biomesettingsmobs_a);
        biomesettingsmobs_a.addSpawn(EnumCreatureType.CREATURE, new BiomeSettingsMobs.c(EntityTypes.WOLF, 8, 4, 4));
        biomesettingsmobs_a.addSpawn(EnumCreatureType.CREATURE, new BiomeSettingsMobs.c(EntityTypes.RABBIT, 4, 2, 3));
        biomesettingsmobs_a.addSpawn(EnumCreatureType.CREATURE, new BiomeSettingsMobs.c(EntityTypes.FOX, 8, 2, 4));
        if (flag) {
            BiomeSettings.commonSpawns(biomesettingsmobs_a);
        } else {
            BiomeSettings.caveSpawns(biomesettingsmobs_a);
            BiomeSettings.monsters(biomesettingsmobs_a, 100, 25, 100, false);
        }

        BiomeSettingsGeneration.a biomesettingsgeneration_a = new BiomeSettingsGeneration.a(holdergetter, holdergetter1);

        globalOverworldGeneration(biomesettingsgeneration_a);
        BiomeSettings.addMossyStoneBlock(biomesettingsgeneration_a);
        BiomeSettings.addFerns(biomesettingsgeneration_a);
        BiomeSettings.addDefaultOres(biomesettingsgeneration_a);
        BiomeSettings.addDefaultSoftDisks(biomesettingsgeneration_a);
        biomesettingsgeneration_a.addFeature(WorldGenStage.Decoration.VEGETAL_DECORATION, flag ? VegetationPlacements.TREES_OLD_GROWTH_SPRUCE_TAIGA : VegetationPlacements.TREES_OLD_GROWTH_PINE_TAIGA);
        BiomeSettings.addDefaultFlowers(biomesettingsgeneration_a);
        BiomeSettings.addGiantTaigaVegetation(biomesettingsgeneration_a);
        BiomeSettings.addDefaultMushrooms(biomesettingsgeneration_a);
        BiomeSettings.addDefaultExtraVegetation(biomesettingsgeneration_a);
        BiomeSettings.addCommonBerryBushes(biomesettingsgeneration_a);
        Music music = Musics.createGameMusic(SoundEffects.MUSIC_BIOME_OLD_GROWTH_TAIGA);

        return biome(true, flag ? 0.25F : 0.3F, 0.8F, biomesettingsmobs_a, biomesettingsgeneration_a, music);
    }

    public static BiomeBase sparseJungle(HolderGetter<PlacedFeature> holdergetter, HolderGetter<WorldGenCarverWrapper<?>> holdergetter1) {
        BiomeSettingsMobs.a biomesettingsmobs_a = new BiomeSettingsMobs.a();

        BiomeSettings.baseJungleSpawns(biomesettingsmobs_a);
        biomesettingsmobs_a.addSpawn(EnumCreatureType.CREATURE, new BiomeSettingsMobs.c(EntityTypes.WOLF, 8, 2, 4));
        return baseJungle(holdergetter, holdergetter1, 0.8F, false, true, false, biomesettingsmobs_a, Musics.createGameMusic(SoundEffects.MUSIC_BIOME_SPARSE_JUNGLE));
    }

    public static BiomeBase jungle(HolderGetter<PlacedFeature> holdergetter, HolderGetter<WorldGenCarverWrapper<?>> holdergetter1) {
        BiomeSettingsMobs.a biomesettingsmobs_a = new BiomeSettingsMobs.a();

        BiomeSettings.baseJungleSpawns(biomesettingsmobs_a);
        biomesettingsmobs_a.addSpawn(EnumCreatureType.CREATURE, new BiomeSettingsMobs.c(EntityTypes.PARROT, 40, 1, 2)).addSpawn(EnumCreatureType.MONSTER, new BiomeSettingsMobs.c(EntityTypes.OCELOT, 2, 1, 3)).addSpawn(EnumCreatureType.CREATURE, new BiomeSettingsMobs.c(EntityTypes.PANDA, 1, 1, 2));
        return baseJungle(holdergetter, holdergetter1, 0.9F, false, false, true, biomesettingsmobs_a, Musics.createGameMusic(SoundEffects.MUSIC_BIOME_JUNGLE));
    }

    public static BiomeBase bambooJungle(HolderGetter<PlacedFeature> holdergetter, HolderGetter<WorldGenCarverWrapper<?>> holdergetter1) {
        BiomeSettingsMobs.a biomesettingsmobs_a = new BiomeSettingsMobs.a();

        BiomeSettings.baseJungleSpawns(biomesettingsmobs_a);
        biomesettingsmobs_a.addSpawn(EnumCreatureType.CREATURE, new BiomeSettingsMobs.c(EntityTypes.PARROT, 40, 1, 2)).addSpawn(EnumCreatureType.CREATURE, new BiomeSettingsMobs.c(EntityTypes.PANDA, 80, 1, 2)).addSpawn(EnumCreatureType.MONSTER, new BiomeSettingsMobs.c(EntityTypes.OCELOT, 2, 1, 1));
        return baseJungle(holdergetter, holdergetter1, 0.9F, true, false, true, biomesettingsmobs_a, Musics.createGameMusic(SoundEffects.MUSIC_BIOME_BAMBOO_JUNGLE));
    }

    private static BiomeBase baseJungle(HolderGetter<PlacedFeature> holdergetter, HolderGetter<WorldGenCarverWrapper<?>> holdergetter1, float f, boolean flag, boolean flag1, boolean flag2, BiomeSettingsMobs.a biomesettingsmobs_a, Music music) {
        BiomeSettingsGeneration.a biomesettingsgeneration_a = new BiomeSettingsGeneration.a(holdergetter, holdergetter1);

        globalOverworldGeneration(biomesettingsgeneration_a);
        BiomeSettings.addDefaultOres(biomesettingsgeneration_a);
        BiomeSettings.addDefaultSoftDisks(biomesettingsgeneration_a);
        if (flag) {
            BiomeSettings.addBambooVegetation(biomesettingsgeneration_a);
        } else {
            if (flag2) {
                BiomeSettings.addLightBambooVegetation(biomesettingsgeneration_a);
            }

            if (flag1) {
                BiomeSettings.addSparseJungleTrees(biomesettingsgeneration_a);
            } else {
                BiomeSettings.addJungleTrees(biomesettingsgeneration_a);
            }
        }

        BiomeSettings.addWarmFlowers(biomesettingsgeneration_a);
        BiomeSettings.addJungleGrass(biomesettingsgeneration_a);
        BiomeSettings.addDefaultMushrooms(biomesettingsgeneration_a);
        BiomeSettings.addDefaultExtraVegetation(biomesettingsgeneration_a);
        BiomeSettings.addJungleVines(biomesettingsgeneration_a);
        if (flag1) {
            BiomeSettings.addSparseJungleMelons(biomesettingsgeneration_a);
        } else {
            BiomeSettings.addJungleMelons(biomesettingsgeneration_a);
        }

        return biome(true, 0.95F, f, biomesettingsmobs_a, biomesettingsgeneration_a, music);
    }

    public static BiomeBase windsweptHills(HolderGetter<PlacedFeature> holdergetter, HolderGetter<WorldGenCarverWrapper<?>> holdergetter1, boolean flag) {
        BiomeSettingsMobs.a biomesettingsmobs_a = new BiomeSettingsMobs.a();

        BiomeSettings.farmAnimals(biomesettingsmobs_a);
        biomesettingsmobs_a.addSpawn(EnumCreatureType.CREATURE, new BiomeSettingsMobs.c(EntityTypes.LLAMA, 5, 4, 6));
        BiomeSettings.commonSpawns(biomesettingsmobs_a);
        BiomeSettingsGeneration.a biomesettingsgeneration_a = new BiomeSettingsGeneration.a(holdergetter, holdergetter1);

        globalOverworldGeneration(biomesettingsgeneration_a);
        BiomeSettings.addDefaultOres(biomesettingsgeneration_a);
        BiomeSettings.addDefaultSoftDisks(biomesettingsgeneration_a);
        if (flag) {
            BiomeSettings.addMountainForestTrees(biomesettingsgeneration_a);
        } else {
            BiomeSettings.addMountainTrees(biomesettingsgeneration_a);
        }

        BiomeSettings.addDefaultFlowers(biomesettingsgeneration_a);
        BiomeSettings.addDefaultGrass(biomesettingsgeneration_a);
        BiomeSettings.addDefaultMushrooms(biomesettingsgeneration_a);
        BiomeSettings.addDefaultExtraVegetation(biomesettingsgeneration_a);
        BiomeSettings.addExtraEmeralds(biomesettingsgeneration_a);
        BiomeSettings.addInfestedStone(biomesettingsgeneration_a);
        return biome(true, 0.2F, 0.3F, biomesettingsmobs_a, biomesettingsgeneration_a, OverworldBiomes.NORMAL_MUSIC);
    }

    public static BiomeBase desert(HolderGetter<PlacedFeature> holdergetter, HolderGetter<WorldGenCarverWrapper<?>> holdergetter1) {
        BiomeSettingsMobs.a biomesettingsmobs_a = new BiomeSettingsMobs.a();

        BiomeSettings.desertSpawns(biomesettingsmobs_a);
        BiomeSettingsGeneration.a biomesettingsgeneration_a = new BiomeSettingsGeneration.a(holdergetter, holdergetter1);

        BiomeSettings.addFossilDecoration(biomesettingsgeneration_a);
        globalOverworldGeneration(biomesettingsgeneration_a);
        BiomeSettings.addDefaultOres(biomesettingsgeneration_a);
        BiomeSettings.addDefaultSoftDisks(biomesettingsgeneration_a);
        BiomeSettings.addDefaultFlowers(biomesettingsgeneration_a);
        BiomeSettings.addDefaultGrass(biomesettingsgeneration_a);
        BiomeSettings.addDesertVegetation(biomesettingsgeneration_a);
        BiomeSettings.addDefaultMushrooms(biomesettingsgeneration_a);
        BiomeSettings.addDesertExtraVegetation(biomesettingsgeneration_a);
        BiomeSettings.addDesertExtraDecoration(biomesettingsgeneration_a);
        return biome(false, 2.0F, 0.0F, biomesettingsmobs_a, biomesettingsgeneration_a, Musics.createGameMusic(SoundEffects.MUSIC_BIOME_DESERT));
    }

    public static BiomeBase plains(HolderGetter<PlacedFeature> holdergetter, HolderGetter<WorldGenCarverWrapper<?>> holdergetter1, boolean flag, boolean flag1, boolean flag2) {
        BiomeSettingsMobs.a biomesettingsmobs_a = new BiomeSettingsMobs.a();
        BiomeSettingsGeneration.a biomesettingsgeneration_a = new BiomeSettingsGeneration.a(holdergetter, holdergetter1);

        globalOverworldGeneration(biomesettingsgeneration_a);
        if (flag1) {
            biomesettingsmobs_a.creatureGenerationProbability(0.07F);
            BiomeSettings.snowySpawns(biomesettingsmobs_a);
            if (flag2) {
                biomesettingsgeneration_a.addFeature(WorldGenStage.Decoration.SURFACE_STRUCTURES, MiscOverworldPlacements.ICE_SPIKE);
                biomesettingsgeneration_a.addFeature(WorldGenStage.Decoration.SURFACE_STRUCTURES, MiscOverworldPlacements.ICE_PATCH);
            }
        } else {
            BiomeSettings.plainsSpawns(biomesettingsmobs_a);
            BiomeSettings.addPlainGrass(biomesettingsgeneration_a);
            if (flag) {
                biomesettingsgeneration_a.addFeature(WorldGenStage.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_SUNFLOWER);
            }
        }

        BiomeSettings.addDefaultOres(biomesettingsgeneration_a);
        BiomeSettings.addDefaultSoftDisks(biomesettingsgeneration_a);
        if (flag1) {
            BiomeSettings.addSnowyTrees(biomesettingsgeneration_a);
            BiomeSettings.addDefaultFlowers(biomesettingsgeneration_a);
            BiomeSettings.addDefaultGrass(biomesettingsgeneration_a);
        } else {
            BiomeSettings.addPlainVegetation(biomesettingsgeneration_a);
        }

        BiomeSettings.addDefaultMushrooms(biomesettingsgeneration_a);
        if (flag) {
            biomesettingsgeneration_a.addFeature(WorldGenStage.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_SUGAR_CANE);
            biomesettingsgeneration_a.addFeature(WorldGenStage.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_PUMPKIN);
        } else {
            BiomeSettings.addDefaultExtraVegetation(biomesettingsgeneration_a);
        }

        float f = flag1 ? 0.0F : 0.8F;

        return biome(true, f, flag1 ? 0.5F : 0.4F, biomesettingsmobs_a, biomesettingsgeneration_a, OverworldBiomes.NORMAL_MUSIC);
    }

    public static BiomeBase mushroomFields(HolderGetter<PlacedFeature> holdergetter, HolderGetter<WorldGenCarverWrapper<?>> holdergetter1) {
        BiomeSettingsMobs.a biomesettingsmobs_a = new BiomeSettingsMobs.a();

        BiomeSettings.mooshroomSpawns(biomesettingsmobs_a);
        BiomeSettingsGeneration.a biomesettingsgeneration_a = new BiomeSettingsGeneration.a(holdergetter, holdergetter1);

        globalOverworldGeneration(biomesettingsgeneration_a);
        BiomeSettings.addDefaultOres(biomesettingsgeneration_a);
        BiomeSettings.addDefaultSoftDisks(biomesettingsgeneration_a);
        BiomeSettings.addMushroomFieldVegetation(biomesettingsgeneration_a);
        BiomeSettings.addDefaultExtraVegetation(biomesettingsgeneration_a);
        return biome(true, 0.9F, 1.0F, biomesettingsmobs_a, biomesettingsgeneration_a, OverworldBiomes.NORMAL_MUSIC);
    }

    public static BiomeBase savanna(HolderGetter<PlacedFeature> holdergetter, HolderGetter<WorldGenCarverWrapper<?>> holdergetter1, boolean flag, boolean flag1) {
        BiomeSettingsGeneration.a biomesettingsgeneration_a = new BiomeSettingsGeneration.a(holdergetter, holdergetter1);

        globalOverworldGeneration(biomesettingsgeneration_a);
        if (!flag) {
            BiomeSettings.addSavannaGrass(biomesettingsgeneration_a);
        }

        BiomeSettings.addDefaultOres(biomesettingsgeneration_a);
        BiomeSettings.addDefaultSoftDisks(biomesettingsgeneration_a);
        if (flag) {
            BiomeSettings.addShatteredSavannaTrees(biomesettingsgeneration_a);
            BiomeSettings.addDefaultFlowers(biomesettingsgeneration_a);
            BiomeSettings.addShatteredSavannaGrass(biomesettingsgeneration_a);
        } else {
            BiomeSettings.addSavannaTrees(biomesettingsgeneration_a);
            BiomeSettings.addWarmFlowers(biomesettingsgeneration_a);
            BiomeSettings.addSavannaExtraGrass(biomesettingsgeneration_a);
        }

        BiomeSettings.addDefaultMushrooms(biomesettingsgeneration_a);
        BiomeSettings.addDefaultExtraVegetation(biomesettingsgeneration_a);
        BiomeSettingsMobs.a biomesettingsmobs_a = new BiomeSettingsMobs.a();

        BiomeSettings.farmAnimals(biomesettingsmobs_a);
        biomesettingsmobs_a.addSpawn(EnumCreatureType.CREATURE, new BiomeSettingsMobs.c(EntityTypes.HORSE, 1, 2, 6)).addSpawn(EnumCreatureType.CREATURE, new BiomeSettingsMobs.c(EntityTypes.DONKEY, 1, 1, 1)).addSpawn(EnumCreatureType.CREATURE, new BiomeSettingsMobs.c(EntityTypes.ARMADILLO, 10, 2, 3));
        BiomeSettings.commonSpawns(biomesettingsmobs_a);
        if (flag1) {
            biomesettingsmobs_a.addSpawn(EnumCreatureType.CREATURE, new BiomeSettingsMobs.c(EntityTypes.LLAMA, 8, 4, 4));
            biomesettingsmobs_a.addSpawn(EnumCreatureType.CREATURE, new BiomeSettingsMobs.c(EntityTypes.WOLF, 8, 4, 8));
        }

        return biome(false, 2.0F, 0.0F, biomesettingsmobs_a, biomesettingsgeneration_a, OverworldBiomes.NORMAL_MUSIC);
    }

    public static BiomeBase badlands(HolderGetter<PlacedFeature> holdergetter, HolderGetter<WorldGenCarverWrapper<?>> holdergetter1, boolean flag) {
        BiomeSettingsMobs.a biomesettingsmobs_a = new BiomeSettingsMobs.a();

        BiomeSettings.commonSpawns(biomesettingsmobs_a);
        biomesettingsmobs_a.addSpawn(EnumCreatureType.CREATURE, new BiomeSettingsMobs.c(EntityTypes.ARMADILLO, 6, 1, 2));
        biomesettingsmobs_a.creatureGenerationProbability(0.03F);
        if (flag) {
            biomesettingsmobs_a.addSpawn(EnumCreatureType.CREATURE, new BiomeSettingsMobs.c(EntityTypes.WOLF, 2, 4, 8));
            biomesettingsmobs_a.creatureGenerationProbability(0.04F);
        }

        BiomeSettingsGeneration.a biomesettingsgeneration_a = new BiomeSettingsGeneration.a(holdergetter, holdergetter1);

        globalOverworldGeneration(biomesettingsgeneration_a);
        BiomeSettings.addDefaultOres(biomesettingsgeneration_a);
        BiomeSettings.addExtraGold(biomesettingsgeneration_a);
        BiomeSettings.addDefaultSoftDisks(biomesettingsgeneration_a);
        if (flag) {
            BiomeSettings.addBadlandsTrees(biomesettingsgeneration_a);
        }

        BiomeSettings.addBadlandGrass(biomesettingsgeneration_a);
        BiomeSettings.addDefaultMushrooms(biomesettingsgeneration_a);
        BiomeSettings.addBadlandExtraVegetation(biomesettingsgeneration_a);
        return (new BiomeBase.a()).hasPrecipitation(false).temperature(2.0F).downfall(0.0F).specialEffects((new BiomeFog.a()).waterColor(4159204).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(2.0F)).foliageColorOverride(10387789).grassColorOverride(9470285).ambientMoodSound(CaveSoundSettings.LEGACY_CAVE_SETTINGS).backgroundMusic(Musics.createGameMusic(SoundEffects.MUSIC_BIOME_BADLANDS)).build()).mobSpawnSettings(biomesettingsmobs_a.build()).generationSettings(biomesettingsgeneration_a.build()).build();
    }

    private static BiomeBase baseOcean(BiomeSettingsMobs.a biomesettingsmobs_a, int i, int j, BiomeSettingsGeneration.a biomesettingsgeneration_a) {
        return biome(true, 0.5F, 0.5F, i, j, (Integer) null, (Integer) null, biomesettingsmobs_a, biomesettingsgeneration_a, OverworldBiomes.NORMAL_MUSIC);
    }

    private static BiomeSettingsGeneration.a baseOceanGeneration(HolderGetter<PlacedFeature> holdergetter, HolderGetter<WorldGenCarverWrapper<?>> holdergetter1) {
        BiomeSettingsGeneration.a biomesettingsgeneration_a = new BiomeSettingsGeneration.a(holdergetter, holdergetter1);

        globalOverworldGeneration(biomesettingsgeneration_a);
        BiomeSettings.addDefaultOres(biomesettingsgeneration_a);
        BiomeSettings.addDefaultSoftDisks(biomesettingsgeneration_a);
        BiomeSettings.addWaterTrees(biomesettingsgeneration_a);
        BiomeSettings.addDefaultFlowers(biomesettingsgeneration_a);
        BiomeSettings.addDefaultGrass(biomesettingsgeneration_a);
        BiomeSettings.addDefaultMushrooms(biomesettingsgeneration_a);
        BiomeSettings.addDefaultExtraVegetation(biomesettingsgeneration_a);
        return biomesettingsgeneration_a;
    }

    public static BiomeBase coldOcean(HolderGetter<PlacedFeature> holdergetter, HolderGetter<WorldGenCarverWrapper<?>> holdergetter1, boolean flag) {
        BiomeSettingsMobs.a biomesettingsmobs_a = new BiomeSettingsMobs.a();

        BiomeSettings.oceanSpawns(biomesettingsmobs_a, 3, 4, 15);
        biomesettingsmobs_a.addSpawn(EnumCreatureType.WATER_AMBIENT, new BiomeSettingsMobs.c(EntityTypes.SALMON, 15, 1, 5));
        BiomeSettingsGeneration.a biomesettingsgeneration_a = baseOceanGeneration(holdergetter, holdergetter1);

        biomesettingsgeneration_a.addFeature(WorldGenStage.Decoration.VEGETAL_DECORATION, flag ? AquaticPlacements.SEAGRASS_DEEP_COLD : AquaticPlacements.SEAGRASS_COLD);
        BiomeSettings.addDefaultSeagrass(biomesettingsgeneration_a);
        BiomeSettings.addColdOceanExtraVegetation(biomesettingsgeneration_a);
        return baseOcean(biomesettingsmobs_a, 4020182, 329011, biomesettingsgeneration_a);
    }

    public static BiomeBase ocean(HolderGetter<PlacedFeature> holdergetter, HolderGetter<WorldGenCarverWrapper<?>> holdergetter1, boolean flag) {
        BiomeSettingsMobs.a biomesettingsmobs_a = new BiomeSettingsMobs.a();

        BiomeSettings.oceanSpawns(biomesettingsmobs_a, 1, 4, 10);
        biomesettingsmobs_a.addSpawn(EnumCreatureType.WATER_CREATURE, new BiomeSettingsMobs.c(EntityTypes.DOLPHIN, 1, 1, 2));
        BiomeSettingsGeneration.a biomesettingsgeneration_a = baseOceanGeneration(holdergetter, holdergetter1);

        biomesettingsgeneration_a.addFeature(WorldGenStage.Decoration.VEGETAL_DECORATION, flag ? AquaticPlacements.SEAGRASS_DEEP : AquaticPlacements.SEAGRASS_NORMAL);
        BiomeSettings.addDefaultSeagrass(biomesettingsgeneration_a);
        BiomeSettings.addColdOceanExtraVegetation(biomesettingsgeneration_a);
        return baseOcean(biomesettingsmobs_a, 4159204, 329011, biomesettingsgeneration_a);
    }

    public static BiomeBase lukeWarmOcean(HolderGetter<PlacedFeature> holdergetter, HolderGetter<WorldGenCarverWrapper<?>> holdergetter1, boolean flag) {
        BiomeSettingsMobs.a biomesettingsmobs_a = new BiomeSettingsMobs.a();

        if (flag) {
            BiomeSettings.oceanSpawns(biomesettingsmobs_a, 8, 4, 8);
        } else {
            BiomeSettings.oceanSpawns(biomesettingsmobs_a, 10, 2, 15);
        }

        biomesettingsmobs_a.addSpawn(EnumCreatureType.WATER_AMBIENT, new BiomeSettingsMobs.c(EntityTypes.PUFFERFISH, 5, 1, 3)).addSpawn(EnumCreatureType.WATER_AMBIENT, new BiomeSettingsMobs.c(EntityTypes.TROPICAL_FISH, 25, 8, 8)).addSpawn(EnumCreatureType.WATER_CREATURE, new BiomeSettingsMobs.c(EntityTypes.DOLPHIN, 2, 1, 2));
        BiomeSettingsGeneration.a biomesettingsgeneration_a = baseOceanGeneration(holdergetter, holdergetter1);

        biomesettingsgeneration_a.addFeature(WorldGenStage.Decoration.VEGETAL_DECORATION, flag ? AquaticPlacements.SEAGRASS_DEEP_WARM : AquaticPlacements.SEAGRASS_WARM);
        if (flag) {
            BiomeSettings.addDefaultSeagrass(biomesettingsgeneration_a);
        }

        BiomeSettings.addLukeWarmKelp(biomesettingsgeneration_a);
        return baseOcean(biomesettingsmobs_a, 4566514, 267827, biomesettingsgeneration_a);
    }

    public static BiomeBase warmOcean(HolderGetter<PlacedFeature> holdergetter, HolderGetter<WorldGenCarverWrapper<?>> holdergetter1) {
        BiomeSettingsMobs.a biomesettingsmobs_a = (new BiomeSettingsMobs.a()).addSpawn(EnumCreatureType.WATER_AMBIENT, new BiomeSettingsMobs.c(EntityTypes.PUFFERFISH, 15, 1, 3));

        BiomeSettings.warmOceanSpawns(biomesettingsmobs_a, 10, 4);
        BiomeSettingsGeneration.a biomesettingsgeneration_a = baseOceanGeneration(holdergetter, holdergetter1).addFeature(WorldGenStage.Decoration.VEGETAL_DECORATION, AquaticPlacements.WARM_OCEAN_VEGETATION).addFeature(WorldGenStage.Decoration.VEGETAL_DECORATION, AquaticPlacements.SEAGRASS_WARM).addFeature(WorldGenStage.Decoration.VEGETAL_DECORATION, AquaticPlacements.SEA_PICKLE);

        return baseOcean(biomesettingsmobs_a, 4445678, 270131, biomesettingsgeneration_a);
    }

    public static BiomeBase frozenOcean(HolderGetter<PlacedFeature> holdergetter, HolderGetter<WorldGenCarverWrapper<?>> holdergetter1, boolean flag) {
        BiomeSettingsMobs.a biomesettingsmobs_a = (new BiomeSettingsMobs.a()).addSpawn(EnumCreatureType.WATER_CREATURE, new BiomeSettingsMobs.c(EntityTypes.SQUID, 1, 1, 4)).addSpawn(EnumCreatureType.WATER_AMBIENT, new BiomeSettingsMobs.c(EntityTypes.SALMON, 15, 1, 5)).addSpawn(EnumCreatureType.CREATURE, new BiomeSettingsMobs.c(EntityTypes.POLAR_BEAR, 1, 1, 2));

        BiomeSettings.commonSpawns(biomesettingsmobs_a);
        biomesettingsmobs_a.addSpawn(EnumCreatureType.MONSTER, new BiomeSettingsMobs.c(EntityTypes.DROWNED, 5, 1, 1));
        float f = flag ? 0.5F : 0.0F;
        BiomeSettingsGeneration.a biomesettingsgeneration_a = new BiomeSettingsGeneration.a(holdergetter, holdergetter1);

        BiomeSettings.addIcebergs(biomesettingsgeneration_a);
        globalOverworldGeneration(biomesettingsgeneration_a);
        BiomeSettings.addBlueIce(biomesettingsgeneration_a);
        BiomeSettings.addDefaultOres(biomesettingsgeneration_a);
        BiomeSettings.addDefaultSoftDisks(biomesettingsgeneration_a);
        BiomeSettings.addWaterTrees(biomesettingsgeneration_a);
        BiomeSettings.addDefaultFlowers(biomesettingsgeneration_a);
        BiomeSettings.addDefaultGrass(biomesettingsgeneration_a);
        BiomeSettings.addDefaultMushrooms(biomesettingsgeneration_a);
        BiomeSettings.addDefaultExtraVegetation(biomesettingsgeneration_a);
        return (new BiomeBase.a()).hasPrecipitation(true).temperature(f).temperatureAdjustment(BiomeBase.TemperatureModifier.FROZEN).downfall(0.5F).specialEffects((new BiomeFog.a()).waterColor(3750089).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(f)).ambientMoodSound(CaveSoundSettings.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(biomesettingsmobs_a.build()).generationSettings(biomesettingsgeneration_a.build()).build();
    }

    public static BiomeBase forest(HolderGetter<PlacedFeature> holdergetter, HolderGetter<WorldGenCarverWrapper<?>> holdergetter1, boolean flag, boolean flag1, boolean flag2) {
        BiomeSettingsGeneration.a biomesettingsgeneration_a = new BiomeSettingsGeneration.a(holdergetter, holdergetter1);

        globalOverworldGeneration(biomesettingsgeneration_a);
        Music music;

        if (flag2) {
            music = Musics.createGameMusic(SoundEffects.MUSIC_BIOME_FLOWER_FOREST);
            biomesettingsgeneration_a.addFeature(WorldGenStage.Decoration.VEGETAL_DECORATION, VegetationPlacements.FLOWER_FOREST_FLOWERS);
        } else {
            music = Musics.createGameMusic(SoundEffects.MUSIC_BIOME_FOREST);
            BiomeSettings.addForestFlowers(biomesettingsgeneration_a);
        }

        BiomeSettings.addDefaultOres(biomesettingsgeneration_a);
        BiomeSettings.addDefaultSoftDisks(biomesettingsgeneration_a);
        if (flag2) {
            biomesettingsgeneration_a.addFeature(WorldGenStage.Decoration.VEGETAL_DECORATION, VegetationPlacements.TREES_FLOWER_FOREST);
            biomesettingsgeneration_a.addFeature(WorldGenStage.Decoration.VEGETAL_DECORATION, VegetationPlacements.FLOWER_FLOWER_FOREST);
            BiomeSettings.addDefaultGrass(biomesettingsgeneration_a);
        } else {
            if (flag) {
                if (flag1) {
                    BiomeSettings.addTallBirchTrees(biomesettingsgeneration_a);
                } else {
                    BiomeSettings.addBirchTrees(biomesettingsgeneration_a);
                }
            } else {
                BiomeSettings.addOtherBirchTrees(biomesettingsgeneration_a);
            }

            BiomeSettings.addDefaultFlowers(biomesettingsgeneration_a);
            BiomeSettings.addForestGrass(biomesettingsgeneration_a);
        }

        BiomeSettings.addDefaultMushrooms(biomesettingsgeneration_a);
        BiomeSettings.addDefaultExtraVegetation(biomesettingsgeneration_a);
        BiomeSettingsMobs.a biomesettingsmobs_a = new BiomeSettingsMobs.a();

        BiomeSettings.farmAnimals(biomesettingsmobs_a);
        BiomeSettings.commonSpawns(biomesettingsmobs_a);
        if (flag2) {
            biomesettingsmobs_a.addSpawn(EnumCreatureType.CREATURE, new BiomeSettingsMobs.c(EntityTypes.RABBIT, 4, 2, 3));
        } else if (!flag) {
            biomesettingsmobs_a.addSpawn(EnumCreatureType.CREATURE, new BiomeSettingsMobs.c(EntityTypes.WOLF, 5, 4, 4));
        }

        float f = flag ? 0.6F : 0.7F;

        return biome(true, f, flag ? 0.6F : 0.8F, biomesettingsmobs_a, biomesettingsgeneration_a, music);
    }

    public static BiomeBase taiga(HolderGetter<PlacedFeature> holdergetter, HolderGetter<WorldGenCarverWrapper<?>> holdergetter1, boolean flag) {
        BiomeSettingsMobs.a biomesettingsmobs_a = new BiomeSettingsMobs.a();

        BiomeSettings.farmAnimals(biomesettingsmobs_a);
        biomesettingsmobs_a.addSpawn(EnumCreatureType.CREATURE, new BiomeSettingsMobs.c(EntityTypes.WOLF, 8, 4, 4)).addSpawn(EnumCreatureType.CREATURE, new BiomeSettingsMobs.c(EntityTypes.RABBIT, 4, 2, 3)).addSpawn(EnumCreatureType.CREATURE, new BiomeSettingsMobs.c(EntityTypes.FOX, 8, 2, 4));
        BiomeSettings.commonSpawns(biomesettingsmobs_a);
        float f = flag ? -0.5F : 0.25F;
        BiomeSettingsGeneration.a biomesettingsgeneration_a = new BiomeSettingsGeneration.a(holdergetter, holdergetter1);

        globalOverworldGeneration(biomesettingsgeneration_a);
        BiomeSettings.addFerns(biomesettingsgeneration_a);
        BiomeSettings.addDefaultOres(biomesettingsgeneration_a);
        BiomeSettings.addDefaultSoftDisks(biomesettingsgeneration_a);
        BiomeSettings.addTaigaTrees(biomesettingsgeneration_a);
        BiomeSettings.addDefaultFlowers(biomesettingsgeneration_a);
        BiomeSettings.addTaigaGrass(biomesettingsgeneration_a);
        BiomeSettings.addDefaultExtraVegetation(biomesettingsgeneration_a);
        if (flag) {
            BiomeSettings.addRareBerryBushes(biomesettingsgeneration_a);
        } else {
            BiomeSettings.addCommonBerryBushes(biomesettingsgeneration_a);
        }

        return biome(true, f, flag ? 0.4F : 0.8F, flag ? 4020182 : 4159204, 329011, (Integer) null, (Integer) null, biomesettingsmobs_a, biomesettingsgeneration_a, OverworldBiomes.NORMAL_MUSIC);
    }

    public static BiomeBase darkForest(HolderGetter<PlacedFeature> holdergetter, HolderGetter<WorldGenCarverWrapper<?>> holdergetter1) {
        BiomeSettingsMobs.a biomesettingsmobs_a = new BiomeSettingsMobs.a();

        BiomeSettings.farmAnimals(biomesettingsmobs_a);
        BiomeSettings.commonSpawns(biomesettingsmobs_a);
        BiomeSettingsGeneration.a biomesettingsgeneration_a = new BiomeSettingsGeneration.a(holdergetter, holdergetter1);

        globalOverworldGeneration(biomesettingsgeneration_a);
        biomesettingsgeneration_a.addFeature(WorldGenStage.Decoration.VEGETAL_DECORATION, VegetationPlacements.DARK_FOREST_VEGETATION);
        BiomeSettings.addForestFlowers(biomesettingsgeneration_a);
        BiomeSettings.addDefaultOres(biomesettingsgeneration_a);
        BiomeSettings.addDefaultSoftDisks(biomesettingsgeneration_a);
        BiomeSettings.addDefaultFlowers(biomesettingsgeneration_a);
        BiomeSettings.addForestGrass(biomesettingsgeneration_a);
        BiomeSettings.addDefaultMushrooms(biomesettingsgeneration_a);
        BiomeSettings.addDefaultExtraVegetation(biomesettingsgeneration_a);
        Music music = Musics.createGameMusic(SoundEffects.MUSIC_BIOME_FOREST);

        return (new BiomeBase.a()).hasPrecipitation(true).temperature(0.7F).downfall(0.8F).specialEffects((new BiomeFog.a()).waterColor(4159204).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(0.7F)).grassColorModifier(BiomeFog.GrassColor.DARK_FOREST).ambientMoodSound(CaveSoundSettings.LEGACY_CAVE_SETTINGS).backgroundMusic(music).build()).mobSpawnSettings(biomesettingsmobs_a.build()).generationSettings(biomesettingsgeneration_a.build()).build();
    }

    public static BiomeBase swamp(HolderGetter<PlacedFeature> holdergetter, HolderGetter<WorldGenCarverWrapper<?>> holdergetter1, Consumer<BiomeSettingsMobs.a> consumer) {
        BiomeSettingsMobs.a biomesettingsmobs_a = new BiomeSettingsMobs.a();

        BiomeSettings.farmAnimals(biomesettingsmobs_a);
        BiomeSettings.commonSpawns(biomesettingsmobs_a);
        biomesettingsmobs_a.addSpawn(EnumCreatureType.MONSTER, new BiomeSettingsMobs.c(EntityTypes.SLIME, 1, 1, 1));
        biomesettingsmobs_a.addSpawn(EnumCreatureType.CREATURE, new BiomeSettingsMobs.c(EntityTypes.FROG, 10, 2, 5));
        consumer.accept(biomesettingsmobs_a);
        BiomeSettingsGeneration.a biomesettingsgeneration_a = new BiomeSettingsGeneration.a(holdergetter, holdergetter1);

        BiomeSettings.addFossilDecoration(biomesettingsgeneration_a);
        globalOverworldGeneration(biomesettingsgeneration_a);
        BiomeSettings.addDefaultOres(biomesettingsgeneration_a);
        BiomeSettings.addSwampClayDisk(biomesettingsgeneration_a);
        BiomeSettings.addSwampVegetation(biomesettingsgeneration_a);
        BiomeSettings.addDefaultMushrooms(biomesettingsgeneration_a);
        BiomeSettings.addSwampExtraVegetation(biomesettingsgeneration_a);
        biomesettingsgeneration_a.addFeature(WorldGenStage.Decoration.VEGETAL_DECORATION, AquaticPlacements.SEAGRASS_SWAMP);
        Music music = Musics.createGameMusic(SoundEffects.MUSIC_BIOME_SWAMP);

        return (new BiomeBase.a()).hasPrecipitation(true).temperature(0.8F).downfall(0.9F).specialEffects((new BiomeFog.a()).waterColor(6388580).waterFogColor(2302743).fogColor(12638463).skyColor(calculateSkyColor(0.8F)).foliageColorOverride(6975545).grassColorModifier(BiomeFog.GrassColor.SWAMP).ambientMoodSound(CaveSoundSettings.LEGACY_CAVE_SETTINGS).backgroundMusic(music).build()).mobSpawnSettings(biomesettingsmobs_a.build()).generationSettings(biomesettingsgeneration_a.build()).build();
    }

    public static BiomeBase mangroveSwamp(HolderGetter<PlacedFeature> holdergetter, HolderGetter<WorldGenCarverWrapper<?>> holdergetter1, Consumer<BiomeSettingsMobs.a> consumer) {
        BiomeSettingsMobs.a biomesettingsmobs_a = new BiomeSettingsMobs.a();

        BiomeSettings.commonSpawns(biomesettingsmobs_a);
        biomesettingsmobs_a.addSpawn(EnumCreatureType.MONSTER, new BiomeSettingsMobs.c(EntityTypes.SLIME, 1, 1, 1));
        biomesettingsmobs_a.addSpawn(EnumCreatureType.CREATURE, new BiomeSettingsMobs.c(EntityTypes.FROG, 10, 2, 5));
        biomesettingsmobs_a.addSpawn(EnumCreatureType.WATER_AMBIENT, new BiomeSettingsMobs.c(EntityTypes.TROPICAL_FISH, 25, 8, 8));
        consumer.accept(biomesettingsmobs_a);
        BiomeSettingsGeneration.a biomesettingsgeneration_a = new BiomeSettingsGeneration.a(holdergetter, holdergetter1);

        BiomeSettings.addFossilDecoration(biomesettingsgeneration_a);
        globalOverworldGeneration(biomesettingsgeneration_a);
        BiomeSettings.addDefaultOres(biomesettingsgeneration_a);
        BiomeSettings.addMangroveSwampDisks(biomesettingsgeneration_a);
        BiomeSettings.addMangroveSwampVegetation(biomesettingsgeneration_a);
        biomesettingsgeneration_a.addFeature(WorldGenStage.Decoration.VEGETAL_DECORATION, AquaticPlacements.SEAGRASS_SWAMP);
        Music music = Musics.createGameMusic(SoundEffects.MUSIC_BIOME_SWAMP);

        return (new BiomeBase.a()).hasPrecipitation(true).temperature(0.8F).downfall(0.9F).specialEffects((new BiomeFog.a()).waterColor(3832426).waterFogColor(5077600).fogColor(12638463).skyColor(calculateSkyColor(0.8F)).foliageColorOverride(9285927).grassColorModifier(BiomeFog.GrassColor.SWAMP).ambientMoodSound(CaveSoundSettings.LEGACY_CAVE_SETTINGS).backgroundMusic(music).build()).mobSpawnSettings(biomesettingsmobs_a.build()).generationSettings(biomesettingsgeneration_a.build()).build();
    }

    public static BiomeBase river(HolderGetter<PlacedFeature> holdergetter, HolderGetter<WorldGenCarverWrapper<?>> holdergetter1, boolean flag) {
        BiomeSettingsMobs.a biomesettingsmobs_a = (new BiomeSettingsMobs.a()).addSpawn(EnumCreatureType.WATER_CREATURE, new BiomeSettingsMobs.c(EntityTypes.SQUID, 2, 1, 4)).addSpawn(EnumCreatureType.WATER_AMBIENT, new BiomeSettingsMobs.c(EntityTypes.SALMON, 5, 1, 5));

        BiomeSettings.commonSpawns(biomesettingsmobs_a);
        biomesettingsmobs_a.addSpawn(EnumCreatureType.MONSTER, new BiomeSettingsMobs.c(EntityTypes.DROWNED, flag ? 1 : 100, 1, 1));
        BiomeSettingsGeneration.a biomesettingsgeneration_a = new BiomeSettingsGeneration.a(holdergetter, holdergetter1);

        globalOverworldGeneration(biomesettingsgeneration_a);
        BiomeSettings.addDefaultOres(biomesettingsgeneration_a);
        BiomeSettings.addDefaultSoftDisks(biomesettingsgeneration_a);
        BiomeSettings.addWaterTrees(biomesettingsgeneration_a);
        BiomeSettings.addDefaultFlowers(biomesettingsgeneration_a);
        BiomeSettings.addDefaultGrass(biomesettingsgeneration_a);
        BiomeSettings.addDefaultMushrooms(biomesettingsgeneration_a);
        BiomeSettings.addDefaultExtraVegetation(biomesettingsgeneration_a);
        if (!flag) {
            biomesettingsgeneration_a.addFeature(WorldGenStage.Decoration.VEGETAL_DECORATION, AquaticPlacements.SEAGRASS_RIVER);
        }

        float f = flag ? 0.0F : 0.5F;

        return biome(true, f, 0.5F, flag ? 3750089 : 4159204, 329011, (Integer) null, (Integer) null, biomesettingsmobs_a, biomesettingsgeneration_a, OverworldBiomes.NORMAL_MUSIC);
    }

    public static BiomeBase beach(HolderGetter<PlacedFeature> holdergetter, HolderGetter<WorldGenCarverWrapper<?>> holdergetter1, boolean flag, boolean flag1) {
        BiomeSettingsMobs.a biomesettingsmobs_a = new BiomeSettingsMobs.a();
        boolean flag2 = !flag1 && !flag;

        if (flag2) {
            biomesettingsmobs_a.addSpawn(EnumCreatureType.CREATURE, new BiomeSettingsMobs.c(EntityTypes.TURTLE, 5, 2, 5));
        }

        BiomeSettings.commonSpawns(biomesettingsmobs_a);
        BiomeSettingsGeneration.a biomesettingsgeneration_a = new BiomeSettingsGeneration.a(holdergetter, holdergetter1);

        globalOverworldGeneration(biomesettingsgeneration_a);
        BiomeSettings.addDefaultOres(biomesettingsgeneration_a);
        BiomeSettings.addDefaultSoftDisks(biomesettingsgeneration_a);
        BiomeSettings.addDefaultFlowers(biomesettingsgeneration_a);
        BiomeSettings.addDefaultGrass(biomesettingsgeneration_a);
        BiomeSettings.addDefaultMushrooms(biomesettingsgeneration_a);
        BiomeSettings.addDefaultExtraVegetation(biomesettingsgeneration_a);
        float f;

        if (flag) {
            f = 0.05F;
        } else if (flag1) {
            f = 0.2F;
        } else {
            f = 0.8F;
        }

        return biome(true, f, flag2 ? 0.4F : 0.3F, flag ? 4020182 : 4159204, 329011, (Integer) null, (Integer) null, biomesettingsmobs_a, biomesettingsgeneration_a, OverworldBiomes.NORMAL_MUSIC);
    }

    public static BiomeBase theVoid(HolderGetter<PlacedFeature> holdergetter, HolderGetter<WorldGenCarverWrapper<?>> holdergetter1) {
        BiomeSettingsGeneration.a biomesettingsgeneration_a = new BiomeSettingsGeneration.a(holdergetter, holdergetter1);

        biomesettingsgeneration_a.addFeature(WorldGenStage.Decoration.TOP_LAYER_MODIFICATION, MiscOverworldPlacements.VOID_START_PLATFORM);
        return biome(false, 0.5F, 0.5F, new BiomeSettingsMobs.a(), biomesettingsgeneration_a, OverworldBiomes.NORMAL_MUSIC);
    }

    public static BiomeBase meadowOrCherryGrove(HolderGetter<PlacedFeature> holdergetter, HolderGetter<WorldGenCarverWrapper<?>> holdergetter1, boolean flag) {
        BiomeSettingsGeneration.a biomesettingsgeneration_a = new BiomeSettingsGeneration.a(holdergetter, holdergetter1);
        BiomeSettingsMobs.a biomesettingsmobs_a = new BiomeSettingsMobs.a();

        biomesettingsmobs_a.addSpawn(EnumCreatureType.CREATURE, new BiomeSettingsMobs.c(flag ? EntityTypes.PIG : EntityTypes.DONKEY, 1, 1, 2)).addSpawn(EnumCreatureType.CREATURE, new BiomeSettingsMobs.c(EntityTypes.RABBIT, 2, 2, 6)).addSpawn(EnumCreatureType.CREATURE, new BiomeSettingsMobs.c(EntityTypes.SHEEP, 2, 2, 4));
        BiomeSettings.commonSpawns(biomesettingsmobs_a);
        globalOverworldGeneration(biomesettingsgeneration_a);
        BiomeSettings.addPlainGrass(biomesettingsgeneration_a);
        BiomeSettings.addDefaultOres(biomesettingsgeneration_a);
        BiomeSettings.addDefaultSoftDisks(biomesettingsgeneration_a);
        if (flag) {
            BiomeSettings.addCherryGroveVegetation(biomesettingsgeneration_a);
        } else {
            BiomeSettings.addMeadowVegetation(biomesettingsgeneration_a);
        }

        BiomeSettings.addExtraEmeralds(biomesettingsgeneration_a);
        BiomeSettings.addInfestedStone(biomesettingsgeneration_a);
        Music music = Musics.createGameMusic(flag ? SoundEffects.MUSIC_BIOME_CHERRY_GROVE : SoundEffects.MUSIC_BIOME_MEADOW);

        return flag ? biome(true, 0.5F, 0.8F, 6141935, 6141935, 11983713, 11983713, biomesettingsmobs_a, biomesettingsgeneration_a, music) : biome(true, 0.5F, 0.8F, 937679, 329011, (Integer) null, (Integer) null, biomesettingsmobs_a, biomesettingsgeneration_a, music);
    }

    public static BiomeBase frozenPeaks(HolderGetter<PlacedFeature> holdergetter, HolderGetter<WorldGenCarverWrapper<?>> holdergetter1) {
        BiomeSettingsGeneration.a biomesettingsgeneration_a = new BiomeSettingsGeneration.a(holdergetter, holdergetter1);
        BiomeSettingsMobs.a biomesettingsmobs_a = new BiomeSettingsMobs.a();

        biomesettingsmobs_a.addSpawn(EnumCreatureType.CREATURE, new BiomeSettingsMobs.c(EntityTypes.GOAT, 5, 1, 3));
        BiomeSettings.commonSpawns(biomesettingsmobs_a);
        globalOverworldGeneration(biomesettingsgeneration_a);
        BiomeSettings.addFrozenSprings(biomesettingsgeneration_a);
        BiomeSettings.addDefaultOres(biomesettingsgeneration_a);
        BiomeSettings.addDefaultSoftDisks(biomesettingsgeneration_a);
        BiomeSettings.addExtraEmeralds(biomesettingsgeneration_a);
        BiomeSettings.addInfestedStone(biomesettingsgeneration_a);
        Music music = Musics.createGameMusic(SoundEffects.MUSIC_BIOME_FROZEN_PEAKS);

        return biome(true, -0.7F, 0.9F, biomesettingsmobs_a, biomesettingsgeneration_a, music);
    }

    public static BiomeBase jaggedPeaks(HolderGetter<PlacedFeature> holdergetter, HolderGetter<WorldGenCarverWrapper<?>> holdergetter1) {
        BiomeSettingsGeneration.a biomesettingsgeneration_a = new BiomeSettingsGeneration.a(holdergetter, holdergetter1);
        BiomeSettingsMobs.a biomesettingsmobs_a = new BiomeSettingsMobs.a();

        biomesettingsmobs_a.addSpawn(EnumCreatureType.CREATURE, new BiomeSettingsMobs.c(EntityTypes.GOAT, 5, 1, 3));
        BiomeSettings.commonSpawns(biomesettingsmobs_a);
        globalOverworldGeneration(biomesettingsgeneration_a);
        BiomeSettings.addFrozenSprings(biomesettingsgeneration_a);
        BiomeSettings.addDefaultOres(biomesettingsgeneration_a);
        BiomeSettings.addDefaultSoftDisks(biomesettingsgeneration_a);
        BiomeSettings.addExtraEmeralds(biomesettingsgeneration_a);
        BiomeSettings.addInfestedStone(biomesettingsgeneration_a);
        Music music = Musics.createGameMusic(SoundEffects.MUSIC_BIOME_JAGGED_PEAKS);

        return biome(true, -0.7F, 0.9F, biomesettingsmobs_a, biomesettingsgeneration_a, music);
    }

    public static BiomeBase stonyPeaks(HolderGetter<PlacedFeature> holdergetter, HolderGetter<WorldGenCarverWrapper<?>> holdergetter1) {
        BiomeSettingsGeneration.a biomesettingsgeneration_a = new BiomeSettingsGeneration.a(holdergetter, holdergetter1);
        BiomeSettingsMobs.a biomesettingsmobs_a = new BiomeSettingsMobs.a();

        BiomeSettings.commonSpawns(biomesettingsmobs_a);
        globalOverworldGeneration(biomesettingsgeneration_a);
        BiomeSettings.addDefaultOres(biomesettingsgeneration_a);
        BiomeSettings.addDefaultSoftDisks(biomesettingsgeneration_a);
        BiomeSettings.addExtraEmeralds(biomesettingsgeneration_a);
        BiomeSettings.addInfestedStone(biomesettingsgeneration_a);
        Music music = Musics.createGameMusic(SoundEffects.MUSIC_BIOME_STONY_PEAKS);

        return biome(true, 1.0F, 0.3F, biomesettingsmobs_a, biomesettingsgeneration_a, music);
    }

    public static BiomeBase snowySlopes(HolderGetter<PlacedFeature> holdergetter, HolderGetter<WorldGenCarverWrapper<?>> holdergetter1) {
        BiomeSettingsGeneration.a biomesettingsgeneration_a = new BiomeSettingsGeneration.a(holdergetter, holdergetter1);
        BiomeSettingsMobs.a biomesettingsmobs_a = new BiomeSettingsMobs.a();

        biomesettingsmobs_a.addSpawn(EnumCreatureType.CREATURE, new BiomeSettingsMobs.c(EntityTypes.RABBIT, 4, 2, 3)).addSpawn(EnumCreatureType.CREATURE, new BiomeSettingsMobs.c(EntityTypes.GOAT, 5, 1, 3));
        BiomeSettings.commonSpawns(biomesettingsmobs_a);
        globalOverworldGeneration(biomesettingsgeneration_a);
        BiomeSettings.addFrozenSprings(biomesettingsgeneration_a);
        BiomeSettings.addDefaultOres(biomesettingsgeneration_a);
        BiomeSettings.addDefaultSoftDisks(biomesettingsgeneration_a);
        BiomeSettings.addDefaultExtraVegetation(biomesettingsgeneration_a);
        BiomeSettings.addExtraEmeralds(biomesettingsgeneration_a);
        BiomeSettings.addInfestedStone(biomesettingsgeneration_a);
        Music music = Musics.createGameMusic(SoundEffects.MUSIC_BIOME_SNOWY_SLOPES);

        return biome(true, -0.3F, 0.9F, biomesettingsmobs_a, biomesettingsgeneration_a, music);
    }

    public static BiomeBase grove(HolderGetter<PlacedFeature> holdergetter, HolderGetter<WorldGenCarverWrapper<?>> holdergetter1) {
        BiomeSettingsGeneration.a biomesettingsgeneration_a = new BiomeSettingsGeneration.a(holdergetter, holdergetter1);
        BiomeSettingsMobs.a biomesettingsmobs_a = new BiomeSettingsMobs.a();

        biomesettingsmobs_a.addSpawn(EnumCreatureType.CREATURE, new BiomeSettingsMobs.c(EntityTypes.WOLF, 1, 1, 1)).addSpawn(EnumCreatureType.CREATURE, new BiomeSettingsMobs.c(EntityTypes.RABBIT, 8, 2, 3)).addSpawn(EnumCreatureType.CREATURE, new BiomeSettingsMobs.c(EntityTypes.FOX, 4, 2, 4));
        BiomeSettings.commonSpawns(biomesettingsmobs_a);
        globalOverworldGeneration(biomesettingsgeneration_a);
        BiomeSettings.addFrozenSprings(biomesettingsgeneration_a);
        BiomeSettings.addDefaultOres(biomesettingsgeneration_a);
        BiomeSettings.addDefaultSoftDisks(biomesettingsgeneration_a);
        BiomeSettings.addGroveTrees(biomesettingsgeneration_a);
        BiomeSettings.addDefaultExtraVegetation(biomesettingsgeneration_a);
        BiomeSettings.addExtraEmeralds(biomesettingsgeneration_a);
        BiomeSettings.addInfestedStone(biomesettingsgeneration_a);
        Music music = Musics.createGameMusic(SoundEffects.MUSIC_BIOME_GROVE);

        return biome(true, -0.2F, 0.8F, biomesettingsmobs_a, biomesettingsgeneration_a, music);
    }

    public static BiomeBase lushCaves(HolderGetter<PlacedFeature> holdergetter, HolderGetter<WorldGenCarverWrapper<?>> holdergetter1) {
        BiomeSettingsMobs.a biomesettingsmobs_a = new BiomeSettingsMobs.a();

        biomesettingsmobs_a.addSpawn(EnumCreatureType.AXOLOTLS, new BiomeSettingsMobs.c(EntityTypes.AXOLOTL, 10, 4, 6));
        biomesettingsmobs_a.addSpawn(EnumCreatureType.WATER_AMBIENT, new BiomeSettingsMobs.c(EntityTypes.TROPICAL_FISH, 25, 8, 8));
        BiomeSettings.commonSpawns(biomesettingsmobs_a);
        BiomeSettingsGeneration.a biomesettingsgeneration_a = new BiomeSettingsGeneration.a(holdergetter, holdergetter1);

        globalOverworldGeneration(biomesettingsgeneration_a);
        BiomeSettings.addPlainGrass(biomesettingsgeneration_a);
        BiomeSettings.addDefaultOres(biomesettingsgeneration_a);
        BiomeSettings.addLushCavesSpecialOres(biomesettingsgeneration_a);
        BiomeSettings.addDefaultSoftDisks(biomesettingsgeneration_a);
        BiomeSettings.addLushCavesVegetationFeatures(biomesettingsgeneration_a);
        Music music = Musics.createGameMusic(SoundEffects.MUSIC_BIOME_LUSH_CAVES);

        return biome(true, 0.5F, 0.5F, biomesettingsmobs_a, biomesettingsgeneration_a, music);
    }

    public static BiomeBase dripstoneCaves(HolderGetter<PlacedFeature> holdergetter, HolderGetter<WorldGenCarverWrapper<?>> holdergetter1) {
        BiomeSettingsMobs.a biomesettingsmobs_a = new BiomeSettingsMobs.a();

        BiomeSettings.dripstoneCavesSpawns(biomesettingsmobs_a);
        BiomeSettingsGeneration.a biomesettingsgeneration_a = new BiomeSettingsGeneration.a(holdergetter, holdergetter1);

        globalOverworldGeneration(biomesettingsgeneration_a);
        BiomeSettings.addPlainGrass(biomesettingsgeneration_a);
        BiomeSettings.addDefaultOres(biomesettingsgeneration_a, true);
        BiomeSettings.addDefaultSoftDisks(biomesettingsgeneration_a);
        BiomeSettings.addPlainVegetation(biomesettingsgeneration_a);
        BiomeSettings.addDefaultMushrooms(biomesettingsgeneration_a);
        BiomeSettings.addDefaultExtraVegetation(biomesettingsgeneration_a);
        BiomeSettings.addDripstone(biomesettingsgeneration_a);
        Music music = Musics.createGameMusic(SoundEffects.MUSIC_BIOME_DRIPSTONE_CAVES);

        return biome(true, 0.8F, 0.4F, biomesettingsmobs_a, biomesettingsgeneration_a, music);
    }

    public static BiomeBase deepDark(HolderGetter<PlacedFeature> holdergetter, HolderGetter<WorldGenCarverWrapper<?>> holdergetter1) {
        BiomeSettingsMobs.a biomesettingsmobs_a = new BiomeSettingsMobs.a();
        BiomeSettingsGeneration.a biomesettingsgeneration_a = new BiomeSettingsGeneration.a(holdergetter, holdergetter1);

        biomesettingsgeneration_a.addCarver(WorldGenStage.Features.AIR, WorldGenCarvers.CAVE);
        biomesettingsgeneration_a.addCarver(WorldGenStage.Features.AIR, WorldGenCarvers.CAVE_EXTRA_UNDERGROUND);
        biomesettingsgeneration_a.addCarver(WorldGenStage.Features.AIR, WorldGenCarvers.CANYON);
        BiomeSettings.addDefaultCrystalFormations(biomesettingsgeneration_a);
        BiomeSettings.addDefaultMonsterRoom(biomesettingsgeneration_a);
        BiomeSettings.addDefaultUndergroundVariety(biomesettingsgeneration_a);
        BiomeSettings.addSurfaceFreezing(biomesettingsgeneration_a);
        BiomeSettings.addPlainGrass(biomesettingsgeneration_a);
        BiomeSettings.addDefaultOres(biomesettingsgeneration_a);
        BiomeSettings.addDefaultSoftDisks(biomesettingsgeneration_a);
        BiomeSettings.addPlainVegetation(biomesettingsgeneration_a);
        BiomeSettings.addDefaultMushrooms(biomesettingsgeneration_a);
        BiomeSettings.addDefaultExtraVegetation(biomesettingsgeneration_a);
        BiomeSettings.addSculk(biomesettingsgeneration_a);
        Music music = Musics.createGameMusic(SoundEffects.MUSIC_BIOME_DEEP_DARK);

        return biome(true, 0.8F, 0.4F, biomesettingsmobs_a, biomesettingsgeneration_a, music);
    }
}
