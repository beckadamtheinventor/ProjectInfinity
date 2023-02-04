package net.lerariemann.infinity.dimensions.features;

import net.lerariemann.infinity.dimensions.RandomFeaturesList;
import net.lerariemann.infinity.dimensions.RandomProvider;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

public class RandomGeode extends RandomisedFeature {
    public RandomGeode(RandomFeaturesList parent) {
        super(parent, "geode");
        type = "geode";
        save(1 + random.nextInt(32));
    }

    NbtCompound feature() {
        NbtCompound config = new NbtCompound();
        NbtCompound blocks = new NbtCompound();
        blocks.put("filling_provider", PROVIDER.randomBlockProvider(random, RandomProvider.weighedRandom(random,1, 3)? "airs" : "fluids"));
        blocks.put("inner_layer_provider", PROVIDER.randomBlockProvider(random, "full_blocks"));
        blocks.put("alternate_inner_layer_provider", PROVIDER.randomBlockProvider(random, "full_blocks"));
        blocks.put("middle_layer_provider", PROVIDER.randomBlockProvider(random, "full_blocks"));
        blocks.put("outer_layer_provider", PROVIDER.randomBlockProvider(random, "full_blocks"));
        NbtList inner_placements = new NbtList();
        inner_placements.add(PROVIDER.randomBlock(random, "all_blocks"));
        blocks.put("inner_placements", inner_placements);
        blocks.putString("cannot_replace", PROVIDER.randomName(random, "tags"));
        blocks.putString("invalid_blocks", PROVIDER.randomName(random, "tags"));
        config.put("blocks", blocks);
        NbtCompound layers = new NbtCompound();
        double r = 1.0;
        for (String str: new String[]{"filling", "inner_layer", "middle_layer", "outer_layer"}) {
            r += random.nextExponential();
            layers.putDouble(str, r);
        }
        config.put("layers", layers);
        NbtCompound crack = new NbtCompound();
        crack.putDouble("generate_crack_chance", random.nextDouble());
        crack.putDouble("base_crack_size", random.nextDouble()*5);
        crack.putInt("crack_point_offset", random.nextInt(11));
        config.put("crack", crack);
        config.putDouble("noise_multiplier", Math.min(1.0, random.nextExponential()*0.1));
        config.putDouble("use_potential_placements_chance", random.nextDouble());
        config.putDouble("use_alternate_layer0_chance:", random.nextDouble());
        config.putBoolean("placements_require_layer0_alternate", random.nextBoolean());
        config.putInt("invalid_blocks_threshold", 1 + random.nextInt(16));
        return feature(config);
    }
}
