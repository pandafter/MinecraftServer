package net.minecraft.world.entity.ai.attributes;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import java.util.function.IntFunction;
import javax.annotation.Nullable;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.INamable;
import net.minecraft.util.MathHelper;
import net.minecraft.util.RandomSource;
import org.slf4j.Logger;

public record AttributeModifier(UUID id, String name, double amount, AttributeModifier.Operation operation) {

    private static final Logger LOGGER = LogUtils.getLogger();
    public static final MapCodec<AttributeModifier> MAP_CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(UUIDUtil.CODEC.fieldOf("uuid").forGetter(AttributeModifier::id), Codec.STRING.fieldOf("name").forGetter((attributemodifier) -> {
            return attributemodifier.name;
        }), Codec.DOUBLE.fieldOf("amount").forGetter(AttributeModifier::amount), AttributeModifier.Operation.CODEC.fieldOf("operation").forGetter(AttributeModifier::operation)).apply(instance, AttributeModifier::new);
    });
    public static final Codec<AttributeModifier> CODEC = AttributeModifier.MAP_CODEC.codec();
    public static final StreamCodec<ByteBuf, AttributeModifier> STREAM_CODEC = StreamCodec.composite(UUIDUtil.STREAM_CODEC, AttributeModifier::id, ByteBufCodecs.STRING_UTF8, (attributemodifier) -> {
        return attributemodifier.name;
    }, ByteBufCodecs.DOUBLE, AttributeModifier::amount, AttributeModifier.Operation.STREAM_CODEC, AttributeModifier::operation, AttributeModifier::new);

    public AttributeModifier(String s, double d0, AttributeModifier.Operation attributemodifier_operation) {
        this(MathHelper.createInsecureUUID(RandomSource.createNewThreadLocalInstance()), s, d0, attributemodifier_operation);
    }

    public NBTTagCompound save() {
        NBTTagCompound nbttagcompound = new NBTTagCompound();

        nbttagcompound.putString("Name", this.name);
        nbttagcompound.putDouble("Amount", this.amount);
        nbttagcompound.putInt("Operation", this.operation.id());
        nbttagcompound.putUUID("UUID", this.id);
        return nbttagcompound;
    }

    @Nullable
    public static AttributeModifier load(NBTTagCompound nbttagcompound) {
        try {
            UUID uuid = nbttagcompound.getUUID("UUID");
            AttributeModifier.Operation attributemodifier_operation = (AttributeModifier.Operation) AttributeModifier.Operation.BY_ID.apply(nbttagcompound.getInt("Operation"));

            return new AttributeModifier(uuid, nbttagcompound.getString("Name"), nbttagcompound.getDouble("Amount"), attributemodifier_operation);
        } catch (Exception exception) {
            AttributeModifier.LOGGER.warn("Unable to create attribute: {}", exception.getMessage());
            return null;
        }
    }

    public static enum Operation implements INamable {

        ADD_VALUE("add_value", 0), ADD_MULTIPLIED_BASE("add_multiplied_base", 1), ADD_MULTIPLIED_TOTAL("add_multiplied_total", 2);

        public static final IntFunction<AttributeModifier.Operation> BY_ID = ByIdMap.continuous(AttributeModifier.Operation::id, values(), ByIdMap.a.ZERO);
        public static final StreamCodec<ByteBuf, AttributeModifier.Operation> STREAM_CODEC = ByteBufCodecs.idMapper(AttributeModifier.Operation.BY_ID, AttributeModifier.Operation::id);
        public static final Codec<AttributeModifier.Operation> CODEC = INamable.fromEnum(AttributeModifier.Operation::values);
        private final String name;
        private final int id;

        private Operation(final String s, final int i) {
            this.name = s;
            this.id = i;
        }

        public int id() {
            return this.id;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
