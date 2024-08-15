package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.commands.arguments.ArgumentNBTTag;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.coordinates.ArgumentVec3;
import net.minecraft.commands.synchronization.CompletionProviders;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EnumMobSpawn;
import net.minecraft.world.entity.GroupDataEntity;
import net.minecraft.world.level.World;
import net.minecraft.world.phys.Vec3D;

public class CommandSummon {

    private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(IChatBaseComponent.translatable("commands.summon.failed"));
    private static final SimpleCommandExceptionType ERROR_DUPLICATE_UUID = new SimpleCommandExceptionType(IChatBaseComponent.translatable("commands.summon.failed.uuid"));
    private static final SimpleCommandExceptionType INVALID_POSITION = new SimpleCommandExceptionType(IChatBaseComponent.translatable("commands.summon.invalidPosition"));

    public CommandSummon() {}

    public static void register(CommandDispatcher<CommandListenerWrapper> commanddispatcher, CommandBuildContext commandbuildcontext) {
        commanddispatcher.register((LiteralArgumentBuilder) ((LiteralArgumentBuilder) net.minecraft.commands.CommandDispatcher.literal("summon").requires((commandlistenerwrapper) -> {
            return commandlistenerwrapper.hasPermission(2);
        })).then(((RequiredArgumentBuilder) net.minecraft.commands.CommandDispatcher.argument("entity", ResourceArgument.resource(commandbuildcontext, Registries.ENTITY_TYPE)).suggests(CompletionProviders.SUMMONABLE_ENTITIES).executes((commandcontext) -> {
            return spawnEntity((CommandListenerWrapper) commandcontext.getSource(), ResourceArgument.getSummonableEntityType(commandcontext, "entity"), ((CommandListenerWrapper) commandcontext.getSource()).getPosition(), new NBTTagCompound(), true);
        })).then(((RequiredArgumentBuilder) net.minecraft.commands.CommandDispatcher.argument("pos", ArgumentVec3.vec3()).executes((commandcontext) -> {
            return spawnEntity((CommandListenerWrapper) commandcontext.getSource(), ResourceArgument.getSummonableEntityType(commandcontext, "entity"), ArgumentVec3.getVec3(commandcontext, "pos"), new NBTTagCompound(), true);
        })).then(net.minecraft.commands.CommandDispatcher.argument("nbt", ArgumentNBTTag.compoundTag()).executes((commandcontext) -> {
            return spawnEntity((CommandListenerWrapper) commandcontext.getSource(), ResourceArgument.getSummonableEntityType(commandcontext, "entity"), ArgumentVec3.getVec3(commandcontext, "pos"), ArgumentNBTTag.getCompoundTag(commandcontext, "nbt"), false);
        })))));
    }

    public static Entity createEntity(CommandListenerWrapper commandlistenerwrapper, Holder.c<EntityTypes<?>> holder_c, Vec3D vec3d, NBTTagCompound nbttagcompound, boolean flag) throws CommandSyntaxException {
        BlockPosition blockposition = BlockPosition.containing(vec3d);

        if (!World.isInSpawnableBounds(blockposition)) {
            throw CommandSummon.INVALID_POSITION.create();
        } else {
            NBTTagCompound nbttagcompound1 = nbttagcompound.copy();

            nbttagcompound1.putString("id", holder_c.key().location().toString());
            WorldServer worldserver = commandlistenerwrapper.getLevel();
            Entity entity = EntityTypes.loadEntityRecursive(nbttagcompound1, worldserver, (entity1) -> {
                entity1.moveTo(vec3d.x, vec3d.y, vec3d.z, entity1.getYRot(), entity1.getXRot());
                return entity1;
            });

            if (entity == null) {
                throw CommandSummon.ERROR_FAILED.create();
            } else {
                if (flag && entity instanceof EntityInsentient) {
                    ((EntityInsentient) entity).finalizeSpawn(commandlistenerwrapper.getLevel(), commandlistenerwrapper.getLevel().getCurrentDifficultyAt(entity.blockPosition()), EnumMobSpawn.COMMAND, (GroupDataEntity) null);
                }

                if (!worldserver.tryAddFreshEntityWithPassengers(entity)) {
                    throw CommandSummon.ERROR_DUPLICATE_UUID.create();
                } else {
                    return entity;
                }
            }
        }
    }

    private static int spawnEntity(CommandListenerWrapper commandlistenerwrapper, Holder.c<EntityTypes<?>> holder_c, Vec3D vec3d, NBTTagCompound nbttagcompound, boolean flag) throws CommandSyntaxException {
        Entity entity = createEntity(commandlistenerwrapper, holder_c, vec3d, nbttagcompound, flag);

        commandlistenerwrapper.sendSuccess(() -> {
            return IChatBaseComponent.translatable("commands.summon.success", entity.getDisplayName());
        }, true);
        return 1;
    }
}
