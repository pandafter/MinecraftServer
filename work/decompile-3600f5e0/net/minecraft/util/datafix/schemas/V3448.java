package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.DataConverterTypes;

public class V3448 extends DataConverterSchemaNamed {

    public V3448(int i, Schema schema) {
        super(i, schema);
    }

    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema schema) {
        Map<String, Supplier<TypeTemplate>> map = super.registerBlockEntities(schema);

        schema.register(map, "minecraft:decorated_pot", () -> {
            return DSL.optionalFields("sherds", DSL.list(DataConverterTypes.ITEM_NAME.in(schema)), "item", DataConverterTypes.ITEM_STACK.in(schema));
        });
        return map;
    }
}
