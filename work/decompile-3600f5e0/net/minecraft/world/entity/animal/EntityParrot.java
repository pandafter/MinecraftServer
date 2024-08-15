package net.minecraft.world.entity.animal;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.SystemUtils;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.tags.TagsBlock;
import net.minecraft.tags.TagsItem;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.INamable;
import net.minecraft.util.MathHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyDamageScaler;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityAgeable;
import net.minecraft.world.entity.EntityCreature;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EnumMobSpawn;
import net.minecraft.world.entity.GroupDataEntity;
import net.minecraft.world.entity.VariantHolder;
import net.minecraft.world.entity.ai.attributes.AttributeProvider;
import net.minecraft.world.entity.ai.attributes.GenericAttributes;
import net.minecraft.world.entity.ai.control.ControllerMoveFlying;
import net.minecraft.world.entity.ai.goal.PathfinderGoalFloat;
import net.minecraft.world.entity.ai.goal.PathfinderGoalFollowEntity;
import net.minecraft.world.entity.ai.goal.PathfinderGoalFollowOwner;
import net.minecraft.world.entity.ai.goal.PathfinderGoalLookAtPlayer;
import net.minecraft.world.entity.ai.goal.PathfinderGoalPanic;
import net.minecraft.world.entity.ai.goal.PathfinderGoalPerch;
import net.minecraft.world.entity.ai.goal.PathfinderGoalRandomFly;
import net.minecraft.world.entity.ai.goal.PathfinderGoalSit;
import net.minecraft.world.entity.ai.navigation.NavigationAbstract;
import net.minecraft.world.entity.ai.navigation.NavigationFlying;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.World;
import net.minecraft.world.level.WorldAccess;
import net.minecraft.world.level.block.BlockLeaves;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3D;

public class EntityParrot extends EntityPerchable implements VariantHolder<EntityParrot.Variant>, EntityBird {

    private static final DataWatcherObject<Integer> DATA_VARIANT_ID = DataWatcher.defineId(EntityParrot.class, DataWatcherRegistry.INT);
    private static final Predicate<EntityInsentient> NOT_PARROT_PREDICATE = new Predicate<EntityInsentient>() {
        public boolean test(@Nullable EntityInsentient entityinsentient) {
            return entityinsentient != null && EntityParrot.MOB_SOUND_MAP.containsKey(entityinsentient.getType());
        }
    };
    static final Map<EntityTypes<?>, SoundEffect> MOB_SOUND_MAP = (Map) SystemUtils.make(Maps.newHashMap(), (hashmap) -> {
        hashmap.put(EntityTypes.BLAZE, SoundEffects.PARROT_IMITATE_BLAZE);
        hashmap.put(EntityTypes.BOGGED, SoundEffects.PARROT_IMITATE_BOGGED);
        hashmap.put(EntityTypes.BREEZE, SoundEffects.PARROT_IMITATE_BREEZE);
        hashmap.put(EntityTypes.CAVE_SPIDER, SoundEffects.PARROT_IMITATE_SPIDER);
        hashmap.put(EntityTypes.CREEPER, SoundEffects.PARROT_IMITATE_CREEPER);
        hashmap.put(EntityTypes.DROWNED, SoundEffects.PARROT_IMITATE_DROWNED);
        hashmap.put(EntityTypes.ELDER_GUARDIAN, SoundEffects.PARROT_IMITATE_ELDER_GUARDIAN);
        hashmap.put(EntityTypes.ENDER_DRAGON, SoundEffects.PARROT_IMITATE_ENDER_DRAGON);
        hashmap.put(EntityTypes.ENDERMITE, SoundEffects.PARROT_IMITATE_ENDERMITE);
        hashmap.put(EntityTypes.EVOKER, SoundEffects.PARROT_IMITATE_EVOKER);
        hashmap.put(EntityTypes.GHAST, SoundEffects.PARROT_IMITATE_GHAST);
        hashmap.put(EntityTypes.GUARDIAN, SoundEffects.PARROT_IMITATE_GUARDIAN);
        hashmap.put(EntityTypes.HOGLIN, SoundEffects.PARROT_IMITATE_HOGLIN);
        hashmap.put(EntityTypes.HUSK, SoundEffects.PARROT_IMITATE_HUSK);
        hashmap.put(EntityTypes.ILLUSIONER, SoundEffects.PARROT_IMITATE_ILLUSIONER);
        hashmap.put(EntityTypes.MAGMA_CUBE, SoundEffects.PARROT_IMITATE_MAGMA_CUBE);
        hashmap.put(EntityTypes.PHANTOM, SoundEffects.PARROT_IMITATE_PHANTOM);
        hashmap.put(EntityTypes.PIGLIN, SoundEffects.PARROT_IMITATE_PIGLIN);
        hashmap.put(EntityTypes.PIGLIN_BRUTE, SoundEffects.PARROT_IMITATE_PIGLIN_BRUTE);
        hashmap.put(EntityTypes.PILLAGER, SoundEffects.PARROT_IMITATE_PILLAGER);
        hashmap.put(EntityTypes.RAVAGER, SoundEffects.PARROT_IMITATE_RAVAGER);
        hashmap.put(EntityTypes.SHULKER, SoundEffects.PARROT_IMITATE_SHULKER);
        hashmap.put(EntityTypes.SILVERFISH, SoundEffects.PARROT_IMITATE_SILVERFISH);
        hashmap.put(EntityTypes.SKELETON, SoundEffects.PARROT_IMITATE_SKELETON);
        hashmap.put(EntityTypes.SLIME, SoundEffects.PARROT_IMITATE_SLIME);
        hashmap.put(EntityTypes.SPIDER, SoundEffects.PARROT_IMITATE_SPIDER);
        hashmap.put(EntityTypes.STRAY, SoundEffects.PARROT_IMITATE_STRAY);
        hashmap.put(EntityTypes.VEX, SoundEffects.PARROT_IMITATE_VEX);
        hashmap.put(EntityTypes.VINDICATOR, SoundEffects.PARROT_IMITATE_VINDICATOR);
        hashmap.put(EntityTypes.WARDEN, SoundEffects.PARROT_IMITATE_WARDEN);
        hashmap.put(EntityTypes.WITCH, SoundEffects.PARROT_IMITATE_WITCH);
        hashmap.put(EntityTypes.WITHER, SoundEffects.PARROT_IMITATE_WITHER);
        hashmap.put(EntityTypes.WITHER_SKELETON, SoundEffects.PARROT_IMITATE_WITHER_SKELETON);
        hashmap.put(EntityTypes.ZOGLIN, SoundEffects.PARROT_IMITATE_ZOGLIN);
        hashmap.put(EntityTypes.ZOMBIE, SoundEffects.PARROT_IMITATE_ZOMBIE);
        hashmap.put(EntityTypes.ZOMBIE_VILLAGER, SoundEffects.PARROT_IMITATE_ZOMBIE_VILLAGER);
    });
    public float flap;
    public float flapSpeed;
    public float oFlapSpeed;
    public float oFlap;
    private float flapping = 1.0F;
    private float nextFlap = 1.0F;
    private boolean partyParrot;
    @Nullable
    private BlockPosition jukebox;

    public EntityParrot(EntityTypes<? extends EntityParrot> entitytypes, World world) {
        super(entitytypes, world);
        this.moveControl = new ControllerMoveFlying(this, 10, false);
        this.setPathfindingMalus(PathType.DANGER_FIRE, -1.0F);
        this.setPathfindingMalus(PathType.DAMAGE_FIRE, -1.0F);
        this.setPathfindingMalus(PathType.COCOA, -1.0F);
    }

    @Nullable
    @Override
    public GroupDataEntity finalizeSpawn(WorldAccess worldaccess, DifficultyDamageScaler difficultydamagescaler, EnumMobSpawn enummobspawn, @Nullable GroupDataEntity groupdataentity) {
        this.setVariant((EntityParrot.Variant) SystemUtils.getRandom((Object[]) EntityParrot.Variant.values(), worldaccess.getRandom()));
        if (groupdataentity == null) {
            groupdataentity = new EntityAgeable.a(false);
        }

        return super.finalizeSpawn(worldaccess, difficultydamagescaler, enummobspawn, (GroupDataEntity) groupdataentity);
    }

    @Override
    public boolean isBaby() {
        return false;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new PathfinderGoalPanic(this, 1.25D));
        this.goalSelector.addGoal(0, new PathfinderGoalFloat(this));
        this.goalSelector.addGoal(1, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
        this.goalSelector.addGoal(2, new PathfinderGoalSit(this));
        this.goalSelector.addGoal(2, new PathfinderGoalFollowOwner(this, 1.0D, 5.0F, 1.0F, true));
        this.goalSelector.addGoal(2, new EntityParrot.a(this, 1.0D));
        this.goalSelector.addGoal(3, new PathfinderGoalPerch(this));
        this.goalSelector.addGoal(3, new PathfinderGoalFollowEntity(this, 1.0D, 3.0F, 7.0F));
    }

    public static AttributeProvider.Builder createAttributes() {
        return EntityInsentient.createMobAttributes().add(GenericAttributes.MAX_HEALTH, 6.0D).add(GenericAttributes.FLYING_SPEED, 0.4000000059604645D).add(GenericAttributes.MOVEMENT_SPEED, 0.20000000298023224D);
    }

    @Override
    protected NavigationAbstract createNavigation(World world) {
        NavigationFlying navigationflying = new NavigationFlying(this, world);

        navigationflying.setCanOpenDoors(false);
        navigationflying.setCanFloat(true);
        navigationflying.setCanPassDoors(true);
        return navigationflying;
    }

    @Override
    public void aiStep() {
        if (this.jukebox == null || !this.jukebox.closerToCenterThan(this.position(), 3.46D) || !this.level().getBlockState(this.jukebox).is(Blocks.JUKEBOX)) {
            this.partyParrot = false;
            this.jukebox = null;
        }

        if (this.level().random.nextInt(400) == 0) {
            imitateNearbyMobs(this.level(), this);
        }

        super.aiStep();
        this.calculateFlapping();
    }

    @Override
    public void setRecordPlayingNearby(BlockPosition blockposition, boolean flag) {
        this.jukebox = blockposition;
        this.partyParrot = flag;
    }

    public boolean isPartyParrot() {
        return this.partyParrot;
    }

    private void calculateFlapping() {
        this.oFlap = this.flap;
        this.oFlapSpeed = this.flapSpeed;
        this.flapSpeed += (float) (!this.onGround() && !this.isPassenger() ? 4 : -1) * 0.3F;
        this.flapSpeed = MathHelper.clamp(this.flapSpeed, 0.0F, 1.0F);
        if (!this.onGround() && this.flapping < 1.0F) {
            this.flapping = 1.0F;
        }

        this.flapping *= 0.9F;
        Vec3D vec3d = this.getDeltaMovement();

        if (!this.onGround() && vec3d.y < 0.0D) {
            this.setDeltaMovement(vec3d.multiply(1.0D, 0.6D, 1.0D));
        }

        this.flap += this.flapping * 2.0F;
    }

    public static boolean imitateNearbyMobs(World world, Entity entity) {
        if (entity.isAlive() && !entity.isSilent() && world.random.nextInt(2) == 0) {
            List<EntityInsentient> list = world.getEntitiesOfClass(EntityInsentient.class, entity.getBoundingBox().inflate(20.0D), EntityParrot.NOT_PARROT_PREDICATE);

            if (!list.isEmpty()) {
                EntityInsentient entityinsentient = (EntityInsentient) list.get(world.random.nextInt(list.size()));

                if (!entityinsentient.isSilent()) {
                    SoundEffect soundeffect = getImitatedSound(entityinsentient.getType());

                    world.playSound((EntityHuman) null, entity.getX(), entity.getY(), entity.getZ(), soundeffect, entity.getSoundSource(), 0.7F, getPitch(world.random));
                    return true;
                }
            }

            return false;
        } else {
            return false;
        }
    }

    @Override
    public EnumInteractionResult mobInteract(EntityHuman entityhuman, EnumHand enumhand) {
        ItemStack itemstack = entityhuman.getItemInHand(enumhand);

        if (!this.isTame() && itemstack.is(TagsItem.PARROT_FOOD)) {
            itemstack.consume(1, entityhuman);
            if (!this.isSilent()) {
                this.level().playSound((EntityHuman) null, this.getX(), this.getY(), this.getZ(), SoundEffects.PARROT_EAT, this.getSoundSource(), 1.0F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
            }

            if (!this.level().isClientSide) {
                if (this.random.nextInt(10) == 0) {
                    this.tame(entityhuman);
                    this.level().broadcastEntityEvent(this, (byte) 7);
                } else {
                    this.level().broadcastEntityEvent(this, (byte) 6);
                }
            }

            return EnumInteractionResult.sidedSuccess(this.level().isClientSide);
        } else if (!itemstack.is(TagsItem.PARROT_POISONOUS_FOOD)) {
            if (!this.isFlying() && this.isTame() && this.isOwnedBy(entityhuman)) {
                if (!this.level().isClientSide) {
                    this.setOrderedToSit(!this.isOrderedToSit());
                }

                return EnumInteractionResult.sidedSuccess(this.level().isClientSide);
            } else {
                return super.mobInteract(entityhuman, enumhand);
            }
        } else {
            itemstack.consume(1, entityhuman);
            this.addEffect(new MobEffect(MobEffects.POISON, 900));
            if (entityhuman.isCreative() || !this.isInvulnerable()) {
                this.hurt(this.damageSources().playerAttack(entityhuman), Float.MAX_VALUE);
            }

            return EnumInteractionResult.sidedSuccess(this.level().isClientSide);
        }
    }

    @Override
    public boolean isFood(ItemStack itemstack) {
        return false;
    }

    public static boolean checkParrotSpawnRules(EntityTypes<EntityParrot> entitytypes, GeneratorAccess generatoraccess, EnumMobSpawn enummobspawn, BlockPosition blockposition, RandomSource randomsource) {
        return generatoraccess.getBlockState(blockposition.below()).is(TagsBlock.PARROTS_SPAWNABLE_ON) && isBrightEnoughToSpawn(generatoraccess, blockposition);
    }

    @Override
    protected void checkFallDamage(double d0, boolean flag, IBlockData iblockdata, BlockPosition blockposition) {}

    @Override
    public boolean canMate(EntityAnimal entityanimal) {
        return false;
    }

    @Nullable
    @Override
    public EntityAgeable getBreedOffspring(WorldServer worldserver, EntityAgeable entityageable) {
        return null;
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        return entity.hurt(this.damageSources().mobAttack(this), 3.0F);
    }

    @Nullable
    @Override
    public SoundEffect getAmbientSound() {
        return getAmbient(this.level(), this.level().random);
    }

    public static SoundEffect getAmbient(World world, RandomSource randomsource) {
        if (world.getDifficulty() != EnumDifficulty.PEACEFUL && randomsource.nextInt(1000) == 0) {
            List<EntityTypes<?>> list = Lists.newArrayList(EntityParrot.MOB_SOUND_MAP.keySet());

            return getImitatedSound((EntityTypes) list.get(randomsource.nextInt(list.size())));
        } else {
            return SoundEffects.PARROT_AMBIENT;
        }
    }

    private static SoundEffect getImitatedSound(EntityTypes<?> entitytypes) {
        return (SoundEffect) EntityParrot.MOB_SOUND_MAP.getOrDefault(entitytypes, SoundEffects.PARROT_AMBIENT);
    }

    @Override
    protected SoundEffect getHurtSound(DamageSource damagesource) {
        return SoundEffects.PARROT_HURT;
    }

    @Override
    protected SoundEffect getDeathSound() {
        return SoundEffects.PARROT_DEATH;
    }

    @Override
    protected void playStepSound(BlockPosition blockposition, IBlockData iblockdata) {
        this.playSound(SoundEffects.PARROT_STEP, 0.15F, 1.0F);
    }

    @Override
    protected boolean isFlapping() {
        return this.flyDist > this.nextFlap;
    }

    @Override
    protected void onFlap() {
        this.playSound(SoundEffects.PARROT_FLY, 0.15F, 1.0F);
        this.nextFlap = this.flyDist + this.flapSpeed / 2.0F;
    }

    @Override
    public float getVoicePitch() {
        return getPitch(this.random);
    }

    public static float getPitch(RandomSource randomsource) {
        return (randomsource.nextFloat() - randomsource.nextFloat()) * 0.2F + 1.0F;
    }

    @Override
    public SoundCategory getSoundSource() {
        return SoundCategory.NEUTRAL;
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    protected void doPush(Entity entity) {
        if (!(entity instanceof EntityHuman)) {
            super.doPush(entity);
        }
    }

    @Override
    public boolean hurt(DamageSource damagesource, float f) {
        if (this.isInvulnerableTo(damagesource)) {
            return false;
        } else {
            if (!this.level().isClientSide) {
                this.setOrderedToSit(false);
            }

            return super.hurt(damagesource, f);
        }
    }

    @Override
    public EntityParrot.Variant getVariant() {
        return EntityParrot.Variant.byId((Integer) this.entityData.get(EntityParrot.DATA_VARIANT_ID));
    }

    public void setVariant(EntityParrot.Variant entityparrot_variant) {
        this.entityData.set(EntityParrot.DATA_VARIANT_ID, entityparrot_variant.id);
    }

    @Override
    protected void defineSynchedData(DataWatcher.a datawatcher_a) {
        super.defineSynchedData(datawatcher_a);
        datawatcher_a.define(EntityParrot.DATA_VARIANT_ID, 0);
    }

    @Override
    public void addAdditionalSaveData(NBTTagCompound nbttagcompound) {
        super.addAdditionalSaveData(nbttagcompound);
        nbttagcompound.putInt("Variant", this.getVariant().id);
    }

    @Override
    public void readAdditionalSaveData(NBTTagCompound nbttagcompound) {
        super.readAdditionalSaveData(nbttagcompound);
        this.setVariant(EntityParrot.Variant.byId(nbttagcompound.getInt("Variant")));
    }

    @Override
    public boolean isFlying() {
        return !this.onGround();
    }

    @Override
    public Vec3D getLeashOffset() {
        return new Vec3D(0.0D, (double) (0.5F * this.getEyeHeight()), (double) (this.getBbWidth() * 0.4F));
    }

    public static enum Variant implements INamable {

        RED_BLUE(0, "red_blue"), BLUE(1, "blue"), GREEN(2, "green"), YELLOW_BLUE(3, "yellow_blue"), GRAY(4, "gray");

        public static final Codec<EntityParrot.Variant> CODEC = INamable.fromEnum(EntityParrot.Variant::values);
        private static final IntFunction<EntityParrot.Variant> BY_ID = ByIdMap.continuous(EntityParrot.Variant::getId, values(), ByIdMap.a.CLAMP);
        final int id;
        private final String name;

        private Variant(final int i, final String s) {
            this.id = i;
            this.name = s;
        }

        public int getId() {
            return this.id;
        }

        public static EntityParrot.Variant byId(int i) {
            return (EntityParrot.Variant) EntityParrot.Variant.BY_ID.apply(i);
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    private static class a extends PathfinderGoalRandomFly {

        public a(EntityCreature entitycreature, double d0) {
            super(entitycreature, d0);
        }

        @Nullable
        @Override
        protected Vec3D getPosition() {
            Vec3D vec3d = null;

            if (this.mob.isInWater()) {
                vec3d = LandRandomPos.getPos(this.mob, 15, 15);
            }

            if (this.mob.getRandom().nextFloat() >= this.probability) {
                vec3d = this.getTreePos();
            }

            return vec3d == null ? super.getPosition() : vec3d;
        }

        @Nullable
        private Vec3D getTreePos() {
            BlockPosition blockposition = this.mob.blockPosition();
            BlockPosition.MutableBlockPosition blockposition_mutableblockposition = new BlockPosition.MutableBlockPosition();
            BlockPosition.MutableBlockPosition blockposition_mutableblockposition1 = new BlockPosition.MutableBlockPosition();
            Iterable<BlockPosition> iterable = BlockPosition.betweenClosed(MathHelper.floor(this.mob.getX() - 3.0D), MathHelper.floor(this.mob.getY() - 6.0D), MathHelper.floor(this.mob.getZ() - 3.0D), MathHelper.floor(this.mob.getX() + 3.0D), MathHelper.floor(this.mob.getY() + 6.0D), MathHelper.floor(this.mob.getZ() + 3.0D));
            Iterator iterator = iterable.iterator();

            while (iterator.hasNext()) {
                BlockPosition blockposition1 = (BlockPosition) iterator.next();

                if (!blockposition.equals(blockposition1)) {
                    IBlockData iblockdata = this.mob.level().getBlockState(blockposition_mutableblockposition1.setWithOffset(blockposition1, EnumDirection.DOWN));
                    boolean flag = iblockdata.getBlock() instanceof BlockLeaves || iblockdata.is(TagsBlock.LOGS);

                    if (flag && this.mob.level().isEmptyBlock(blockposition1) && this.mob.level().isEmptyBlock(blockposition_mutableblockposition.setWithOffset(blockposition1, EnumDirection.UP))) {
                        return Vec3D.atBottomCenterOf(blockposition1);
                    }
                }
            }

            return null;
        }
    }
}
