package net.minecraft.sounds;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.RegistryFileCodec;

public class SoundEffect {

    public static final Codec<SoundEffect> DIRECT_CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(MinecraftKey.CODEC.fieldOf("sound_id").forGetter(SoundEffect::getLocation), Codec.FLOAT.lenientOptionalFieldOf("range").forGetter(SoundEffect::fixedRange)).apply(instance, SoundEffect::create);
    });
    public static final Codec<Holder<SoundEffect>> CODEC = RegistryFileCodec.create(Registries.SOUND_EVENT, SoundEffect.DIRECT_CODEC);
    public static final StreamCodec<ByteBuf, SoundEffect> DIRECT_STREAM_CODEC = StreamCodec.composite(MinecraftKey.STREAM_CODEC, SoundEffect::getLocation, ByteBufCodecs.FLOAT.apply(ByteBufCodecs::optional), SoundEffect::fixedRange, SoundEffect::create);
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<SoundEffect>> STREAM_CODEC = ByteBufCodecs.holder(Registries.SOUND_EVENT, SoundEffect.DIRECT_STREAM_CODEC);
    private static final float DEFAULT_RANGE = 16.0F;
    private final MinecraftKey location;
    private final float range;
    private final boolean newSystem;

    private static SoundEffect create(MinecraftKey minecraftkey, Optional<Float> optional) {
        return (SoundEffect) optional.map((ofloat) -> {
            return createFixedRangeEvent(minecraftkey, ofloat);
        }).orElseGet(() -> {
            return createVariableRangeEvent(minecraftkey);
        });
    }

    public static SoundEffect createVariableRangeEvent(MinecraftKey minecraftkey) {
        return new SoundEffect(minecraftkey, 16.0F, false);
    }

    public static SoundEffect createFixedRangeEvent(MinecraftKey minecraftkey, float f) {
        return new SoundEffect(minecraftkey, f, true);
    }

    private SoundEffect(MinecraftKey minecraftkey, float f, boolean flag) {
        this.location = minecraftkey;
        this.range = f;
        this.newSystem = flag;
    }

    public MinecraftKey getLocation() {
        return this.location;
    }

    public float getRange(float f) {
        return this.newSystem ? this.range : (f > 1.0F ? 16.0F * f : 16.0F);
    }

    private Optional<Float> fixedRange() {
        return this.newSystem ? Optional.of(this.range) : Optional.empty();
    }
}
