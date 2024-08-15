package net.minecraft.network.protocol.game;

import it.unimi.dsi.fastutil.shorts.ShortIterator;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.SectionPosition;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.chunk.ChunkSection;

public class PacketPlayOutMultiBlockChange implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<PacketDataSerializer, PacketPlayOutMultiBlockChange> STREAM_CODEC = Packet.codec(PacketPlayOutMultiBlockChange::write, PacketPlayOutMultiBlockChange::new);
    private static final int POS_IN_SECTION_BITS = 12;
    private final SectionPosition sectionPos;
    private final short[] positions;
    private final IBlockData[] states;

    public PacketPlayOutMultiBlockChange(SectionPosition sectionposition, ShortSet shortset, ChunkSection chunksection) {
        this.sectionPos = sectionposition;
        int i = shortset.size();

        this.positions = new short[i];
        this.states = new IBlockData[i];
        int j = 0;

        for (ShortIterator shortiterator = shortset.iterator(); shortiterator.hasNext(); ++j) {
            short short0 = (Short) shortiterator.next();

            this.positions[j] = short0;
            this.states[j] = chunksection.getBlockState(SectionPosition.sectionRelativeX(short0), SectionPosition.sectionRelativeY(short0), SectionPosition.sectionRelativeZ(short0));
        }

    }

    private PacketPlayOutMultiBlockChange(PacketDataSerializer packetdataserializer) {
        this.sectionPos = SectionPosition.of(packetdataserializer.readLong());
        int i = packetdataserializer.readVarInt();

        this.positions = new short[i];
        this.states = new IBlockData[i];

        for (int j = 0; j < i; ++j) {
            long k = packetdataserializer.readVarLong();

            this.positions[j] = (short) ((int) (k & 4095L));
            this.states[j] = (IBlockData) Block.BLOCK_STATE_REGISTRY.byId((int) (k >>> 12));
        }

    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeLong(this.sectionPos.asLong());
        packetdataserializer.writeVarInt(this.positions.length);

        for (int i = 0; i < this.positions.length; ++i) {
            packetdataserializer.writeVarLong((long) Block.getId(this.states[i]) << 12 | (long) this.positions[i]);
        }

    }

    @Override
    public PacketType<PacketPlayOutMultiBlockChange> type() {
        return GamePacketTypes.CLIENTBOUND_SECTION_BLOCKS_UPDATE;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleChunkBlocksUpdate(this);
    }

    public void runUpdates(BiConsumer<BlockPosition, IBlockData> biconsumer) {
        BlockPosition.MutableBlockPosition blockposition_mutableblockposition = new BlockPosition.MutableBlockPosition();

        for (int i = 0; i < this.positions.length; ++i) {
            short short0 = this.positions[i];

            blockposition_mutableblockposition.set(this.sectionPos.relativeToBlockX(short0), this.sectionPos.relativeToBlockY(short0), this.sectionPos.relativeToBlockZ(short0));
            biconsumer.accept(blockposition_mutableblockposition, this.states[i]);
        }

    }
}
