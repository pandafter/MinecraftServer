package net.minecraft.world.entity.ai.behavior;

import java.util.Iterator;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.tags.TagsFluid;
import net.minecraft.world.entity.EntityCreature;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryTarget;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;
import org.apache.commons.lang3.mutable.MutableLong;

public class TryFindLand {

    private static final int COOLDOWN_TICKS = 60;

    public TryFindLand() {}

    public static BehaviorControl<EntityCreature> create(int i, float f) {
        MutableLong mutablelong = new MutableLong(0L);

        return BehaviorBuilder.create((behaviorbuilder_b) -> {
            return behaviorbuilder_b.group(behaviorbuilder_b.absent(MemoryModuleType.ATTACK_TARGET), behaviorbuilder_b.absent(MemoryModuleType.WALK_TARGET), behaviorbuilder_b.registered(MemoryModuleType.LOOK_TARGET)).apply(behaviorbuilder_b, (memoryaccessor, memoryaccessor1, memoryaccessor2) -> {
                return (worldserver, entitycreature, j) -> {
                    if (!worldserver.getFluidState(entitycreature.blockPosition()).is(TagsFluid.WATER)) {
                        return false;
                    } else if (j < mutablelong.getValue()) {
                        mutablelong.setValue(j + 60L);
                        return true;
                    } else {
                        BlockPosition blockposition = entitycreature.blockPosition();
                        BlockPosition.MutableBlockPosition blockposition_mutableblockposition = new BlockPosition.MutableBlockPosition();
                        VoxelShapeCollision voxelshapecollision = VoxelShapeCollision.of(entitycreature);
                        Iterator iterator = BlockPosition.withinManhattan(blockposition, i, i, i).iterator();

                        while (iterator.hasNext()) {
                            BlockPosition blockposition1 = (BlockPosition) iterator.next();

                            if (blockposition1.getX() != blockposition.getX() || blockposition1.getZ() != blockposition.getZ()) {
                                IBlockData iblockdata = worldserver.getBlockState(blockposition1);
                                IBlockData iblockdata1 = worldserver.getBlockState(blockposition_mutableblockposition.setWithOffset(blockposition1, EnumDirection.DOWN));

                                if (!iblockdata.is(Blocks.WATER) && worldserver.getFluidState(blockposition1).isEmpty() && iblockdata.getCollisionShape(worldserver, blockposition1, voxelshapecollision).isEmpty() && iblockdata1.isFaceSturdy(worldserver, blockposition_mutableblockposition, EnumDirection.UP)) {
                                    BlockPosition blockposition2 = blockposition1.immutable();

                                    memoryaccessor2.set(new BehaviorTarget(blockposition2));
                                    memoryaccessor1.set(new MemoryTarget(new BehaviorTarget(blockposition2), f, 1));
                                    break;
                                }
                            }
                        }

                        mutablelong.setValue(j + 60L);
                        return true;
                    }
                };
            });
        });
    }
}
