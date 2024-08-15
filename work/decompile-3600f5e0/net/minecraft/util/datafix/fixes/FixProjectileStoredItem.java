package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.function.Function;
import net.minecraft.SystemUtils;

public class FixProjectileStoredItem extends DataFix {

    private static final String EMPTY_POTION = "minecraft:empty";

    public FixProjectileStoredItem(Schema schema) {
        super(schema, true);
    }

    protected TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(DataConverterTypes.ENTITY);
        Type<?> type1 = this.getOutputSchema().getType(DataConverterTypes.ENTITY);

        return this.fixTypeEverywhereTyped("Fix AbstractArrow item type", type, type1, this.chainAllFilters(this.fixChoice("minecraft:trident", FixProjectileStoredItem::castUnchecked), this.fixChoice("minecraft:arrow", FixProjectileStoredItem::fixArrow), this.fixChoice("minecraft:spectral_arrow", FixProjectileStoredItem::fixSpectralArrow)));
    }

    @SafeVarargs
    private <T> Function<Typed<?>, Typed<?>> chainAllFilters(Function<Typed<?>, Typed<?>>... afunction) {
        return (typed) -> {
            Function[] afunction1 = afunction;
            int i = afunction.length;

            for (int j = 0; j < i; ++j) {
                Function<Typed<?>, Typed<?>> function = afunction1[j];

                typed = (Typed) function.apply(typed);
            }

            return typed;
        };
    }

    private Function<Typed<?>, Typed<?>> fixChoice(String s, FixProjectileStoredItem.a<?> fixprojectilestoreditem_a) {
        Type<?> type = this.getInputSchema().getChoiceType(DataConverterTypes.ENTITY, s);
        Type<?> type1 = this.getOutputSchema().getChoiceType(DataConverterTypes.ENTITY, s);

        return fixChoiceCap(s, fixprojectilestoreditem_a, type, type1);
    }

    private static <T> Function<Typed<?>, Typed<?>> fixChoiceCap(String s, FixProjectileStoredItem.a<?> fixprojectilestoreditem_a, Type<?> type, Type<T> type1) {
        OpticFinder<?> opticfinder = DSL.namedChoice(s, type);

        return (typed) -> {
            return typed.updateTyped(opticfinder, type1, (typed1) -> {
                return fixprojectilestoreditem_a.fix(typed1, type1);
            });
        };
    }

    private static <T> Typed<T> fixArrow(Typed<?> typed, Type<T> type) {
        return SystemUtils.writeAndReadTypedOrThrow(typed, type, (dynamic) -> {
            return dynamic.set("item", createItemStack(dynamic, getArrowType(dynamic)));
        });
    }

    private static String getArrowType(Dynamic<?> dynamic) {
        return dynamic.get("Potion").asString("minecraft:empty").equals("minecraft:empty") ? "minecraft:arrow" : "minecraft:tipped_arrow";
    }

    private static <T> Typed<T> fixSpectralArrow(Typed<?> typed, Type<T> type) {
        return SystemUtils.writeAndReadTypedOrThrow(typed, type, (dynamic) -> {
            return dynamic.set("item", createItemStack(dynamic, "minecraft:spectral_arrow"));
        });
    }

    private static Dynamic<?> createItemStack(Dynamic<?> dynamic, String s) {
        return dynamic.createMap(ImmutableMap.of(dynamic.createString("id"), dynamic.createString(s), dynamic.createString("Count"), dynamic.createInt(1)));
    }

    private static <T> Typed<T> castUnchecked(Typed<?> typed, Type<T> type) {
        return new Typed(type, typed.getOps(), typed.getValue());
    }

    private interface a<F> {

        Typed<F> fix(Typed<?> typed, Type<F> type);
    }
}
