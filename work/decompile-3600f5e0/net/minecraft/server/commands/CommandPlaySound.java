package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.commands.arguments.ArgumentEntity;
import net.minecraft.commands.arguments.ArgumentMinecraftKeyRegistered;
import net.minecraft.commands.arguments.coordinates.ArgumentVec3;
import net.minecraft.commands.synchronization.CompletionProviders;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.PacketPlayOutNamedSoundEffect;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.util.MathHelper;
import net.minecraft.world.phys.Vec3D;

public class CommandPlaySound {

    private static final SimpleCommandExceptionType ERROR_TOO_FAR = new SimpleCommandExceptionType(IChatBaseComponent.translatable("commands.playsound.failed"));

    public CommandPlaySound() {}

    public static void register(CommandDispatcher<CommandListenerWrapper> commanddispatcher) {
        RequiredArgumentBuilder<CommandListenerWrapper, MinecraftKey> requiredargumentbuilder = (RequiredArgumentBuilder) net.minecraft.commands.CommandDispatcher.argument("sound", ArgumentMinecraftKeyRegistered.id()).suggests(CompletionProviders.AVAILABLE_SOUNDS).executes((commandcontext) -> {
            return playSound((CommandListenerWrapper) commandcontext.getSource(), getCallingPlayerAsCollection(((CommandListenerWrapper) commandcontext.getSource()).getPlayer()), ArgumentMinecraftKeyRegistered.getId(commandcontext, "sound"), SoundCategory.MASTER, ((CommandListenerWrapper) commandcontext.getSource()).getPosition(), 1.0F, 1.0F, 0.0F);
        });
        SoundCategory[] asoundcategory = SoundCategory.values();
        int i = asoundcategory.length;

        for (int j = 0; j < i; ++j) {
            SoundCategory soundcategory = asoundcategory[j];

            requiredargumentbuilder.then(source(soundcategory));
        }

        commanddispatcher.register((LiteralArgumentBuilder) ((LiteralArgumentBuilder) net.minecraft.commands.CommandDispatcher.literal("playsound").requires((commandlistenerwrapper) -> {
            return commandlistenerwrapper.hasPermission(2);
        })).then(requiredargumentbuilder));
    }

    private static LiteralArgumentBuilder<CommandListenerWrapper> source(SoundCategory soundcategory) {
        return (LiteralArgumentBuilder) ((LiteralArgumentBuilder) net.minecraft.commands.CommandDispatcher.literal(soundcategory.getName()).executes((commandcontext) -> {
            return playSound((CommandListenerWrapper) commandcontext.getSource(), getCallingPlayerAsCollection(((CommandListenerWrapper) commandcontext.getSource()).getPlayer()), ArgumentMinecraftKeyRegistered.getId(commandcontext, "sound"), soundcategory, ((CommandListenerWrapper) commandcontext.getSource()).getPosition(), 1.0F, 1.0F, 0.0F);
        })).then(((RequiredArgumentBuilder) net.minecraft.commands.CommandDispatcher.argument("targets", ArgumentEntity.players()).executes((commandcontext) -> {
            return playSound((CommandListenerWrapper) commandcontext.getSource(), ArgumentEntity.getPlayers(commandcontext, "targets"), ArgumentMinecraftKeyRegistered.getId(commandcontext, "sound"), soundcategory, ((CommandListenerWrapper) commandcontext.getSource()).getPosition(), 1.0F, 1.0F, 0.0F);
        })).then(((RequiredArgumentBuilder) net.minecraft.commands.CommandDispatcher.argument("pos", ArgumentVec3.vec3()).executes((commandcontext) -> {
            return playSound((CommandListenerWrapper) commandcontext.getSource(), ArgumentEntity.getPlayers(commandcontext, "targets"), ArgumentMinecraftKeyRegistered.getId(commandcontext, "sound"), soundcategory, ArgumentVec3.getVec3(commandcontext, "pos"), 1.0F, 1.0F, 0.0F);
        })).then(((RequiredArgumentBuilder) net.minecraft.commands.CommandDispatcher.argument("volume", FloatArgumentType.floatArg(0.0F)).executes((commandcontext) -> {
            return playSound((CommandListenerWrapper) commandcontext.getSource(), ArgumentEntity.getPlayers(commandcontext, "targets"), ArgumentMinecraftKeyRegistered.getId(commandcontext, "sound"), soundcategory, ArgumentVec3.getVec3(commandcontext, "pos"), (Float) commandcontext.getArgument("volume", Float.class), 1.0F, 0.0F);
        })).then(((RequiredArgumentBuilder) net.minecraft.commands.CommandDispatcher.argument("pitch", FloatArgumentType.floatArg(0.0F, 2.0F)).executes((commandcontext) -> {
            return playSound((CommandListenerWrapper) commandcontext.getSource(), ArgumentEntity.getPlayers(commandcontext, "targets"), ArgumentMinecraftKeyRegistered.getId(commandcontext, "sound"), soundcategory, ArgumentVec3.getVec3(commandcontext, "pos"), (Float) commandcontext.getArgument("volume", Float.class), (Float) commandcontext.getArgument("pitch", Float.class), 0.0F);
        })).then(net.minecraft.commands.CommandDispatcher.argument("minVolume", FloatArgumentType.floatArg(0.0F, 1.0F)).executes((commandcontext) -> {
            return playSound((CommandListenerWrapper) commandcontext.getSource(), ArgumentEntity.getPlayers(commandcontext, "targets"), ArgumentMinecraftKeyRegistered.getId(commandcontext, "sound"), soundcategory, ArgumentVec3.getVec3(commandcontext, "pos"), (Float) commandcontext.getArgument("volume", Float.class), (Float) commandcontext.getArgument("pitch", Float.class), (Float) commandcontext.getArgument("minVolume", Float.class));
        }))))));
    }

    private static Collection<EntityPlayer> getCallingPlayerAsCollection(@Nullable EntityPlayer entityplayer) {
        return entityplayer != null ? List.of(entityplayer) : List.of();
    }

    private static int playSound(CommandListenerWrapper commandlistenerwrapper, Collection<EntityPlayer> collection, MinecraftKey minecraftkey, SoundCategory soundcategory, Vec3D vec3d, float f, float f1, float f2) throws CommandSyntaxException {
        Holder<SoundEffect> holder = Holder.direct(SoundEffect.createVariableRangeEvent(minecraftkey));
        double d0 = (double) MathHelper.square(((SoundEffect) holder.value()).getRange(f));
        int i = 0;
        long j = commandlistenerwrapper.getLevel().getRandom().nextLong();
        Iterator iterator = collection.iterator();

        while (iterator.hasNext()) {
            EntityPlayer entityplayer = (EntityPlayer) iterator.next();
            double d1 = vec3d.x - entityplayer.getX();
            double d2 = vec3d.y - entityplayer.getY();
            double d3 = vec3d.z - entityplayer.getZ();
            double d4 = d1 * d1 + d2 * d2 + d3 * d3;
            Vec3D vec3d1 = vec3d;
            float f3 = f;

            if (d4 > d0) {
                if (f2 <= 0.0F) {
                    continue;
                }

                double d5 = Math.sqrt(d4);

                vec3d1 = new Vec3D(entityplayer.getX() + d1 / d5 * 2.0D, entityplayer.getY() + d2 / d5 * 2.0D, entityplayer.getZ() + d3 / d5 * 2.0D);
                f3 = f2;
            }

            entityplayer.connection.send(new PacketPlayOutNamedSoundEffect(holder, soundcategory, vec3d1.x(), vec3d1.y(), vec3d1.z(), f3, f1, j));
            ++i;
        }

        if (i == 0) {
            throw CommandPlaySound.ERROR_TOO_FAR.create();
        } else {
            if (collection.size() == 1) {
                commandlistenerwrapper.sendSuccess(() -> {
                    return IChatBaseComponent.translatable("commands.playsound.success.single", IChatBaseComponent.translationArg(minecraftkey), ((EntityPlayer) collection.iterator().next()).getDisplayName());
                }, true);
            } else {
                commandlistenerwrapper.sendSuccess(() -> {
                    return IChatBaseComponent.translatable("commands.playsound.success.multiple", IChatBaseComponent.translationArg(minecraftkey), collection.size());
                }, true);
            }

            return i;
        }
    }
}
