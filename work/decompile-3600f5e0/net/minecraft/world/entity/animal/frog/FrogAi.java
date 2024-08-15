package net.minecraft.world.entity.animal.frog;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.function.Predicate;
import net.minecraft.core.BlockPosition;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.tags.TagsBlock;
import net.minecraft.tags.TagsItem;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.BehaviorController;
import net.minecraft.world.entity.ai.behavior.AnimalPanic;
import net.minecraft.world.entity.ai.behavior.BehaviorAttackTargetForget;
import net.minecraft.world.entity.ai.behavior.BehaviorAttackTargetSet;
import net.minecraft.world.entity.ai.behavior.BehaviorGate;
import net.minecraft.world.entity.ai.behavior.BehaviorGateSingle;
import net.minecraft.world.entity.ai.behavior.BehaviorLook;
import net.minecraft.world.entity.ai.behavior.BehaviorLookWalk;
import net.minecraft.world.entity.ai.behavior.BehaviorMakeLoveAnimal;
import net.minecraft.world.entity.ai.behavior.BehaviorStrollRandomUnconstrained;
import net.minecraft.world.entity.ai.behavior.BehaviorUtil;
import net.minecraft.world.entity.ai.behavior.BehavorMove;
import net.minecraft.world.entity.ai.behavior.CountDownCooldownTicks;
import net.minecraft.world.entity.ai.behavior.Croak;
import net.minecraft.world.entity.ai.behavior.FollowTemptation;
import net.minecraft.world.entity.ai.behavior.LongJumpMidJump;
import net.minecraft.world.entity.ai.behavior.LongJumpToPreferredBlock;
import net.minecraft.world.entity.ai.behavior.LongJumpToRandomPos;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTargetSometimes;
import net.minecraft.world.entity.ai.behavior.TryFindLand;
import net.minecraft.world.entity.ai.behavior.TryFindLandNearWater;
import net.minecraft.world.entity.ai.behavior.TryLaySpawnOnWaterNearLand;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathfinderNormal;
import net.minecraft.world.level.pathfinder.PathfindingContext;

public class FrogAi {

    private static final float SPEED_MULTIPLIER_WHEN_PANICKING = 2.0F;
    private static final float SPEED_MULTIPLIER_WHEN_IDLING = 1.0F;
    private static final float SPEED_MULTIPLIER_ON_LAND = 1.0F;
    private static final float SPEED_MULTIPLIER_IN_WATER = 0.75F;
    private static final UniformInt TIME_BETWEEN_LONG_JUMPS = UniformInt.of(100, 140);
    private static final int MAX_LONG_JUMP_HEIGHT = 2;
    private static final int MAX_LONG_JUMP_WIDTH = 4;
    private static final float MAX_JUMP_VELOCITY_MULTIPLIER = 3.5714288F;
    private static final float SPEED_MULTIPLIER_WHEN_TEMPTED = 1.25F;

    public FrogAi() {}

    protected static void initMemories(Frog frog, RandomSource randomsource) {
        frog.getBrain().setMemory(MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS, (Object) FrogAi.TIME_BETWEEN_LONG_JUMPS.sample(randomsource));
    }

    protected static BehaviorController<?> makeBrain(BehaviorController<Frog> behaviorcontroller) {
        initCoreActivity(behaviorcontroller);
        initIdleActivity(behaviorcontroller);
        initSwimActivity(behaviorcontroller);
        initLaySpawnActivity(behaviorcontroller);
        initTongueActivity(behaviorcontroller);
        initJumpActivity(behaviorcontroller);
        behaviorcontroller.setCoreActivities(ImmutableSet.of(Activity.CORE));
        behaviorcontroller.setDefaultActivity(Activity.IDLE);
        behaviorcontroller.useDefaultActivity();
        return behaviorcontroller;
    }

    private static void initCoreActivity(BehaviorController<Frog> behaviorcontroller) {
        behaviorcontroller.addActivity(Activity.CORE, 0, ImmutableList.of(new AnimalPanic<>(2.0F), new BehaviorLook(45, 90), new BehavorMove(), new CountDownCooldownTicks(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS), new CountDownCooldownTicks(MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS)));
    }

    private static void initIdleActivity(BehaviorController<Frog> behaviorcontroller) {
        behaviorcontroller.addActivityWithConditions(Activity.IDLE, ImmutableList.of(Pair.of(0, SetEntityLookTargetSometimes.create(EntityTypes.PLAYER, 6.0F, UniformInt.of(30, 60))), Pair.of(0, new BehaviorMakeLoveAnimal(EntityTypes.FROG)), Pair.of(1, new FollowTemptation((entityliving) -> {
            return 1.25F;
        })), Pair.of(2, BehaviorAttackTargetSet.create(FrogAi::canAttack, (frog) -> {
            return frog.getBrain().getMemory(MemoryModuleType.NEAREST_ATTACKABLE);
        })), Pair.of(3, TryFindLand.create(6, 1.0F)), Pair.of(4, new BehaviorGateSingle<>(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT), ImmutableList.of(Pair.of(BehaviorStrollRandomUnconstrained.stroll(1.0F), 1), Pair.of(BehaviorLookWalk.create(1.0F, 3), 1), Pair.of(new Croak(), 3), Pair.of(BehaviorBuilder.triggerIf(Entity::onGround), 2))))), ImmutableSet.of(Pair.of(MemoryModuleType.LONG_JUMP_MID_JUMP, MemoryStatus.VALUE_ABSENT), Pair.of(MemoryModuleType.IS_IN_WATER, MemoryStatus.VALUE_ABSENT)));
    }

    private static void initSwimActivity(BehaviorController<Frog> behaviorcontroller) {
        behaviorcontroller.addActivityWithConditions(Activity.SWIM, ImmutableList.of(Pair.of(0, SetEntityLookTargetSometimes.create(EntityTypes.PLAYER, 6.0F, UniformInt.of(30, 60))), Pair.of(1, new FollowTemptation((entityliving) -> {
            return 1.25F;
        })), Pair.of(2, BehaviorAttackTargetSet.create(FrogAi::canAttack, (frog) -> {
            return frog.getBrain().getMemory(MemoryModuleType.NEAREST_ATTACKABLE);
        })), Pair.of(3, TryFindLand.create(8, 1.5F)), Pair.of(5, new BehaviorGate<>(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT), ImmutableSet.of(), BehaviorGate.Order.ORDERED, BehaviorGate.Execution.TRY_ALL, ImmutableList.of(Pair.of(BehaviorStrollRandomUnconstrained.swim(0.75F), 1), Pair.of(BehaviorStrollRandomUnconstrained.stroll(1.0F, true), 1), Pair.of(BehaviorLookWalk.create(1.0F, 3), 1), Pair.of(BehaviorBuilder.triggerIf(Entity::isInWaterOrBubble), 5))))), ImmutableSet.of(Pair.of(MemoryModuleType.LONG_JUMP_MID_JUMP, MemoryStatus.VALUE_ABSENT), Pair.of(MemoryModuleType.IS_IN_WATER, MemoryStatus.VALUE_PRESENT)));
    }

    private static void initLaySpawnActivity(BehaviorController<Frog> behaviorcontroller) {
        behaviorcontroller.addActivityWithConditions(Activity.LAY_SPAWN, ImmutableList.of(Pair.of(0, SetEntityLookTargetSometimes.create(EntityTypes.PLAYER, 6.0F, UniformInt.of(30, 60))), Pair.of(1, BehaviorAttackTargetSet.create(FrogAi::canAttack, (frog) -> {
            return frog.getBrain().getMemory(MemoryModuleType.NEAREST_ATTACKABLE);
        })), Pair.of(2, TryFindLandNearWater.create(8, 1.0F)), Pair.of(3, TryLaySpawnOnWaterNearLand.create(Blocks.FROGSPAWN)), Pair.of(4, new BehaviorGateSingle<>(ImmutableList.of(Pair.of(BehaviorStrollRandomUnconstrained.stroll(1.0F), 2), Pair.of(BehaviorLookWalk.create(1.0F, 3), 1), Pair.of(new Croak(), 2), Pair.of(BehaviorBuilder.triggerIf(Entity::onGround), 1))))), ImmutableSet.of(Pair.of(MemoryModuleType.LONG_JUMP_MID_JUMP, MemoryStatus.VALUE_ABSENT), Pair.of(MemoryModuleType.IS_PREGNANT, MemoryStatus.VALUE_PRESENT)));
    }

    private static void initJumpActivity(BehaviorController<Frog> behaviorcontroller) {
        behaviorcontroller.addActivityWithConditions(Activity.LONG_JUMP, ImmutableList.of(Pair.of(0, new LongJumpMidJump(FrogAi.TIME_BETWEEN_LONG_JUMPS, SoundEffects.FROG_STEP)), Pair.of(1, new LongJumpToPreferredBlock<>(FrogAi.TIME_BETWEEN_LONG_JUMPS, 2, 4, 3.5714288F, (frog) -> {
            return SoundEffects.FROG_LONG_JUMP;
        }, TagsBlock.FROG_PREFER_JUMP_TO, 0.5F, FrogAi::isAcceptableLandingSpot))), ImmutableSet.of(Pair.of(MemoryModuleType.TEMPTING_PLAYER, MemoryStatus.VALUE_ABSENT), Pair.of(MemoryModuleType.BREED_TARGET, MemoryStatus.VALUE_ABSENT), Pair.of(MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS, MemoryStatus.VALUE_ABSENT), Pair.of(MemoryModuleType.IS_IN_WATER, MemoryStatus.VALUE_ABSENT)));
    }

    private static void initTongueActivity(BehaviorController<Frog> behaviorcontroller) {
        behaviorcontroller.addActivityAndRemoveMemoryWhenStopped(Activity.TONGUE, 0, ImmutableList.of(BehaviorAttackTargetForget.create(), new ShootTongue(SoundEffects.FROG_TONGUE, SoundEffects.FROG_EAT)), MemoryModuleType.ATTACK_TARGET);
    }

    private static <E extends EntityInsentient> boolean isAcceptableLandingSpot(E e0, BlockPosition blockposition) {
        World world = e0.level();
        BlockPosition blockposition1 = blockposition.below();

        if (world.getFluidState(blockposition).isEmpty() && world.getFluidState(blockposition1).isEmpty() && world.getFluidState(blockposition.above()).isEmpty()) {
            IBlockData iblockdata = world.getBlockState(blockposition);
            IBlockData iblockdata1 = world.getBlockState(blockposition1);

            if (!iblockdata.is(TagsBlock.FROG_PREFER_JUMP_TO) && !iblockdata1.is(TagsBlock.FROG_PREFER_JUMP_TO)) {
                PathfindingContext pathfindingcontext = new PathfindingContext(e0.level(), e0);
                PathType pathtype = PathfinderNormal.getPathTypeStatic(pathfindingcontext, blockposition.mutable());
                PathType pathtype1 = PathfinderNormal.getPathTypeStatic(pathfindingcontext, blockposition1.mutable());

                return pathtype != PathType.TRAPDOOR && (!iblockdata.isAir() || pathtype1 != PathType.TRAPDOOR) ? LongJumpToRandomPos.defaultAcceptableLandingSpot(e0, blockposition) : true;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    private static boolean canAttack(Frog frog) {
        return !BehaviorUtil.isBreeding(frog);
    }

    public static void updateActivity(Frog frog) {
        frog.getBrain().setActiveActivityToFirstValid(ImmutableList.of(Activity.TONGUE, Activity.LAY_SPAWN, Activity.LONG_JUMP, Activity.SWIM, Activity.IDLE));
    }

    public static Predicate<ItemStack> getTemptations() {
        return (itemstack) -> {
            return itemstack.is(TagsItem.FROG_FOOD);
        };
    }
}
