package net.minecraft.world.entity.raid;

import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.protocol.game.PacketDebug;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.tags.PoiTypeTags;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.ai.village.poi.VillagePlace;
import net.minecraft.world.entity.ai.village.poi.VillagePlaceRecord;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionManager;
import net.minecraft.world.level.saveddata.PersistentBase;
import net.minecraft.world.phys.Vec3D;

public class PersistentRaid extends PersistentBase {

    private static final String RAID_FILE_ID = "raids";
    public final Map<Integer, Raid> raidMap = Maps.newHashMap();
    private final WorldServer level;
    private int nextAvailableID;
    private int tick;

    public static PersistentBase.a<PersistentRaid> factory(WorldServer worldserver) {
        return new PersistentBase.a<>(() -> {
            return new PersistentRaid(worldserver);
        }, (nbttagcompound, holderlookup_a) -> {
            return load(worldserver, nbttagcompound);
        }, DataFixTypes.SAVED_DATA_RAIDS);
    }

    public PersistentRaid(WorldServer worldserver) {
        this.level = worldserver;
        this.nextAvailableID = 1;
        this.setDirty();
    }

    public Raid get(int i) {
        return (Raid) this.raidMap.get(i);
    }

    public void tick() {
        ++this.tick;
        Iterator<Raid> iterator = this.raidMap.values().iterator();

        while (iterator.hasNext()) {
            Raid raid = (Raid) iterator.next();

            if (this.level.getGameRules().getBoolean(GameRules.RULE_DISABLE_RAIDS)) {
                raid.stop();
            }

            if (raid.isStopped()) {
                iterator.remove();
                this.setDirty();
            } else {
                raid.tick();
            }
        }

        if (this.tick % 200 == 0) {
            this.setDirty();
        }

        PacketDebug.sendRaids(this.level, this.raidMap.values());
    }

    public static boolean canJoinRaid(EntityRaider entityraider, Raid raid) {
        return entityraider != null && raid != null && raid.getLevel() != null ? entityraider.isAlive() && entityraider.canJoinRaid() && entityraider.getNoActionTime() <= 2400 && entityraider.level().dimensionType() == raid.getLevel().dimensionType() : false;
    }

    @Nullable
    public Raid createOrExtendRaid(EntityPlayer entityplayer, BlockPosition blockposition) {
        if (entityplayer.isSpectator()) {
            return null;
        } else if (this.level.getGameRules().getBoolean(GameRules.RULE_DISABLE_RAIDS)) {
            return null;
        } else {
            DimensionManager dimensionmanager = entityplayer.level().dimensionType();

            if (!dimensionmanager.hasRaids()) {
                return null;
            } else {
                List<VillagePlaceRecord> list = this.level.getPoiManager().getInRange((holder) -> {
                    return holder.is(PoiTypeTags.VILLAGE);
                }, blockposition, 64, VillagePlace.Occupancy.IS_OCCUPIED).toList();
                int i = 0;
                Vec3D vec3d = Vec3D.ZERO;

                for (Iterator iterator = list.iterator(); iterator.hasNext(); ++i) {
                    VillagePlaceRecord villageplacerecord = (VillagePlaceRecord) iterator.next();
                    BlockPosition blockposition1 = villageplacerecord.getPos();

                    vec3d = vec3d.add((double) blockposition1.getX(), (double) blockposition1.getY(), (double) blockposition1.getZ());
                }

                BlockPosition blockposition2;

                if (i > 0) {
                    vec3d = vec3d.scale(1.0D / (double) i);
                    blockposition2 = BlockPosition.containing(vec3d);
                } else {
                    blockposition2 = blockposition;
                }

                Raid raid = this.getOrCreateRaid(entityplayer.serverLevel(), blockposition2);

                if (!raid.isStarted() && !this.raidMap.containsKey(raid.getId())) {
                    this.raidMap.put(raid.getId(), raid);
                }

                if (!raid.isStarted() || raid.getRaidOmenLevel() < raid.getMaxRaidOmenLevel()) {
                    raid.absorbRaidOmen(entityplayer);
                }

                this.setDirty();
                return raid;
            }
        }
    }

    private Raid getOrCreateRaid(WorldServer worldserver, BlockPosition blockposition) {
        Raid raid = worldserver.getRaidAt(blockposition);

        return raid != null ? raid : new Raid(this.getUniqueId(), worldserver, blockposition);
    }

    public static PersistentRaid load(WorldServer worldserver, NBTTagCompound nbttagcompound) {
        PersistentRaid persistentraid = new PersistentRaid(worldserver);

        persistentraid.nextAvailableID = nbttagcompound.getInt("NextAvailableID");
        persistentraid.tick = nbttagcompound.getInt("Tick");
        NBTTagList nbttaglist = nbttagcompound.getList("Raids", 10);

        for (int i = 0; i < nbttaglist.size(); ++i) {
            NBTTagCompound nbttagcompound1 = nbttaglist.getCompound(i);
            Raid raid = new Raid(worldserver, nbttagcompound1);

            persistentraid.raidMap.put(raid.getId(), raid);
        }

        return persistentraid;
    }

    @Override
    public NBTTagCompound save(NBTTagCompound nbttagcompound, HolderLookup.a holderlookup_a) {
        nbttagcompound.putInt("NextAvailableID", this.nextAvailableID);
        nbttagcompound.putInt("Tick", this.tick);
        NBTTagList nbttaglist = new NBTTagList();
        Iterator iterator = this.raidMap.values().iterator();

        while (iterator.hasNext()) {
            Raid raid = (Raid) iterator.next();
            NBTTagCompound nbttagcompound1 = new NBTTagCompound();

            raid.save(nbttagcompound1);
            nbttaglist.add(nbttagcompound1);
        }

        nbttagcompound.put("Raids", nbttaglist);
        return nbttagcompound;
    }

    public static String getFileId(Holder<DimensionManager> holder) {
        return holder.is(BuiltinDimensionTypes.END) ? "raids_end" : "raids";
    }

    private int getUniqueId() {
        return ++this.nextAvailableID;
    }

    @Nullable
    public Raid getNearbyRaid(BlockPosition blockposition, int i) {
        Raid raid = null;
        double d0 = (double) i;
        Iterator iterator = this.raidMap.values().iterator();

        while (iterator.hasNext()) {
            Raid raid1 = (Raid) iterator.next();
            double d1 = raid1.getCenter().distSqr(blockposition);

            if (raid1.isActive() && d1 < d0) {
                raid = raid1;
                d0 = d1;
            }
        }

        return raid;
    }
}
