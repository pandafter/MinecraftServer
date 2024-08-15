package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import net.minecraft.util.datafix.schemas.DataConverterSchemaNamed;

public abstract class ItemStackTagFix extends DataFix {

    private final String name;
    private final Predicate<String> idFilter;

    public ItemStackTagFix(Schema schema, String s, Predicate<String> predicate) {
        super(schema, false);
        this.name = s;
        this.idFilter = predicate;
    }

    public final TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(DataConverterTypes.ITEM_STACK);

        return this.fixTypeEverywhereTyped(this.name, type, createFixer(type, this.idFilter, this::fixItemStackTag));
    }

    public static UnaryOperator<Typed<?>> createFixer(Type<?> type, Predicate<String> predicate, UnaryOperator<Dynamic<?>> unaryoperator) {
        OpticFinder<Pair<String, String>> opticfinder = DSL.fieldFinder("id", DSL.named(DataConverterTypes.ITEM_NAME.typeName(), DataConverterSchemaNamed.namespacedString()));
        OpticFinder<?> opticfinder1 = type.findField("tag");

        return (typed) -> {
            Optional<Pair<String, String>> optional = typed.getOptional(opticfinder);

            return optional.isPresent() && predicate.test((String) ((Pair) optional.get()).getSecond()) ? typed.updateTyped(opticfinder1, (typed1) -> {
                return typed1.update(DSL.remainderFinder(), unaryoperator);
            }) : typed;
        };
    }

    protected abstract <T> Dynamic<T> fixItemStackTag(Dynamic<T> dynamic);
}
