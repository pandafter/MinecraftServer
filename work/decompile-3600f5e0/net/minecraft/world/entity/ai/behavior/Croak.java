package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.EntityPose;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.frog.Frog;

public class Croak extends Behavior<Frog> {

    private static final int CROAK_TICKS = 60;
    private static final int TIME_OUT_DURATION = 100;
    private int croakCounter;

    public Croak() {
        super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT), 100);
    }

    protected boolean checkExtraStartConditions(WorldServer worldserver, Frog frog) {
        return frog.getPose() == EntityPose.STANDING;
    }

    protected boolean canStillUse(WorldServer worldserver, Frog frog, long i) {
        return this.croakCounter < 60;
    }

    protected void start(WorldServer worldserver, Frog frog, long i) {
        if (!frog.isInLiquid()) {
            frog.setPose(EntityPose.CROAKING);
            this.croakCounter = 0;
        }
    }

    protected void stop(WorldServer worldserver, Frog frog, long i) {
        frog.setPose(EntityPose.STANDING);
    }

    protected void tick(WorldServer worldserver, Frog frog, long i) {
        ++this.croakCounter;
    }
}
