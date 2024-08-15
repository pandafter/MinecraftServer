package net.minecraft.world.entity.animal.goat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.function.Predicate;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.tags.TagsItem;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.BehaviorController;
import net.minecraft.world.entity.ai.behavior.AnimalPanic;
import net.minecraft.world.entity.ai.behavior.BehaviorFollowAdult;
import net.minecraft.world.entity.ai.behavior.BehaviorGateSingle;
import net.minecraft.world.entity.ai.behavior.BehaviorLook;
import net.minecraft.world.entity.ai.behavior.BehaviorLookWalk;
import net.minecraft.world.entity.ai.behavior.BehaviorMakeLoveAnimal;
import net.minecraft.world.entity.ai.behavior.BehaviorNop;
import net.minecraft.world.entity.ai.behavior.BehaviorStrollRandomUnconstrained;
import net.minecraft.world.entity.ai.behavior.BehaviorSwim;
import net.minecraft.world.entity.ai.behavior.BehavorMove;
import net.minecraft.world.entity.ai.behavior.CountDownCooldownTicks;
import net.minecraft.world.entity.ai.behavior.FollowTemptation;
import net.minecraft.world.entity.ai.behavior.LongJumpMidJump;
import net.minecraft.world.entity.ai.behavior.LongJumpToRandomPos;
import net.minecraft.world.entity.ai.behavior.PrepareRamNearestTarget;
import net.minecraft.world.entity.ai.behavior.RamTarget;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTargetSometimes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.targeting.PathfinderTargetCondition;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.ItemStack;

public class GoatAi {

    public static final int RAM_PREPARE_TIME = 20;
    public static final int RAM_MAX_DISTANCE = 7;
    private static final UniformInt ADULT_FOLLOW_RANGE = UniformInt.of(5, 16);
    private static final float SPEED_MULTIPLIER_WHEN_IDLING = 1.0F;
    private static final float SPEED_MULTIPLIER_WHEN_FOLLOWING_ADULT = 1.25F;
    private static final float SPEED_MULTIPLIER_WHEN_TEMPTED = 1.25F;
    private static final float SPEED_MULTIPLIER_WHEN_PANICKING = 2.0F;
    private static final float SPEED_MULTIPLIER_WHEN_PREPARING_TO_RAM = 1.25F;
    private static final UniformInt TIME_BETWEEN_LONG_JUMPS = UniformInt.of(600, 1200);
    public static final int MAX_LONG_JUMP_HEIGHT = 5;
    public static final int MAX_LONG_JUMP_WIDTH = 5;
    public static final float MAX_JUMP_VELOCITY_MULTIPLIER = 3.5714288F;
    private static final UniformInt TIME_BETWEEN_RAMS = UniformInt.of(600, 6000);
    private static final UniformInt TIME_BETWEEN_RAMS_SCREAMER = UniformInt.of(100, 300);
    private static final PathfinderTargetCondition RAM_TARGET_CONDITIONS = PathfinderTargetCondition.forCombat().selector((entityliving) -> {
        return !entityliving.getType().equals(EntityTypes.GOAT) && entityliving.level().getWorldBorder().isWithinBounds(entityliving.getBoundingBox());
    });
    private static final float SPEED_MULTIPLIER_WHEN_RAMMING = 3.0F;
    public static final int RAM_MIN_DISTANCE = 4;
    public static final float ADULT_RAM_KNOCKBACK_FORCE = 2.5F;
    public static final float BABY_RAM_KNOCKBACK_FORCE = 1.0F;

    public GoatAi() {}

    protected static void initMemories(Goat goat, RandomSource randomsource) {
        goat.getBrain().setMemory(MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS, (Object) GoatAi.TIME_BETWEEN_LONG_JUMPS.sample(randomsource));
        goat.getBrain().setMemory(MemoryModuleType.RAM_COOLDOWN_TICKS, (Object) GoatAi.TIME_BETWEEN_RAMS.sample(randomsource));
    }

    protected static BehaviorController<?> makeBrain(BehaviorController<Goat> behaviorcontroller) {
        initCoreActivity(behaviorcontroller);
        initIdleActivity(behaviorcontroller);
        initLongJumpActivity(behaviorcontroller);
        initRamActivity(behaviorcontroller);
        behaviorcontroller.setCoreActivities(ImmutableSet.of(Activity.CORE));
        behaviorcontroller.setDefaultActivity(Activity.IDLE);
        behaviorcontroller.useDefaultActivity();
        return behaviorcontroller;
    }

    private static void initCoreActivity(BehaviorController<Goat> behaviorcontroller) {
        behaviorcontroller.addActivity(Activity.CORE, 0, ImmutableList.of(new BehaviorSwim(0.8F), new AnimalPanic<>(2.0F), new BehaviorLook(45, 90), new BehavorMove(), new CountDownCooldownTicks(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS), new CountDownCooldownTicks(MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS), new CountDownCooldownTicks(MemoryModuleType.RAM_COOLDOWN_TICKS)));
    }

    private static void initIdleActivity(BehaviorController<Goat> behaviorcontroller) {
        behaviorcontroller.addActivityWithConditions(Activity.IDLE, ImmutableList.of(Pair.of(0, SetEntityLookTargetSometimes.create(EntityTypes.PLAYER, 6.0F, UniformInt.of(30, 60))), Pair.of(0, new BehaviorMakeLoveAnimal(EntityTypes.GOAT)), Pair.of(1, new FollowTemptation((entityliving) -> {
            return 1.25F;
        })), Pair.of(2, BehaviorFollowAdult.create(GoatAi.ADULT_FOLLOW_RANGE, 1.25F)), Pair.of(3, new BehaviorGateSingle<>(ImmutableList.of(Pair.of(BehaviorStrollRandomUnconstrained.stroll(1.0F), 2), Pair.of(BehaviorLookWalk.create(1.0F, 3), 2), Pair.of(new BehaviorNop(30, 60), 1))))), ImmutableSet.of(Pair.of(MemoryModuleType.RAM_TARGET, MemoryStatus.VALUE_ABSENT), Pair.of(MemoryModuleType.LONG_JUMP_MID_JUMP, MemoryStatus.VALUE_ABSENT)));
    }

    private static void initLongJumpActivity(BehaviorController<Goat> behaviorcontroller) {
        behaviorcontroller.addActivityWithConditions(Activity.LONG_JUMP, ImmutableList.of(Pair.of(0, new LongJumpMidJump(GoatAi.TIME_BETWEEN_LONG_JUMPS, SoundEffects.GOAT_STEP)), Pair.of(1, new LongJumpToRandomPos<>(GoatAi.TIME_BETWEEN_LONG_JUMPS, 5, 5, 3.5714288F, (goat) -> {
            return goat.isScreamingGoat() ? SoundEffects.GOAT_SCREAMING_LONG_JUMP : SoundEffects.GOAT_LONG_JUMP;
        }))), ImmutableSet.of(Pair.of(MemoryModuleType.TEMPTING_PLAYER, MemoryStatus.VALUE_ABSENT), Pair.of(MemoryModuleType.BREED_TARGET, MemoryStatus.VALUE_ABSENT), Pair.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT), Pair.of(MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS, MemoryStatus.VALUE_ABSENT)));
    }

    private static void initRamActivity(BehaviorController<Goat> behaviorcontroller) {
        behaviorcontroller.addActivityWithConditions(Activity.RAM, ImmutableList.of(Pair.of(0, new RamTarget((goat) -> {
            return goat.isScreamingGoat() ? GoatAi.TIME_BETWEEN_RAMS_SCREAMER : GoatAi.TIME_BETWEEN_RAMS;
        }, GoatAi.RAM_TARGET_CONDITIONS, 3.0F, (goat) -> {
            return goat.isBaby() ? 1.0D : 2.5D;
        }, (goat) -> {
            return goat.isScreamingGoat() ? SoundEffects.GOAT_SCREAMING_RAM_IMPACT : SoundEffects.GOAT_RAM_IMPACT;
        }, (goat) -> {
            return goat.isScreamingGoat() ? SoundEffects.GOAT_SCREAMING_HORN_BREAK : SoundEffects.GOAT_HORN_BREAK;
        })), Pair.of(1, new PrepareRamNearestTarget<>((goat) -> {
            return goat.isScreamingGoat() ? GoatAi.TIME_BETWEEN_RAMS_SCREAMER.getMinValue() : GoatAi.TIME_BETWEEN_RAMS.getMinValue();
        }, 4, 7, 1.25F, GoatAi.RAM_TARGET_CONDITIONS, 20, (goat) -> {
            return goat.isScreamingGoat() ? SoundEffects.GOAT_SCREAMING_PREPARE_RAM : SoundEffects.GOAT_PREPARE_RAM;
        }))), ImmutableSet.of(Pair.of(MemoryModuleType.TEMPTING_PLAYER, MemoryStatus.VALUE_ABSENT), Pair.of(MemoryModuleType.BREED_TARGET, MemoryStatus.VALUE_ABSENT), Pair.of(MemoryModuleType.RAM_COOLDOWN_TICKS, MemoryStatus.VALUE_ABSENT)));
    }

    public static void updateActivity(Goat goat) {
        goat.getBrain().setActiveActivityToFirstValid(ImmutableList.of(Activity.RAM, Activity.LONG_JUMP, Activity.IDLE));
    }

    public static Predicate<ItemStack> getTemptations() {
        return (itemstack) -> {
            return itemstack.is(TagsItem.GOAT_FOOD);
        };
    }
}
