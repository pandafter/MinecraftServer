package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.levelgen.HeightMap;

public class DefinedStructureProcessorGravity extends DefinedStructureProcessor {

    public static final MapCodec<DefinedStructureProcessorGravity> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(HeightMap.Type.CODEC.fieldOf("heightmap").orElse(HeightMap.Type.WORLD_SURFACE_WG).forGetter((definedstructureprocessorgravity) -> {
            return definedstructureprocessorgravity.heightmap;
        }), Codec.INT.fieldOf("offset").orElse(0).forGetter((definedstructureprocessorgravity) -> {
            return definedstructureprocessorgravity.offset;
        })).apply(instance, DefinedStructureProcessorGravity::new);
    });
    private final HeightMap.Type heightmap;
    private final int offset;

    public DefinedStructureProcessorGravity(HeightMap.Type heightmap_type, int i) {
        this.heightmap = heightmap_type;
        this.offset = i;
    }

    @Nullable
    @Override
    public DefinedStructure.BlockInfo processBlock(IWorldReader iworldreader, BlockPosition blockposition, BlockPosition blockposition1, DefinedStructure.BlockInfo definedstructure_blockinfo, DefinedStructure.BlockInfo definedstructure_blockinfo1, DefinedStructureInfo definedstructureinfo) {
        HeightMap.Type heightmap_type;

        if (iworldreader instanceof WorldServer) {
            if (this.heightmap == HeightMap.Type.WORLD_SURFACE_WG) {
                heightmap_type = HeightMap.Type.WORLD_SURFACE;
            } else if (this.heightmap == HeightMap.Type.OCEAN_FLOOR_WG) {
                heightmap_type = HeightMap.Type.OCEAN_FLOOR;
            } else {
                heightmap_type = this.heightmap;
            }
        } else {
            heightmap_type = this.heightmap;
        }

        BlockPosition blockposition2 = definedstructure_blockinfo1.pos();
        int i = iworldreader.getHeight(heightmap_type, blockposition2.getX(), blockposition2.getZ()) + this.offset;
        int j = definedstructure_blockinfo.pos().getY();

        return new DefinedStructure.BlockInfo(new BlockPosition(blockposition2.getX(), i + j, blockposition2.getZ()), definedstructure_blockinfo1.state(), definedstructure_blockinfo1.nbt());
    }

    @Override
    protected DefinedStructureStructureProcessorType<?> getType() {
        return DefinedStructureStructureProcessorType.GRAVITY;
    }
}
