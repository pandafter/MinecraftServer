package net.minecraft.world.entity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketListenerPlayOut;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.world.level.World;
import net.minecraft.world.level.material.EnumPistonReaction;

public class Marker extends Entity {

    private static final String DATA_TAG = "data";
    private NBTTagCompound data = new NBTTagCompound();

    public Marker(EntityTypes<?> entitytypes, World world) {
        super(entitytypes, world);
        this.noPhysics = true;
    }

    @Override
    public void tick() {}

    @Override
    protected void defineSynchedData(DataWatcher.a datawatcher_a) {}

    @Override
    protected void readAdditionalSaveData(NBTTagCompound nbttagcompound) {
        this.data = nbttagcompound.getCompound("data");
    }

    @Override
    protected void addAdditionalSaveData(NBTTagCompound nbttagcompound) {
        nbttagcompound.put("data", this.data.copy());
    }

    @Override
    public Packet<PacketListenerPlayOut> getAddEntityPacket() {
        throw new IllegalStateException("Markers should never be sent");
    }

    @Override
    protected boolean canAddPassenger(Entity entity) {
        return false;
    }

    @Override
    protected boolean couldAcceptPassenger() {
        return false;
    }

    @Override
    protected void addPassenger(Entity entity) {
        throw new IllegalStateException("Should never addPassenger without checking couldAcceptPassenger()");
    }

    @Override
    public EnumPistonReaction getPistonPushReaction() {
        return EnumPistonReaction.IGNORE;
    }

    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }
}
