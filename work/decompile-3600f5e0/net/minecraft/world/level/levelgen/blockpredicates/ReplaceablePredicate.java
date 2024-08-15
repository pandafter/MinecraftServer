package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BaseBlockPosition;
import net.minecraft.world.level.block.state.IBlockData;

class ReplaceablePredicate extends StateTestingPredicate {

    public static final MapCodec<ReplaceablePredicate> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return stateTestingCodec(instance).apply(instance, ReplaceablePredicate::new);
    });

    public ReplaceablePredicate(BaseBlockPosition baseblockposition) {
        super(baseblockposition);
    }

    @Override
    protected boolean test(IBlockData iblockdata) {
        return iblockdata.canBeReplaced();
    }

    @Override
    public BlockPredicateType<?> type() {
        return BlockPredicateType.REPLACEABLE;
    }
}
