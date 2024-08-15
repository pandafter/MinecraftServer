package net.minecraft.world.level.levelgen;

import java.util.Iterator;
import net.minecraft.core.BlockPosition;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.stats.ServerStatisticManager;
import net.minecraft.stats.StatisticList;
import net.minecraft.util.MathHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyDamageScaler;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EnumMobSpawn;
import net.minecraft.world.entity.GroupDataEntity;
import net.minecraft.world.entity.monster.EntityPhantom;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.MobSpawner;
import net.minecraft.world.level.SpawnerCreature;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.material.Fluid;

public class MobSpawnerPhantom implements MobSpawner {

    private int nextTick;

    public MobSpawnerPhantom() {}

    @Override
    public int tick(WorldServer worldserver, boolean flag, boolean flag1) {
        if (!flag) {
            return 0;
        } else if (!worldserver.getGameRules().getBoolean(GameRules.RULE_DOINSOMNIA)) {
            return 0;
        } else {
            RandomSource randomsource = worldserver.random;

            --this.nextTick;
            if (this.nextTick > 0) {
                return 0;
            } else {
                this.nextTick += (60 + randomsource.nextInt(60)) * 20;
                if (worldserver.getSkyDarken() < 5 && worldserver.dimensionType().hasSkyLight()) {
                    return 0;
                } else {
                    int i = 0;
                    Iterator iterator = worldserver.players().iterator();

                    while (iterator.hasNext()) {
                        EntityPlayer entityplayer = (EntityPlayer) iterator.next();

                        if (!entityplayer.isSpectator()) {
                            BlockPosition blockposition = entityplayer.blockPosition();

                            if (!worldserver.dimensionType().hasSkyLight() || blockposition.getY() >= worldserver.getSeaLevel() && worldserver.canSeeSky(blockposition)) {
                                DifficultyDamageScaler difficultydamagescaler = worldserver.getCurrentDifficultyAt(blockposition);

                                if (difficultydamagescaler.isHarderThan(randomsource.nextFloat() * 3.0F)) {
                                    ServerStatisticManager serverstatisticmanager = entityplayer.getStats();
                                    int j = MathHelper.clamp(serverstatisticmanager.getValue(StatisticList.CUSTOM.get(StatisticList.TIME_SINCE_REST)), 1, Integer.MAX_VALUE);
                                    boolean flag2 = true;

                                    if (randomsource.nextInt(j) >= 72000) {
                                        BlockPosition blockposition1 = blockposition.above(20 + randomsource.nextInt(15)).east(-10 + randomsource.nextInt(21)).south(-10 + randomsource.nextInt(21));
                                        IBlockData iblockdata = worldserver.getBlockState(blockposition1);
                                        Fluid fluid = worldserver.getFluidState(blockposition1);

                                        if (SpawnerCreature.isValidEmptySpawnBlock(worldserver, blockposition1, iblockdata, fluid, EntityTypes.PHANTOM)) {
                                            GroupDataEntity groupdataentity = null;
                                            int k = 1 + randomsource.nextInt(difficultydamagescaler.getDifficulty().getId() + 1);

                                            for (int l = 0; l < k; ++l) {
                                                EntityPhantom entityphantom = (EntityPhantom) EntityTypes.PHANTOM.create(worldserver);

                                                if (entityphantom != null) {
                                                    entityphantom.moveTo(blockposition1, 0.0F, 0.0F);
                                                    groupdataentity = entityphantom.finalizeSpawn(worldserver, difficultydamagescaler, EnumMobSpawn.NATURAL, groupdataentity);
                                                    worldserver.addFreshEntityWithPassengers(entityphantom);
                                                    ++i;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    return i;
                }
            }
        }
    }
}
