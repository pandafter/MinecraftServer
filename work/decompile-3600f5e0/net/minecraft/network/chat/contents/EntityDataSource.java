package net.minecraft.network.chat.contents;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.advancements.critereon.CriterionConditionNBT;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.commands.arguments.selector.ArgumentParserSelector;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.entity.Entity;

public record EntityDataSource(String selectorPattern, @Nullable EntitySelector compiledSelector) implements DataSource {

    public static final MapCodec<EntityDataSource> SUB_CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(Codec.STRING.fieldOf("entity").forGetter(EntityDataSource::selectorPattern)).apply(instance, EntityDataSource::new);
    });
    public static final DataSource.a<EntityDataSource> TYPE = new DataSource.a<>(EntityDataSource.SUB_CODEC, "entity");

    public EntityDataSource(String s) {
        this(s, compileSelector(s));
    }

    @Nullable
    private static EntitySelector compileSelector(String s) {
        try {
            ArgumentParserSelector argumentparserselector = new ArgumentParserSelector(new StringReader(s));

            return argumentparserselector.parse();
        } catch (CommandSyntaxException commandsyntaxexception) {
            return null;
        }
    }

    @Override
    public Stream<NBTTagCompound> getData(CommandListenerWrapper commandlistenerwrapper) throws CommandSyntaxException {
        if (this.compiledSelector != null) {
            List<? extends Entity> list = this.compiledSelector.findEntities(commandlistenerwrapper);

            return list.stream().map(CriterionConditionNBT::getEntityTagToCompare);
        } else {
            return Stream.empty();
        }
    }

    @Override
    public DataSource.a<?> type() {
        return EntityDataSource.TYPE;
    }

    public String toString() {
        return "entity=" + this.selectorPattern;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else {
            boolean flag;

            if (object instanceof EntityDataSource) {
                EntityDataSource entitydatasource = (EntityDataSource) object;

                if (this.selectorPattern.equals(entitydatasource.selectorPattern)) {
                    flag = true;
                    return flag;
                }
            }

            flag = false;
            return flag;
        }
    }

    public int hashCode() {
        return this.selectorPattern.hashCode();
    }
}
