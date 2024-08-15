package net.minecraft.world.entity;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.world.level.IEntityAccess;

public interface OwnableEntity {

    @Nullable
    UUID getOwnerUUID();

    IEntityAccess level();

    @Nullable
    default EntityLiving getOwner() {
        UUID uuid = this.getOwnerUUID();

        return uuid == null ? null : this.level().getPlayerByUUID(uuid);
    }
}
