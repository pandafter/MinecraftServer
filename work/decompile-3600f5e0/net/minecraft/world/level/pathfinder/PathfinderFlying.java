package net.minecraft.world.level.pathfinder;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.util.MathHelper;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.level.ChunkCache;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.phys.AxisAlignedBB;

public class PathfinderFlying extends PathfinderNormal {

    private final Long2ObjectMap<PathType> pathTypeByPosCache = new Long2ObjectOpenHashMap();
    private static final float SMALL_MOB_SIZE = 1.0F;
    private static final float SMALL_MOB_INFLATED_START_NODE_BOUNDING_BOX = 1.1F;
    private static final int MAX_START_NODE_CANDIDATES = 10;

    public PathfinderFlying() {}

    @Override
    public void prepare(ChunkCache chunkcache, EntityInsentient entityinsentient) {
        super.prepare(chunkcache, entityinsentient);
        this.pathTypeByPosCache.clear();
        entityinsentient.onPathfindingStart();
    }

    @Override
    public void done() {
        this.mob.onPathfindingDone();
        this.pathTypeByPosCache.clear();
        super.done();
    }

    @Override
    public PathPoint getStart() {
        int i;

        if (this.canFloat() && this.mob.isInWater()) {
            i = this.mob.getBlockY();
            BlockPosition.MutableBlockPosition blockposition_mutableblockposition = new BlockPosition.MutableBlockPosition(this.mob.getX(), (double) i, this.mob.getZ());

            for (IBlockData iblockdata = this.currentContext.getBlockState(blockposition_mutableblockposition); iblockdata.is(Blocks.WATER); iblockdata = this.currentContext.getBlockState(blockposition_mutableblockposition)) {
                ++i;
                blockposition_mutableblockposition.set(this.mob.getX(), (double) i, this.mob.getZ());
            }
        } else {
            i = MathHelper.floor(this.mob.getY() + 0.5D);
        }

        BlockPosition blockposition = BlockPosition.containing(this.mob.getX(), (double) i, this.mob.getZ());

        if (!this.canStartAt(blockposition)) {
            Iterator iterator = this.iteratePathfindingStartNodeCandidatePositions(this.mob).iterator();

            while (iterator.hasNext()) {
                BlockPosition blockposition1 = (BlockPosition) iterator.next();

                if (this.canStartAt(blockposition1)) {
                    return super.getStartNode(blockposition1);
                }
            }
        }

        return super.getStartNode(blockposition);
    }

    @Override
    protected boolean canStartAt(BlockPosition blockposition) {
        PathType pathtype = this.getCachedPathType(blockposition.getX(), blockposition.getY(), blockposition.getZ());

        return this.mob.getPathfindingMalus(pathtype) >= 0.0F;
    }

    @Override
    public PathDestination getTarget(double d0, double d1, double d2) {
        return this.getTargetNodeAt(d0, d1, d2);
    }

    @Override
    public int getNeighbors(PathPoint[] apathpoint, PathPoint pathpoint) {
        int i = 0;
        PathPoint pathpoint1 = this.findAcceptedNode(pathpoint.x, pathpoint.y, pathpoint.z + 1);

        if (this.isOpen(pathpoint1)) {
            apathpoint[i++] = pathpoint1;
        }

        PathPoint pathpoint2 = this.findAcceptedNode(pathpoint.x - 1, pathpoint.y, pathpoint.z);

        if (this.isOpen(pathpoint2)) {
            apathpoint[i++] = pathpoint2;
        }

        PathPoint pathpoint3 = this.findAcceptedNode(pathpoint.x + 1, pathpoint.y, pathpoint.z);

        if (this.isOpen(pathpoint3)) {
            apathpoint[i++] = pathpoint3;
        }

        PathPoint pathpoint4 = this.findAcceptedNode(pathpoint.x, pathpoint.y, pathpoint.z - 1);

        if (this.isOpen(pathpoint4)) {
            apathpoint[i++] = pathpoint4;
        }

        PathPoint pathpoint5 = this.findAcceptedNode(pathpoint.x, pathpoint.y + 1, pathpoint.z);

        if (this.isOpen(pathpoint5)) {
            apathpoint[i++] = pathpoint5;
        }

        PathPoint pathpoint6 = this.findAcceptedNode(pathpoint.x, pathpoint.y - 1, pathpoint.z);

        if (this.isOpen(pathpoint6)) {
            apathpoint[i++] = pathpoint6;
        }

        PathPoint pathpoint7 = this.findAcceptedNode(pathpoint.x, pathpoint.y + 1, pathpoint.z + 1);

        if (this.isOpen(pathpoint7) && this.hasMalus(pathpoint1) && this.hasMalus(pathpoint5)) {
            apathpoint[i++] = pathpoint7;
        }

        PathPoint pathpoint8 = this.findAcceptedNode(pathpoint.x - 1, pathpoint.y + 1, pathpoint.z);

        if (this.isOpen(pathpoint8) && this.hasMalus(pathpoint2) && this.hasMalus(pathpoint5)) {
            apathpoint[i++] = pathpoint8;
        }

        PathPoint pathpoint9 = this.findAcceptedNode(pathpoint.x + 1, pathpoint.y + 1, pathpoint.z);

        if (this.isOpen(pathpoint9) && this.hasMalus(pathpoint3) && this.hasMalus(pathpoint5)) {
            apathpoint[i++] = pathpoint9;
        }

        PathPoint pathpoint10 = this.findAcceptedNode(pathpoint.x, pathpoint.y + 1, pathpoint.z - 1);

        if (this.isOpen(pathpoint10) && this.hasMalus(pathpoint4) && this.hasMalus(pathpoint5)) {
            apathpoint[i++] = pathpoint10;
        }

        PathPoint pathpoint11 = this.findAcceptedNode(pathpoint.x, pathpoint.y - 1, pathpoint.z + 1);

        if (this.isOpen(pathpoint11) && this.hasMalus(pathpoint1) && this.hasMalus(pathpoint6)) {
            apathpoint[i++] = pathpoint11;
        }

        PathPoint pathpoint12 = this.findAcceptedNode(pathpoint.x - 1, pathpoint.y - 1, pathpoint.z);

        if (this.isOpen(pathpoint12) && this.hasMalus(pathpoint2) && this.hasMalus(pathpoint6)) {
            apathpoint[i++] = pathpoint12;
        }

        PathPoint pathpoint13 = this.findAcceptedNode(pathpoint.x + 1, pathpoint.y - 1, pathpoint.z);

        if (this.isOpen(pathpoint13) && this.hasMalus(pathpoint3) && this.hasMalus(pathpoint6)) {
            apathpoint[i++] = pathpoint13;
        }

        PathPoint pathpoint14 = this.findAcceptedNode(pathpoint.x, pathpoint.y - 1, pathpoint.z - 1);

        if (this.isOpen(pathpoint14) && this.hasMalus(pathpoint4) && this.hasMalus(pathpoint6)) {
            apathpoint[i++] = pathpoint14;
        }

        PathPoint pathpoint15 = this.findAcceptedNode(pathpoint.x + 1, pathpoint.y, pathpoint.z - 1);

        if (this.isOpen(pathpoint15) && this.hasMalus(pathpoint4) && this.hasMalus(pathpoint3)) {
            apathpoint[i++] = pathpoint15;
        }

        PathPoint pathpoint16 = this.findAcceptedNode(pathpoint.x + 1, pathpoint.y, pathpoint.z + 1);

        if (this.isOpen(pathpoint16) && this.hasMalus(pathpoint1) && this.hasMalus(pathpoint3)) {
            apathpoint[i++] = pathpoint16;
        }

        PathPoint pathpoint17 = this.findAcceptedNode(pathpoint.x - 1, pathpoint.y, pathpoint.z - 1);

        if (this.isOpen(pathpoint17) && this.hasMalus(pathpoint4) && this.hasMalus(pathpoint2)) {
            apathpoint[i++] = pathpoint17;
        }

        PathPoint pathpoint18 = this.findAcceptedNode(pathpoint.x - 1, pathpoint.y, pathpoint.z + 1);

        if (this.isOpen(pathpoint18) && this.hasMalus(pathpoint1) && this.hasMalus(pathpoint2)) {
            apathpoint[i++] = pathpoint18;
        }

        PathPoint pathpoint19 = this.findAcceptedNode(pathpoint.x + 1, pathpoint.y + 1, pathpoint.z - 1);

        if (this.isOpen(pathpoint19) && this.hasMalus(pathpoint15) && this.hasMalus(pathpoint4) && this.hasMalus(pathpoint3) && this.hasMalus(pathpoint5) && this.hasMalus(pathpoint10) && this.hasMalus(pathpoint9)) {
            apathpoint[i++] = pathpoint19;
        }

        PathPoint pathpoint20 = this.findAcceptedNode(pathpoint.x + 1, pathpoint.y + 1, pathpoint.z + 1);

        if (this.isOpen(pathpoint20) && this.hasMalus(pathpoint16) && this.hasMalus(pathpoint1) && this.hasMalus(pathpoint3) && this.hasMalus(pathpoint5) && this.hasMalus(pathpoint7) && this.hasMalus(pathpoint9)) {
            apathpoint[i++] = pathpoint20;
        }

        PathPoint pathpoint21 = this.findAcceptedNode(pathpoint.x - 1, pathpoint.y + 1, pathpoint.z - 1);

        if (this.isOpen(pathpoint21) && this.hasMalus(pathpoint17) && this.hasMalus(pathpoint4) && this.hasMalus(pathpoint2) && this.hasMalus(pathpoint5) && this.hasMalus(pathpoint10) && this.hasMalus(pathpoint8)) {
            apathpoint[i++] = pathpoint21;
        }

        PathPoint pathpoint22 = this.findAcceptedNode(pathpoint.x - 1, pathpoint.y + 1, pathpoint.z + 1);

        if (this.isOpen(pathpoint22) && this.hasMalus(pathpoint18) && this.hasMalus(pathpoint1) && this.hasMalus(pathpoint2) && this.hasMalus(pathpoint5) && this.hasMalus(pathpoint7) && this.hasMalus(pathpoint8)) {
            apathpoint[i++] = pathpoint22;
        }

        PathPoint pathpoint23 = this.findAcceptedNode(pathpoint.x + 1, pathpoint.y - 1, pathpoint.z - 1);

        if (this.isOpen(pathpoint23) && this.hasMalus(pathpoint15) && this.hasMalus(pathpoint4) && this.hasMalus(pathpoint3) && this.hasMalus(pathpoint6) && this.hasMalus(pathpoint14) && this.hasMalus(pathpoint13)) {
            apathpoint[i++] = pathpoint23;
        }

        PathPoint pathpoint24 = this.findAcceptedNode(pathpoint.x + 1, pathpoint.y - 1, pathpoint.z + 1);

        if (this.isOpen(pathpoint24) && this.hasMalus(pathpoint16) && this.hasMalus(pathpoint1) && this.hasMalus(pathpoint3) && this.hasMalus(pathpoint6) && this.hasMalus(pathpoint11) && this.hasMalus(pathpoint13)) {
            apathpoint[i++] = pathpoint24;
        }

        PathPoint pathpoint25 = this.findAcceptedNode(pathpoint.x - 1, pathpoint.y - 1, pathpoint.z - 1);

        if (this.isOpen(pathpoint25) && this.hasMalus(pathpoint17) && this.hasMalus(pathpoint4) && this.hasMalus(pathpoint2) && this.hasMalus(pathpoint6) && this.hasMalus(pathpoint14) && this.hasMalus(pathpoint12)) {
            apathpoint[i++] = pathpoint25;
        }

        PathPoint pathpoint26 = this.findAcceptedNode(pathpoint.x - 1, pathpoint.y - 1, pathpoint.z + 1);

        if (this.isOpen(pathpoint26) && this.hasMalus(pathpoint18) && this.hasMalus(pathpoint1) && this.hasMalus(pathpoint2) && this.hasMalus(pathpoint6) && this.hasMalus(pathpoint11) && this.hasMalus(pathpoint12)) {
            apathpoint[i++] = pathpoint26;
        }

        return i;
    }

    private boolean hasMalus(@Nullable PathPoint pathpoint) {
        return pathpoint != null && pathpoint.costMalus >= 0.0F;
    }

    private boolean isOpen(@Nullable PathPoint pathpoint) {
        return pathpoint != null && !pathpoint.closed;
    }

    @Nullable
    protected PathPoint findAcceptedNode(int i, int j, int k) {
        PathPoint pathpoint = null;
        PathType pathtype = this.getCachedPathType(i, j, k);
        float f = this.mob.getPathfindingMalus(pathtype);

        if (f >= 0.0F) {
            pathpoint = this.getNode(i, j, k);
            pathpoint.type = pathtype;
            pathpoint.costMalus = Math.max(pathpoint.costMalus, f);
            if (pathtype == PathType.WALKABLE) {
                ++pathpoint.costMalus;
            }
        }

        return pathpoint;
    }

    @Override
    protected PathType getCachedPathType(int i, int j, int k) {
        return (PathType) this.pathTypeByPosCache.computeIfAbsent(BlockPosition.asLong(i, j, k), (l) -> {
            return this.getPathTypeOfMob(this.currentContext, i, j, k, this.mob);
        });
    }

    @Override
    public PathType getPathType(PathfindingContext pathfindingcontext, int i, int j, int k) {
        PathType pathtype = pathfindingcontext.getPathTypeFromState(i, j, k);

        if (pathtype == PathType.OPEN && j >= pathfindingcontext.level().getMinBuildHeight() + 1) {
            BlockPosition blockposition = new BlockPosition(i, j - 1, k);
            PathType pathtype1 = pathfindingcontext.getPathTypeFromState(blockposition.getX(), blockposition.getY(), blockposition.getZ());

            if (pathtype1 != PathType.DAMAGE_FIRE && pathtype1 != PathType.LAVA) {
                if (pathtype1 == PathType.DAMAGE_OTHER) {
                    pathtype = PathType.DAMAGE_OTHER;
                } else if (pathtype1 == PathType.COCOA) {
                    pathtype = PathType.COCOA;
                } else if (pathtype1 == PathType.FENCE) {
                    if (!blockposition.equals(pathfindingcontext.mobPosition())) {
                        pathtype = PathType.FENCE;
                    }
                } else {
                    pathtype = pathtype1 != PathType.WALKABLE && pathtype1 != PathType.OPEN && pathtype1 != PathType.WATER ? PathType.WALKABLE : PathType.OPEN;
                }
            } else {
                pathtype = PathType.DAMAGE_FIRE;
            }
        }

        if (pathtype == PathType.WALKABLE || pathtype == PathType.OPEN) {
            pathtype = checkNeighbourBlocks(pathfindingcontext, i, j, k, pathtype);
        }

        return pathtype;
    }

    private Iterable<BlockPosition> iteratePathfindingStartNodeCandidatePositions(EntityInsentient entityinsentient) {
        AxisAlignedBB axisalignedbb = entityinsentient.getBoundingBox();
        boolean flag = axisalignedbb.getSize() < 1.0D;

        if (!flag) {
            return List.of(BlockPosition.containing(axisalignedbb.minX, (double) entityinsentient.getBlockY(), axisalignedbb.minZ), BlockPosition.containing(axisalignedbb.minX, (double) entityinsentient.getBlockY(), axisalignedbb.maxZ), BlockPosition.containing(axisalignedbb.maxX, (double) entityinsentient.getBlockY(), axisalignedbb.minZ), BlockPosition.containing(axisalignedbb.maxX, (double) entityinsentient.getBlockY(), axisalignedbb.maxZ));
        } else {
            double d0 = Math.max(0.0D, 1.100000023841858D - axisalignedbb.getZsize());
            double d1 = Math.max(0.0D, 1.100000023841858D - axisalignedbb.getXsize());
            double d2 = Math.max(0.0D, 1.100000023841858D - axisalignedbb.getYsize());
            AxisAlignedBB axisalignedbb1 = axisalignedbb.inflate(d1, d2, d0);

            return BlockPosition.randomBetweenClosed(entityinsentient.getRandom(), 10, MathHelper.floor(axisalignedbb1.minX), MathHelper.floor(axisalignedbb1.minY), MathHelper.floor(axisalignedbb1.minZ), MathHelper.floor(axisalignedbb1.maxX), MathHelper.floor(axisalignedbb1.maxY), MathHelper.floor(axisalignedbb1.maxZ));
        }
    }
}
