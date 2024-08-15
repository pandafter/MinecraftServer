package net.minecraft.core;

import com.mojang.datafixers.util.Either;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

public interface Holder<T> {

    T value();

    boolean isBound();

    boolean is(MinecraftKey minecraftkey);

    boolean is(ResourceKey<T> resourcekey);

    boolean is(Predicate<ResourceKey<T>> predicate);

    boolean is(TagKey<T> tagkey);

    /** @deprecated */
    @Deprecated
    boolean is(Holder<T> holder);

    Stream<TagKey<T>> tags();

    Either<ResourceKey<T>, T> unwrap();

    Optional<ResourceKey<T>> unwrapKey();

    Holder.b kind();

    boolean canSerializeIn(HolderOwner<T> holderowner);

    default String getRegisteredName() {
        return (String) this.unwrapKey().map((resourcekey) -> {
            return resourcekey.location().toString();
        }).orElse("[unregistered]");
    }

    static <T> Holder<T> direct(T t0) {
        return new Holder.a<>(t0);
    }

    public static record a<T>(T value) implements Holder<T> {

        @Override
        public boolean isBound() {
            return true;
        }

        @Override
        public boolean is(MinecraftKey minecraftkey) {
            return false;
        }

        @Override
        public boolean is(ResourceKey<T> resourcekey) {
            return false;
        }

        @Override
        public boolean is(TagKey<T> tagkey) {
            return false;
        }

        @Override
        public boolean is(Holder<T> holder) {
            return this.value.equals(holder.value());
        }

        @Override
        public boolean is(Predicate<ResourceKey<T>> predicate) {
            return false;
        }

        @Override
        public Either<ResourceKey<T>, T> unwrap() {
            return Either.right(this.value);
        }

        @Override
        public Optional<ResourceKey<T>> unwrapKey() {
            return Optional.empty();
        }

        @Override
        public Holder.b kind() {
            return Holder.b.DIRECT;
        }

        public String toString() {
            return "Direct{" + String.valueOf(this.value) + "}";
        }

        @Override
        public boolean canSerializeIn(HolderOwner<T> holderowner) {
            return true;
        }

        @Override
        public Stream<TagKey<T>> tags() {
            return Stream.of();
        }
    }

    public static class c<T> implements Holder<T> {

        private final HolderOwner<T> owner;
        private Set<TagKey<T>> tags = Set.of();
        private final Holder.c.a type;
        @Nullable
        private ResourceKey<T> key;
        @Nullable
        private T value;

        protected c(Holder.c.a holder_c_a, HolderOwner<T> holderowner, @Nullable ResourceKey<T> resourcekey, @Nullable T t0) {
            this.owner = holderowner;
            this.type = holder_c_a;
            this.key = resourcekey;
            this.value = t0;
        }

        public static <T> Holder.c<T> createStandAlone(HolderOwner<T> holderowner, ResourceKey<T> resourcekey) {
            return new Holder.c<>(Holder.c.a.STAND_ALONE, holderowner, resourcekey, (Object) null);
        }

        /** @deprecated */
        @Deprecated
        public static <T> Holder.c<T> createIntrusive(HolderOwner<T> holderowner, @Nullable T t0) {
            return new Holder.c<>(Holder.c.a.INTRUSIVE, holderowner, (ResourceKey) null, t0);
        }

        public ResourceKey<T> key() {
            if (this.key == null) {
                String s = String.valueOf(this.value);

                throw new IllegalStateException("Trying to access unbound value '" + s + "' from registry " + String.valueOf(this.owner));
            } else {
                return this.key;
            }
        }

        @Override
        public T value() {
            if (this.value == null) {
                String s = String.valueOf(this.key);

                throw new IllegalStateException("Trying to access unbound value '" + s + "' from registry " + String.valueOf(this.owner));
            } else {
                return this.value;
            }
        }

        @Override
        public boolean is(MinecraftKey minecraftkey) {
            return this.key().location().equals(minecraftkey);
        }

        @Override
        public boolean is(ResourceKey<T> resourcekey) {
            return this.key() == resourcekey;
        }

        @Override
        public boolean is(TagKey<T> tagkey) {
            return this.tags.contains(tagkey);
        }

        @Override
        public boolean is(Holder<T> holder) {
            return holder.is(this.key());
        }

        @Override
        public boolean is(Predicate<ResourceKey<T>> predicate) {
            return predicate.test(this.key());
        }

        @Override
        public boolean canSerializeIn(HolderOwner<T> holderowner) {
            return this.owner.canSerializeIn(holderowner);
        }

        @Override
        public Either<ResourceKey<T>, T> unwrap() {
            return Either.left(this.key());
        }

        @Override
        public Optional<ResourceKey<T>> unwrapKey() {
            return Optional.of(this.key());
        }

        @Override
        public Holder.b kind() {
            return Holder.b.REFERENCE;
        }

        @Override
        public boolean isBound() {
            return this.key != null && this.value != null;
        }

        void bindKey(ResourceKey<T> resourcekey) {
            if (this.key != null && resourcekey != this.key) {
                String s = String.valueOf(this.key);

                throw new IllegalStateException("Can't change holder key: existing=" + s + ", new=" + String.valueOf(resourcekey));
            } else {
                this.key = resourcekey;
            }
        }

        protected void bindValue(T t0) {
            if (this.type == Holder.c.a.INTRUSIVE && this.value != t0) {
                String s = String.valueOf(this.key);

                throw new IllegalStateException("Can't change holder " + s + " value: existing=" + String.valueOf(this.value) + ", new=" + String.valueOf(t0));
            } else {
                this.value = t0;
            }
        }

        void bindTags(Collection<TagKey<T>> collection) {
            this.tags = Set.copyOf(collection);
        }

        @Override
        public Stream<TagKey<T>> tags() {
            return this.tags.stream();
        }

        public String toString() {
            String s = String.valueOf(this.key);

            return "Reference{" + s + "=" + String.valueOf(this.value) + "}";
        }

        protected static enum a {

            STAND_ALONE, INTRUSIVE;

            private a() {}
        }
    }

    public static enum b {

        REFERENCE, DIRECT;

        private b() {}
    }
}
