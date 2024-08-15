package net.minecraft.world.entity.projectile;

import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.TagsBlock;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityAreaEffectCloud;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.PotionRegistry;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.AbstractCandleBlock;
import net.minecraft.world.level.block.BlockCampfire;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.MovingObjectPosition;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import net.minecraft.world.phys.MovingObjectPositionEntity;

public class EntityPotion extends EntityProjectileThrowable implements ItemSupplier {

    public static final double SPLASH_RANGE = 4.0D;
    private static final double SPLASH_RANGE_SQ = 16.0D;
    public static final Predicate<EntityLiving> WATER_SENSITIVE_OR_ON_FIRE = (entityliving) -> {
        return entityliving.isSensitiveToWater() || entityliving.isOnFire();
    };

    public EntityPotion(EntityTypes<? extends EntityPotion> entitytypes, World world) {
        super(entitytypes, world);
    }

    public EntityPotion(World world, EntityLiving entityliving) {
        super(EntityTypes.POTION, entityliving, world);
    }

    public EntityPotion(World world, double d0, double d1, double d2) {
        super(EntityTypes.POTION, d0, d1, d2, world);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.SPLASH_POTION;
    }

    @Override
    protected double getDefaultGravity() {
        return 0.05D;
    }

    @Override
    protected void onHitBlock(MovingObjectPositionBlock movingobjectpositionblock) {
        super.onHitBlock(movingobjectpositionblock);
        if (!this.level().isClientSide) {
            ItemStack itemstack = this.getItem();
            EnumDirection enumdirection = movingobjectpositionblock.getDirection();
            BlockPosition blockposition = movingobjectpositionblock.getBlockPos();
            BlockPosition blockposition1 = blockposition.relative(enumdirection);
            PotionContents potioncontents = (PotionContents) itemstack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);

            if (potioncontents.is(Potions.WATER)) {
                this.dowseFire(blockposition1);
                this.dowseFire(blockposition1.relative(enumdirection.getOpposite()));
                Iterator iterator = EnumDirection.EnumDirectionLimit.HORIZONTAL.iterator();

                while (iterator.hasNext()) {
                    EnumDirection enumdirection1 = (EnumDirection) iterator.next();

                    this.dowseFire(blockposition1.relative(enumdirection1));
                }
            }

        }
    }

    @Override
    protected void onHit(MovingObjectPosition movingobjectposition) {
        super.onHit(movingobjectposition);
        if (!this.level().isClientSide) {
            ItemStack itemstack = this.getItem();
            PotionContents potioncontents = (PotionContents) itemstack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);

            if (potioncontents.is(Potions.WATER)) {
                this.applyWater();
            } else if (potioncontents.hasEffects()) {
                if (this.isLingering()) {
                    this.makeAreaOfEffectCloud(potioncontents);
                } else {
                    this.applySplash(potioncontents.getAllEffects(), movingobjectposition.getType() == MovingObjectPosition.EnumMovingObjectType.ENTITY ? ((MovingObjectPositionEntity) movingobjectposition).getEntity() : null);
                }
            }

            int i = potioncontents.potion().isPresent() && ((PotionRegistry) ((Holder) potioncontents.potion().get()).value()).hasInstantEffects() ? 2007 : 2002;

            this.level().levelEvent(i, this.blockPosition(), potioncontents.getColor());
            this.discard();
        }
    }

    private void applyWater() {
        AxisAlignedBB axisalignedbb = this.getBoundingBox().inflate(4.0D, 2.0D, 4.0D);
        List<EntityLiving> list = this.level().getEntitiesOfClass(EntityLiving.class, axisalignedbb, EntityPotion.WATER_SENSITIVE_OR_ON_FIRE);
        Iterator iterator = list.iterator();

        while (iterator.hasNext()) {
            EntityLiving entityliving = (EntityLiving) iterator.next();
            double d0 = this.distanceToSqr((Entity) entityliving);

            if (d0 < 16.0D) {
                if (entityliving.isSensitiveToWater()) {
                    entityliving.hurt(this.damageSources().indirectMagic(this, this.getOwner()), 1.0F);
                }

                if (entityliving.isOnFire() && entityliving.isAlive()) {
                    entityliving.extinguishFire();
                }
            }
        }

        List<Axolotl> list1 = this.level().getEntitiesOfClass(Axolotl.class, axisalignedbb);
        Iterator iterator1 = list1.iterator();

        while (iterator1.hasNext()) {
            Axolotl axolotl = (Axolotl) iterator1.next();

            axolotl.rehydrate();
        }

    }

    private void applySplash(Iterable<MobEffect> iterable, @Nullable Entity entity) {
        AxisAlignedBB axisalignedbb = this.getBoundingBox().inflate(4.0D, 2.0D, 4.0D);
        List<EntityLiving> list = this.level().getEntitiesOfClass(EntityLiving.class, axisalignedbb);

        if (!list.isEmpty()) {
            Entity entity1 = this.getEffectSource();
            Iterator iterator = list.iterator();

            while (iterator.hasNext()) {
                EntityLiving entityliving = (EntityLiving) iterator.next();

                if (entityliving.isAffectedByPotions()) {
                    double d0 = this.distanceToSqr((Entity) entityliving);

                    if (d0 < 16.0D) {
                        double d1;

                        if (entityliving == entity) {
                            d1 = 1.0D;
                        } else {
                            d1 = 1.0D - Math.sqrt(d0) / 4.0D;
                        }

                        Iterator iterator1 = iterable.iterator();

                        while (iterator1.hasNext()) {
                            MobEffect mobeffect = (MobEffect) iterator1.next();
                            Holder<MobEffectList> holder = mobeffect.getEffect();

                            if (((MobEffectList) holder.value()).isInstantenous()) {
                                ((MobEffectList) holder.value()).applyInstantenousEffect(this, this.getOwner(), entityliving, mobeffect.getAmplifier(), d1);
                            } else {
                                int i = mobeffect.mapDuration((j) -> {
                                    return (int) (d1 * (double) j + 0.5D);
                                });
                                MobEffect mobeffect1 = new MobEffect(holder, i, mobeffect.getAmplifier(), mobeffect.isAmbient(), mobeffect.isVisible());

                                if (!mobeffect1.endsWithin(20)) {
                                    entityliving.addEffect(mobeffect1, entity1);
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    private void makeAreaOfEffectCloud(PotionContents potioncontents) {
        EntityAreaEffectCloud entityareaeffectcloud = new EntityAreaEffectCloud(this.level(), this.getX(), this.getY(), this.getZ());
        Entity entity = this.getOwner();

        if (entity instanceof EntityLiving entityliving) {
            entityareaeffectcloud.setOwner(entityliving);
        }

        entityareaeffectcloud.setRadius(3.0F);
        entityareaeffectcloud.setRadiusOnUse(-0.5F);
        entityareaeffectcloud.setWaitTime(10);
        entityareaeffectcloud.setRadiusPerTick(-entityareaeffectcloud.getRadius() / (float) entityareaeffectcloud.getDuration());
        entityareaeffectcloud.setPotionContents(potioncontents);
        this.level().addFreshEntity(entityareaeffectcloud);
    }

    public boolean isLingering() {
        return this.getItem().is(Items.LINGERING_POTION);
    }

    private void dowseFire(BlockPosition blockposition) {
        IBlockData iblockdata = this.level().getBlockState(blockposition);

        if (iblockdata.is(TagsBlock.FIRE)) {
            this.level().destroyBlock(blockposition, false, this);
        } else if (AbstractCandleBlock.isLit(iblockdata)) {
            AbstractCandleBlock.extinguish((EntityHuman) null, iblockdata, this.level(), blockposition);
        } else if (BlockCampfire.isLitCampfire(iblockdata)) {
            this.level().levelEvent((EntityHuman) null, 1009, blockposition, 0);
            BlockCampfire.dowse(this.getOwner(), this.level(), blockposition, iblockdata);
            this.level().setBlockAndUpdate(blockposition, (IBlockData) iblockdata.setValue(BlockCampfire.LIT, false));
        }

    }
}
