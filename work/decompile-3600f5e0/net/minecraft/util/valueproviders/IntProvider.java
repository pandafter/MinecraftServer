package net.minecraft.util.valueproviders;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;

public abstract class IntProvider {

    private static final Codec<Either<Integer, IntProvider>> CONSTANT_OR_DISPATCH_CODEC = Codec.either(Codec.INT, BuiltInRegistries.INT_PROVIDER_TYPE.byNameCodec().dispatch(IntProvider::getType, IntProviderType::codec));
    public static final Codec<IntProvider> CODEC = IntProvider.CONSTANT_OR_DISPATCH_CODEC.xmap((either) -> {
        return (IntProvider) either.map(ConstantInt::of, (intprovider) -> {
            return intprovider;
        });
    }, (intprovider) -> {
        return intprovider.getType() == IntProviderType.CONSTANT ? Either.left(((ConstantInt) intprovider).getValue()) : Either.right(intprovider);
    });
    public static final Codec<IntProvider> NON_NEGATIVE_CODEC = codec(0, Integer.MAX_VALUE);
    public static final Codec<IntProvider> POSITIVE_CODEC = codec(1, Integer.MAX_VALUE);

    public IntProvider() {}

    public static Codec<IntProvider> codec(int i, int j) {
        return validateCodec(i, j, IntProvider.CODEC);
    }

    public static <T extends IntProvider> Codec<T> validateCodec(int i, int j, Codec<T> codec) {
        return codec.validate((intprovider) -> {
            return validate(i, j, intprovider);
        });
    }

    private static <T extends IntProvider> DataResult<T> validate(int i, int j, T t0) {
        return t0.getMinValue() < i ? DataResult.error(() -> {
            return "Value provider too low: " + i + " [" + t0.getMinValue() + "-" + t0.getMaxValue() + "]";
        }) : (t0.getMaxValue() > j ? DataResult.error(() -> {
            return "Value provider too high: " + j + " [" + t0.getMinValue() + "-" + t0.getMaxValue() + "]";
        }) : DataResult.success(t0));
    }

    public abstract int sample(RandomSource randomsource);

    public abstract int getMinValue();

    public abstract int getMaxValue();

    public abstract IntProviderType<?> getType();
}
