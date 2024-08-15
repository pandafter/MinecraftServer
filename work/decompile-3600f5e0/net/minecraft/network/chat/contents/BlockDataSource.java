package net.minecraft.network.chat.contents;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.commands.arguments.coordinates.ArgumentPosition;
import net.minecraft.commands.arguments.coordinates.IVectorPosition;
import net.minecraft.core.BlockPosition;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.level.block.entity.TileEntity;

public record BlockDataSource(String posPattern, @Nullable IVectorPosition compiledPos) implements DataSource {

    public static final MapCodec<BlockDataSource> SUB_CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(Codec.STRING.fieldOf("block").forGetter(BlockDataSource::posPattern)).apply(instance, BlockDataSource::new);
    });
    public static final DataSource.a<BlockDataSource> TYPE = new DataSource.a<>(BlockDataSource.SUB_CODEC, "block");

    public BlockDataSource(String s) {
        this(s, compilePos(s));
    }

    @Nullable
    private static IVectorPosition compilePos(String s) {
        try {
            return ArgumentPosition.blockPos().parse(new StringReader(s));
        } catch (CommandSyntaxException commandsyntaxexception) {
            return null;
        }
    }

    @Override
    public Stream<NBTTagCompound> getData(CommandListenerWrapper commandlistenerwrapper) {
        if (this.compiledPos != null) {
            WorldServer worldserver = commandlistenerwrapper.getLevel();
            BlockPosition blockposition = this.compiledPos.getBlockPos(commandlistenerwrapper);

            if (worldserver.isLoaded(blockposition)) {
                TileEntity tileentity = worldserver.getBlockEntity(blockposition);

                if (tileentity != null) {
                    return Stream.of(tileentity.saveWithFullMetadata(commandlistenerwrapper.registryAccess()));
                }
            }
        }

        return Stream.empty();
    }

    @Override
    public DataSource.a<?> type() {
        return BlockDataSource.TYPE;
    }

    public String toString() {
        return "block=" + this.posPattern;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else {
            boolean flag;

            if (object instanceof BlockDataSource) {
                BlockDataSource blockdatasource = (BlockDataSource) object;

                if (this.posPattern.equals(blockdatasource.posPattern)) {
                    flag = true;
                    return flag;
                }
            }

            flag = false;
            return flag;
        }
    }

    public int hashCode() {
        return this.posPattern.hashCode();
    }
}
