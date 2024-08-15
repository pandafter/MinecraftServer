package net.minecraft.resources;

import com.google.common.collect.MapMaker;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import net.minecraft.core.IRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.StreamCodec;

public class ResourceKey<T> {

    private static final ConcurrentMap<ResourceKey.a, ResourceKey<?>> VALUES = (new MapMaker()).weakValues().makeMap();
    private final MinecraftKey registryName;
    private final MinecraftKey location;

    public static <T> Codec<ResourceKey<T>> codec(ResourceKey<? extends IRegistry<T>> resourcekey) {
        return MinecraftKey.CODEC.xmap((minecraftkey) -> {
            return create(resourcekey, minecraftkey);
        }, ResourceKey::location);
    }

    public static <T> StreamCodec<ByteBuf, ResourceKey<T>> streamCodec(ResourceKey<? extends IRegistry<T>> resourcekey) {
        return MinecraftKey.STREAM_CODEC.map((minecraftkey) -> {
            return create(resourcekey, minecraftkey);
        }, ResourceKey::location);
    }

    public static <T> ResourceKey<T> create(ResourceKey<? extends IRegistry<T>> resourcekey, MinecraftKey minecraftkey) {
        return create(resourcekey.location, minecraftkey);
    }

    public static <T> ResourceKey<IRegistry<T>> createRegistryKey(MinecraftKey minecraftkey) {
        return create(Registries.ROOT_REGISTRY_NAME, minecraftkey);
    }

    private static <T> ResourceKey<T> create(MinecraftKey minecraftkey, MinecraftKey minecraftkey1) {
        return (ResourceKey) ResourceKey.VALUES.computeIfAbsent(new ResourceKey.a(minecraftkey, minecraftkey1), (resourcekey_a) -> {
            return new ResourceKey<>(resourcekey_a.registry, resourcekey_a.location);
        });
    }

    private ResourceKey(MinecraftKey minecraftkey, MinecraftKey minecraftkey1) {
        this.registryName = minecraftkey;
        this.location = minecraftkey1;
    }

    public String toString() {
        String s = String.valueOf(this.registryName);

        return "ResourceKey[" + s + " / " + String.valueOf(this.location) + "]";
    }

    public boolean isFor(ResourceKey<? extends IRegistry<?>> resourcekey) {
        return this.registryName.equals(resourcekey.location());
    }

    public <E> Optional<ResourceKey<E>> cast(ResourceKey<? extends IRegistry<E>> resourcekey) {
        return this.isFor(resourcekey) ? Optional.of(this) : Optional.empty();
    }

    public MinecraftKey location() {
        return this.location;
    }

    public MinecraftKey registry() {
        return this.registryName;
    }

    public ResourceKey<IRegistry<T>> registryKey() {
        return createRegistryKey(this.registryName);
    }

    private static record a(MinecraftKey registry, MinecraftKey location) {

    }
}
