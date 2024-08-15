package net.minecraft.network.protocol.game;

import io.netty.buffer.ByteBuf;
import java.util.Optional;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ClientboundServerDataPacket(IChatBaseComponent motd, Optional<byte[]> iconBytes) implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<ByteBuf, ClientboundServerDataPacket> STREAM_CODEC = StreamCodec.composite(ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC, ClientboundServerDataPacket::motd, ByteBufCodecs.BYTE_ARRAY.apply(ByteBufCodecs::optional), ClientboundServerDataPacket::iconBytes, ClientboundServerDataPacket::new);

    @Override
    public PacketType<ClientboundServerDataPacket> type() {
        return GamePacketTypes.CLIENTBOUND_SERVER_DATA;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleServerData(this);
    }
}
