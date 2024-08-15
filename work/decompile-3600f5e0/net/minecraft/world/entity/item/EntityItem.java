package net.minecraft.world.entity.item;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.stats.StatisticList;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagsFluid;
import net.minecraft.tags.TagsItem;
import net.minecraft.util.MathHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EnumMoveType;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.World;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3D;

public class EntityItem extends Entity implements TraceableEntity {

    private static final DataWatcherObject<ItemStack> DATA_ITEM = DataWatcher.defineId(EntityItem.class, DataWatcherRegistry.ITEM_STACK);
    private static final float FLOAT_HEIGHT = 0.1F;
    public static final float EYE_HEIGHT = 0.2125F;
    private static final int LIFETIME = 6000;
    private static final int INFINITE_PICKUP_DELAY = 32767;
    private static final int INFINITE_LIFETIME = -32768;
    public int age;
    public int pickupDelay;
    private int health;
    @Nullable
    public UUID thrower;
    @Nullable
    private Entity cachedThrower;
    @Nullable
    public UUID target;
    public final float bobOffs;

    public EntityItem(EntityTypes<? extends EntityItem> entitytypes, World world) {
        super(entitytypes, world);
        this.health = 5;
        this.bobOffs = this.random.nextFloat() * 3.1415927F * 2.0F;
        this.setYRot(this.random.nextFloat() * 360.0F);
    }

    public EntityItem(World world, double d0, double d1, double d2, ItemStack itemstack) {
        this(world, d0, d1, d2, itemstack, world.random.nextDouble() * 0.2D - 0.1D, 0.2D, world.random.nextDouble() * 0.2D - 0.1D);
    }

    public EntityItem(World world, double d0, double d1, double d2, ItemStack itemstack, double d3, double d4, double d5) {
        this(EntityTypes.ITEM, world);
        this.setPos(d0, d1, d2);
        this.setDeltaMovement(d3, d4, d5);
        this.setItem(itemstack);
    }

    private EntityItem(EntityItem entityitem) {
        super(entityitem.getType(), entityitem.level());
        this.health = 5;
        this.setItem(entityitem.getItem().copy());
        this.copyPosition(entityitem);
        this.age = entityitem.age;
        this.bobOffs = entityitem.bobOffs;
    }

    @Override
    public boolean dampensVibrations() {
        return this.getItem().is(TagsItem.DAMPENS_VIBRATIONS);
    }

    @Nullable
    @Override
    public Entity getOwner() {
        if (this.cachedThrower != null && !this.cachedThrower.isRemoved()) {
            return this.cachedThrower;
        } else {
            if (this.thrower != null) {
                World world = this.level();

                if (world instanceof WorldServer) {
                    WorldServer worldserver = (WorldServer) world;

                    this.cachedThrower = worldserver.getEntity(this.thrower);
                    return this.cachedThrower;
                }
            }

            return null;
        }
    }

    @Override
    public void restoreFrom(Entity entity) {
        super.restoreFrom(entity);
        if (entity instanceof EntityItem entityitem) {
            this.cachedThrower = entityitem.cachedThrower;
        }

    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    @Override
    protected void defineSynchedData(DataWatcher.a datawatcher_a) {
        datawatcher_a.define(EntityItem.DATA_ITEM, ItemStack.EMPTY);
    }

    @Override
    protected double getDefaultGravity() {
        return 0.04D;
    }

    @Override
    public void tick() {
        if (this.getItem().isEmpty()) {
            this.discard();
        } else {
            super.tick();
            if (this.pickupDelay > 0 && this.pickupDelay != 32767) {
                --this.pickupDelay;
            }

            this.xo = this.getX();
            this.yo = this.getY();
            this.zo = this.getZ();
            Vec3D vec3d = this.getDeltaMovement();

            if (this.isInWater() && this.getFluidHeight(TagsFluid.WATER) > 0.10000000149011612D) {
                this.setUnderwaterMovement();
            } else if (this.isInLava() && this.getFluidHeight(TagsFluid.LAVA) > 0.10000000149011612D) {
                this.setUnderLavaMovement();
            } else {
                this.applyGravity();
            }

            if (this.level().isClientSide) {
                this.noPhysics = false;
            } else {
                this.noPhysics = !this.level().noCollision(this, this.getBoundingBox().deflate(1.0E-7D));
                if (this.noPhysics) {
                    this.moveTowardsClosestSpace(this.getX(), (this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0D, this.getZ());
                }
            }

            if (!this.onGround() || this.getDeltaMovement().horizontalDistanceSqr() > 9.999999747378752E-6D || (this.tickCount + this.getId()) % 4 == 0) {
                this.move(EnumMoveType.SELF, this.getDeltaMovement());
                float f = 0.98F;

                if (this.onGround()) {
                    f = this.level().getBlockState(this.getBlockPosBelowThatAffectsMyMovement()).getBlock().getFriction() * 0.98F;
                }

                this.setDeltaMovement(this.getDeltaMovement().multiply((double) f, 0.98D, (double) f));
                if (this.onGround()) {
                    Vec3D vec3d1 = this.getDeltaMovement();

                    if (vec3d1.y < 0.0D) {
                        this.setDeltaMovement(vec3d1.multiply(1.0D, -0.5D, 1.0D));
                    }
                }
            }

            boolean flag = MathHelper.floor(this.xo) != MathHelper.floor(this.getX()) || MathHelper.floor(this.yo) != MathHelper.floor(this.getY()) || MathHelper.floor(this.zo) != MathHelper.floor(this.getZ());
            int i = flag ? 2 : 40;

            if (this.tickCount % i == 0 && !this.level().isClientSide && this.isMergable()) {
                this.mergeWithNeighbours();
            }

            if (this.age != -32768) {
                ++this.age;
            }

            this.hasImpulse |= this.updateInWaterStateAndDoFluidPushing();
            if (!this.level().isClientSide) {
                double d0 = this.getDeltaMovement().subtract(vec3d).lengthSqr();

                if (d0 > 0.01D) {
                    this.hasImpulse = true;
                }
            }

            if (!this.level().isClientSide && this.age >= 6000) {
                this.discard();
            }

        }
    }

    @Override
    protected BlockPosition getBlockPosBelowThatAffectsMyMovement() {
        return this.getOnPos(0.999999F);
    }

    private void setUnderwaterMovement() {
        Vec3D vec3d = this.getDeltaMovement();

        this.setDeltaMovement(vec3d.x * 0.9900000095367432D, vec3d.y + (double) (vec3d.y < 0.05999999865889549D ? 5.0E-4F : 0.0F), vec3d.z * 0.9900000095367432D);
    }

    private void setUnderLavaMovement() {
        Vec3D vec3d = this.getDeltaMovement();

        this.setDeltaMovement(vec3d.x * 0.949999988079071D, vec3d.y + (double) (vec3d.y < 0.05999999865889549D ? 5.0E-4F : 0.0F), vec3d.z * 0.949999988079071D);
    }

    private void mergeWithNeighbours() {
        if (this.isMergable()) {
            List<EntityItem> list = this.level().getEntitiesOfClass(EntityItem.class, this.getBoundingBox().inflate(0.5D, 0.0D, 0.5D), (entityitem) -> {
                return entityitem != this && entityitem.isMergable();
            });
            Iterator iterator = list.iterator();

            while (iterator.hasNext()) {
                EntityItem entityitem = (EntityItem) iterator.next();

                if (entityitem.isMergable()) {
                    this.tryToMerge(entityitem);
                    if (this.isRemoved()) {
                        break;
                    }
                }
            }

        }
    }

    private boolean isMergable() {
        ItemStack itemstack = this.getItem();

        return this.isAlive() && this.pickupDelay != 32767 && this.age != -32768 && this.age < 6000 && itemstack.getCount() < itemstack.getMaxStackSize();
    }

    private void tryToMerge(EntityItem entityitem) {
        ItemStack itemstack = this.getItem();
        ItemStack itemstack1 = entityitem.getItem();

        if (Objects.equals(this.target, entityitem.target) && areMergable(itemstack, itemstack1)) {
            if (itemstack1.getCount() < itemstack.getCount()) {
                merge(this, itemstack, entityitem, itemstack1);
            } else {
                merge(entityitem, itemstack1, this, itemstack);
            }

        }
    }

    public static boolean areMergable(ItemStack itemstack, ItemStack itemstack1) {
        return itemstack1.getCount() + itemstack.getCount() > itemstack1.getMaxStackSize() ? false : ItemStack.isSameItemSameComponents(itemstack, itemstack1);
    }

    public static ItemStack merge(ItemStack itemstack, ItemStack itemstack1, int i) {
        int j = Math.min(Math.min(itemstack.getMaxStackSize(), i) - itemstack.getCount(), itemstack1.getCount());
        ItemStack itemstack2 = itemstack.copyWithCount(itemstack.getCount() + j);

        itemstack1.shrink(j);
        return itemstack2;
    }

    private static void merge(EntityItem entityitem, ItemStack itemstack, ItemStack itemstack1) {
        ItemStack itemstack2 = merge(itemstack, itemstack1, 64);

        entityitem.setItem(itemstack2);
    }

    private static void merge(EntityItem entityitem, ItemStack itemstack, EntityItem entityitem1, ItemStack itemstack1) {
        merge(entityitem, itemstack, itemstack1);
        entityitem.pickupDelay = Math.max(entityitem.pickupDelay, entityitem1.pickupDelay);
        entityitem.age = Math.min(entityitem.age, entityitem1.age);
        if (itemstack1.isEmpty()) {
            entityitem1.discard();
        }

    }

    @Override
    public boolean fireImmune() {
        return this.getItem().has(DataComponents.FIRE_RESISTANT) || super.fireImmune();
    }

    @Override
    public boolean hurt(DamageSource damagesource, float f) {
        if (this.isInvulnerableTo(damagesource)) {
            return false;
        } else if (!this.getItem().isEmpty() && this.getItem().is(Items.NETHER_STAR) && damagesource.is(DamageTypeTags.IS_EXPLOSION)) {
            return false;
        } else if (!this.getItem().canBeHurtBy(damagesource)) {
            return false;
        } else if (this.level().isClientSide) {
            return true;
        } else {
            this.markHurt();
            this.health = (int) ((float) this.health - f);
            this.gameEvent(GameEvent.ENTITY_DAMAGE, damagesource.getEntity());
            if (this.health <= 0) {
                this.getItem().onDestroyed(this);
                this.discard();
            }

            return true;
        }
    }

    @Override
    public void addAdditionalSaveData(NBTTagCompound nbttagcompound) {
        nbttagcompound.putShort("Health", (short) this.health);
        nbttagcompound.putShort("Age", (short) this.age);
        nbttagcompound.putShort("PickupDelay", (short) this.pickupDelay);
        if (this.thrower != null) {
            nbttagcompound.putUUID("Thrower", this.thrower);
        }

        if (this.target != null) {
            nbttagcompound.putUUID("Owner", this.target);
        }

        if (!this.getItem().isEmpty()) {
            nbttagcompound.put("Item", this.getItem().save(this.registryAccess()));
        }

    }

    @Override
    public void readAdditionalSaveData(NBTTagCompound nbttagcompound) {
        this.health = nbttagcompound.getShort("Health");
        this.age = nbttagcompound.getShort("Age");
        if (nbttagcompound.contains("PickupDelay")) {
            this.pickupDelay = nbttagcompound.getShort("PickupDelay");
        }

        if (nbttagcompound.hasUUID("Owner")) {
            this.target = nbttagcompound.getUUID("Owner");
        }

        if (nbttagcompound.hasUUID("Thrower")) {
            this.thrower = nbttagcompound.getUUID("Thrower");
            this.cachedThrower = null;
        }

        if (nbttagcompound.contains("Item", 10)) {
            NBTTagCompound nbttagcompound1 = nbttagcompound.getCompound("Item");

            this.setItem((ItemStack) ItemStack.parse(this.registryAccess(), nbttagcompound1).orElse(ItemStack.EMPTY));
        } else {
            this.setItem(ItemStack.EMPTY);
        }

        if (this.getItem().isEmpty()) {
            this.discard();
        }

    }

    @Override
    public void playerTouch(EntityHuman entityhuman) {
        if (!this.level().isClientSide) {
            ItemStack itemstack = this.getItem();
            Item item = itemstack.getItem();
            int i = itemstack.getCount();

            if (this.pickupDelay == 0 && (this.target == null || this.target.equals(entityhuman.getUUID())) && entityhuman.getInventory().add(itemstack)) {
                entityhuman.take(this, i);
                if (itemstack.isEmpty()) {
                    this.discard();
                    itemstack.setCount(i);
                }

                entityhuman.awardStat(StatisticList.ITEM_PICKED_UP.get(item), i);
                entityhuman.onItemPickup(this);
            }

        }
    }

    @Override
    public IChatBaseComponent getName() {
        IChatBaseComponent ichatbasecomponent = this.getCustomName();

        return (IChatBaseComponent) (ichatbasecomponent != null ? ichatbasecomponent : IChatBaseComponent.translatable(this.getItem().getDescriptionId()));
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Nullable
    @Override
    public Entity changeDimension(WorldServer worldserver) {
        Entity entity = super.changeDimension(worldserver);

        if (!this.level().isClientSide && entity instanceof EntityItem) {
            ((EntityItem) entity).mergeWithNeighbours();
        }

        return entity;
    }

    public ItemStack getItem() {
        return (ItemStack) this.getEntityData().get(EntityItem.DATA_ITEM);
    }

    public void setItem(ItemStack itemstack) {
        this.getEntityData().set(EntityItem.DATA_ITEM, itemstack);
    }

    @Override
    public void onSyncedDataUpdated(DataWatcherObject<?> datawatcherobject) {
        super.onSyncedDataUpdated(datawatcherobject);
        if (EntityItem.DATA_ITEM.equals(datawatcherobject)) {
            this.getItem().setEntityRepresentation(this);
        }

    }

    public void setTarget(@Nullable UUID uuid) {
        this.target = uuid;
    }

    public void setThrower(Entity entity) {
        this.thrower = entity.getUUID();
        this.cachedThrower = entity;
    }

    public int getAge() {
        return this.age;
    }

    public void setDefaultPickUpDelay() {
        this.pickupDelay = 10;
    }

    public void setNoPickUpDelay() {
        this.pickupDelay = 0;
    }

    public void setNeverPickUp() {
        this.pickupDelay = 32767;
    }

    public void setPickUpDelay(int i) {
        this.pickupDelay = i;
    }

    public boolean hasPickUpDelay() {
        return this.pickupDelay > 0;
    }

    public void setUnlimitedLifetime() {
        this.age = -32768;
    }

    public void setExtendedLifetime() {
        this.age = -6000;
    }

    public void makeFakeItem() {
        this.setNeverPickUp();
        this.age = 5999;
    }

    public float getSpin(float f) {
        return ((float) this.getAge() + f) / 20.0F + this.bobOffs;
    }

    public EntityItem copy() {
        return new EntityItem(this);
    }

    @Override
    public SoundCategory getSoundSource() {
        return SoundCategory.AMBIENT;
    }

    @Override
    public float getVisualRotationYInDegrees() {
        return 180.0F - this.getSpin(0.5F) / 6.2831855F * 360.0F;
    }

    @Override
    public SlotAccess getSlot(int i) {
        return i == 0 ? SlotAccess.of(this::getItem, this::setItem) : super.getSlot(i);
    }
}
