package net.minecraft.world.entity.ai.behavior;

import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.SectionPosition;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityCreature;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.BehaviorController;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryTarget;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemProjectileWeapon;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.pathfinder.PathMode;
import net.minecraft.world.phys.Vec3D;

public class BehaviorUtil {

    private BehaviorUtil() {}

    public static void lockGazeAndWalkToEachOther(EntityLiving entityliving, EntityLiving entityliving1, float f, int i) {
        lookAtEachOther(entityliving, entityliving1);
        setWalkAndLookTargetMemoriesToEachOther(entityliving, entityliving1, f, i);
    }

    public static boolean entityIsVisible(BehaviorController<?> behaviorcontroller, EntityLiving entityliving) {
        Optional<NearestVisibleLivingEntities> optional = behaviorcontroller.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);

        return optional.isPresent() && ((NearestVisibleLivingEntities) optional.get()).contains(entityliving);
    }

    public static boolean targetIsValid(BehaviorController<?> behaviorcontroller, MemoryModuleType<? extends EntityLiving> memorymoduletype, EntityTypes<?> entitytypes) {
        return targetIsValid(behaviorcontroller, memorymoduletype, (entityliving) -> {
            return entityliving.getType() == entitytypes;
        });
    }

    private static boolean targetIsValid(BehaviorController<?> behaviorcontroller, MemoryModuleType<? extends EntityLiving> memorymoduletype, Predicate<EntityLiving> predicate) {
        return behaviorcontroller.getMemory(memorymoduletype).filter(predicate).filter(EntityLiving::isAlive).filter((entityliving) -> {
            return entityIsVisible(behaviorcontroller, entityliving);
        }).isPresent();
    }

    private static void lookAtEachOther(EntityLiving entityliving, EntityLiving entityliving1) {
        lookAtEntity(entityliving, entityliving1);
        lookAtEntity(entityliving1, entityliving);
    }

    public static void lookAtEntity(EntityLiving entityliving, EntityLiving entityliving1) {
        entityliving.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, (Object) (new BehaviorPositionEntity(entityliving1, true)));
    }

    private static void setWalkAndLookTargetMemoriesToEachOther(EntityLiving entityliving, EntityLiving entityliving1, float f, int i) {
        setWalkAndLookTargetMemories(entityliving, (Entity) entityliving1, f, i);
        setWalkAndLookTargetMemories(entityliving1, (Entity) entityliving, f, i);
    }

    public static void setWalkAndLookTargetMemories(EntityLiving entityliving, Entity entity, float f, int i) {
        setWalkAndLookTargetMemories(entityliving, (BehaviorPosition) (new BehaviorPositionEntity(entity, true)), f, i);
    }

    public static void setWalkAndLookTargetMemories(EntityLiving entityliving, BlockPosition blockposition, float f, int i) {
        setWalkAndLookTargetMemories(entityliving, (BehaviorPosition) (new BehaviorTarget(blockposition)), f, i);
    }

    public static void setWalkAndLookTargetMemories(EntityLiving entityliving, BehaviorPosition behaviorposition, float f, int i) {
        MemoryTarget memorytarget = new MemoryTarget(behaviorposition, f, i);

        entityliving.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, (Object) behaviorposition);
        entityliving.getBrain().setMemory(MemoryModuleType.WALK_TARGET, (Object) memorytarget);
    }

    public static void throwItem(EntityLiving entityliving, ItemStack itemstack, Vec3D vec3d) {
        Vec3D vec3d1 = new Vec3D(0.30000001192092896D, 0.30000001192092896D, 0.30000001192092896D);

        throwItem(entityliving, itemstack, vec3d, vec3d1, 0.3F);
    }

    public static void throwItem(EntityLiving entityliving, ItemStack itemstack, Vec3D vec3d, Vec3D vec3d1, float f) {
        double d0 = entityliving.getEyeY() - (double) f;
        EntityItem entityitem = new EntityItem(entityliving.level(), entityliving.getX(), d0, entityliving.getZ(), itemstack);

        entityitem.setThrower(entityliving);
        Vec3D vec3d2 = vec3d.subtract(entityliving.position());

        vec3d2 = vec3d2.normalize().multiply(vec3d1.x, vec3d1.y, vec3d1.z);
        entityitem.setDeltaMovement(vec3d2);
        entityitem.setDefaultPickUpDelay();
        entityliving.level().addFreshEntity(entityitem);
    }

    public static SectionPosition findSectionClosestToVillage(WorldServer worldserver, SectionPosition sectionposition, int i) {
        int j = worldserver.sectionsToVillage(sectionposition);
        Stream stream = SectionPosition.cube(sectionposition, i).filter((sectionposition1) -> {
            return worldserver.sectionsToVillage(sectionposition1) < j;
        });

        Objects.requireNonNull(worldserver);
        return (SectionPosition) stream.min(Comparator.comparingInt(worldserver::sectionsToVillage)).orElse(sectionposition);
    }

    public static boolean isWithinAttackRange(EntityInsentient entityinsentient, EntityLiving entityliving, int i) {
        Item item = entityinsentient.getMainHandItem().getItem();

        if (item instanceof ItemProjectileWeapon itemprojectileweapon) {
            if (entityinsentient.canFireProjectileWeapon(itemprojectileweapon)) {
                int j = itemprojectileweapon.getDefaultProjectileRange() - i;

                return entityinsentient.closerThan(entityliving, (double) j);
            }
        }

        return entityinsentient.isWithinMeleeAttackRange(entityliving);
    }

    public static boolean isOtherTargetMuchFurtherAwayThanCurrentAttackTarget(EntityLiving entityliving, EntityLiving entityliving1, double d0) {
        Optional<EntityLiving> optional = entityliving.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET);

        if (optional.isEmpty()) {
            return false;
        } else {
            double d1 = entityliving.distanceToSqr(((EntityLiving) optional.get()).position());
            double d2 = entityliving.distanceToSqr(entityliving1.position());

            return d2 > d1 + d0 * d0;
        }
    }

    public static boolean canSee(EntityLiving entityliving, EntityLiving entityliving1) {
        BehaviorController<?> behaviorcontroller = entityliving.getBrain();

        return !behaviorcontroller.hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES) ? false : ((NearestVisibleLivingEntities) behaviorcontroller.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).get()).contains(entityliving1);
    }

    public static EntityLiving getNearestTarget(EntityLiving entityliving, Optional<EntityLiving> optional, EntityLiving entityliving1) {
        return optional.isEmpty() ? entityliving1 : getTargetNearestMe(entityliving, (EntityLiving) optional.get(), entityliving1);
    }

    public static EntityLiving getTargetNearestMe(EntityLiving entityliving, EntityLiving entityliving1, EntityLiving entityliving2) {
        Vec3D vec3d = entityliving1.position();
        Vec3D vec3d1 = entityliving2.position();

        return entityliving.distanceToSqr(vec3d) < entityliving.distanceToSqr(vec3d1) ? entityliving1 : entityliving2;
    }

    public static Optional<EntityLiving> getLivingEntityFromUUIDMemory(EntityLiving entityliving, MemoryModuleType<UUID> memorymoduletype) {
        Optional<UUID> optional = entityliving.getBrain().getMemory(memorymoduletype);

        return optional.map((uuid) -> {
            return ((WorldServer) entityliving.level()).getEntity(uuid);
        }).map((entity) -> {
            EntityLiving entityliving1;

            if (entity instanceof EntityLiving entityliving2) {
                entityliving1 = entityliving2;
            } else {
                entityliving1 = null;
            }

            return entityliving1;
        });
    }

    @Nullable
    public static Vec3D getRandomSwimmablePos(EntityCreature entitycreature, int i, int j) {
        Vec3D vec3d = DefaultRandomPos.getPos(entitycreature, i, j);

        for (int k = 0; vec3d != null && !entitycreature.level().getBlockState(BlockPosition.containing(vec3d)).isPathfindable(PathMode.WATER) && k++ < 10; vec3d = DefaultRandomPos.getPos(entitycreature, i, j)) {
            ;
        }

        return vec3d;
    }

    public static boolean isBreeding(EntityLiving entityliving) {
        return entityliving.getBrain().hasMemoryValue(MemoryModuleType.BREED_TARGET);
    }
}
