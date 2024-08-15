package net.minecraft.world.flag;

import java.util.Set;
import net.minecraft.core.IRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;

public interface FeatureElement {

    Set<ResourceKey<? extends IRegistry<? extends FeatureElement>>> FILTERED_REGISTRIES = Set.of(Registries.ITEM, Registries.BLOCK, Registries.ENTITY_TYPE, Registries.MENU, Registries.ENCHANTMENT, Registries.POTION, Registries.MOB_EFFECT);

    FeatureFlagSet requiredFeatures();

    default boolean isEnabled(FeatureFlagSet featureflagset) {
        return this.requiredFeatures().isSubsetOf(featureflagset);
    }
}
