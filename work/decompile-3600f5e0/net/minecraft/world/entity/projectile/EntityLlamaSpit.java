package net.minecraft.world.entity.projectile;

import net.minecraft.core.particles.Particles;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.util.MathHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.horse.EntityLlama;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.phys.MovingObjectPosition;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import net.minecraft.world.phys.MovingObjectPositionEntity;
import net.minecraft.world.phys.Vec3D;

public class EntityLlamaSpit extends IProjectile {

    public EntityLlamaSpit(EntityTypes<? extends EntityLlamaSpit> entitytypes, World world) {
        super(entitytypes, world);
    }

    public EntityLlamaSpit(World world, EntityLlama entityllama) {
        this(EntityTypes.LLAMA_SPIT, world);
        this.setOwner(entityllama);
        this.setPos(entityllama.getX() - (double) (entityllama.getBbWidth() + 1.0F) * 0.5D * (double) MathHelper.sin(entityllama.yBodyRot * 0.017453292F), entityllama.getEyeY() - 0.10000000149011612D, entityllama.getZ() + (double) (entityllama.getBbWidth() + 1.0F) * 0.5D * (double) MathHelper.cos(entityllama.yBodyRot * 0.017453292F));
    }

    @Override
    protected double getDefaultGravity() {
        return 0.06D;
    }

    @Override
    public void tick() {
        super.tick();
        Vec3D vec3d = this.getDeltaMovement();
        MovingObjectPosition movingobjectposition = ProjectileHelper.getHitResultOnMoveVector(this, this::canHitEntity);

        this.hitTargetOrDeflectSelf(movingobjectposition);
        double d0 = this.getX() + vec3d.x;
        double d1 = this.getY() + vec3d.y;
        double d2 = this.getZ() + vec3d.z;

        this.updateRotation();
        float f = 0.99F;

        if (this.level().getBlockStates(this.getBoundingBox()).noneMatch(BlockBase.BlockData::isAir)) {
            this.discard();
        } else if (this.isInWaterOrBubble()) {
            this.discard();
        } else {
            this.setDeltaMovement(vec3d.scale(0.9900000095367432D));
            this.applyGravity();
            this.setPos(d0, d1, d2);
        }
    }

    @Override
    protected void onHitEntity(MovingObjectPositionEntity movingobjectpositionentity) {
        super.onHitEntity(movingobjectpositionentity);
        Entity entity = this.getOwner();

        if (entity instanceof EntityLiving entityliving) {
            movingobjectpositionentity.getEntity().hurt(this.damageSources().spit(this, entityliving), 1.0F);
        }

    }

    @Override
    protected void onHitBlock(MovingObjectPositionBlock movingobjectpositionblock) {
        super.onHitBlock(movingobjectpositionblock);
        if (!this.level().isClientSide) {
            this.discard();
        }

    }

    @Override
    protected void defineSynchedData(DataWatcher.a datawatcher_a) {}

    @Override
    public void recreateFromPacket(PacketPlayOutSpawnEntity packetplayoutspawnentity) {
        super.recreateFromPacket(packetplayoutspawnentity);
        double d0 = packetplayoutspawnentity.getXa();
        double d1 = packetplayoutspawnentity.getYa();
        double d2 = packetplayoutspawnentity.getZa();

        for (int i = 0; i < 7; ++i) {
            double d3 = 0.4D + 0.1D * (double) i;

            this.level().addParticle(Particles.SPIT, this.getX(), this.getY(), this.getZ(), d0 * d3, d1, d2 * d3);
        }

        this.setDeltaMovement(d0, d1, d2);
    }
}
