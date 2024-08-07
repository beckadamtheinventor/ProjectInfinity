package net.lerariemann.infinity.dimensions.features;

import net.lerariemann.infinity.dimensions.RandomFeaturesList;
import net.minecraft.nbt.NbtCompound;

public class RandomFungus extends RandomisedFeature {
    NbtCompound mainsurfaceblock;

    public RandomFungus(RandomFeaturesList parent) {
        super(parent, "fungus");
        id = "huge_fungus";
        mainsurfaceblock = parent.surface_block;
        save_with_placement();
    }

    void placement() {
        addCountEveryLayer(1);
    }

    NbtCompound feature() {
        NbtCompound config = new NbtCompound();
        addRandomBlock(config, "hat_state", "leaf_like_blocks");
        addRandomBlock(config, "decor_state", "plant_like_blocks");
        addRandomBlock(config, "stem_state", "log_like_blocks");
        NbtCompound replaceableBlocks = new NbtCompound();
        replaceableBlocks.putString("type", "minecraft:matching_block_tag");
        replaceableBlocks.putString("tag", PROVIDER.randomName(random, "tags").replace("#", ""));
        config.put("replaceable_blocks", replaceableBlocks);
        config.put("valid_base_block", mainsurfaceblock);
        return feature(config);
    }
}
