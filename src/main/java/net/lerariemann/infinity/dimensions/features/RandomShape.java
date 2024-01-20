package net.lerariemann.infinity.dimensions.features;

import net.lerariemann.infinity.dimensions.RandomDimension;
import net.lerariemann.infinity.dimensions.RandomFeaturesList;
import net.lerariemann.infinity.dimensions.RandomProvider;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import java.util.Objects;

public class RandomShape extends RandomisedFeature {
    String shape;
    boolean usePreset;
    boolean useBands;

    public RandomShape(RandomFeaturesList parent, String shape) {
        super(parent, shape);
        this.shape = shape;
        this.usePreset = parent.roll("colourful_shapes");
        this.useBands = parent.roll("banded_shapes");
        id = (Objects.equals(shape, "cube")) ? "random_cube" : "random_shape";
        type = "floating";
        RandomDimension dim = parent.parent.parent;
        int a = (int)random.nextGaussian(dim.sea_level, 16);
        int b = random.nextInt(dim.sea_level, dim.height + dim.min_y);
        int min_inclusive = Math.min(b, Math.max(a, dim.min_y));
        int max_inclusive = Math.max(a, b);
        save(1 + random.nextInt(64), min_inclusive, max_inclusive);
    }

    NbtCompound feature() {
        NbtCompound config = new NbtCompound();
        NbtList replaceable = new NbtList();
        replaceable.add(RandomProvider.Block(parent.parent.parent.default_fluid.getString("Name")));
        config.put("replaceable", replaceable);
        if (!usePreset) addRandomBlockProvider(config, "block_provider", "full_blocks_worldgen");
        else config.put("block_provider", PROVIDER.randomPreset(random, useBands ? "weighted_state_provider" : "noise_provider"));
        config.put("radius", RandomProvider.floatProvider(random, 2.0f, 20.0f));
        config.putBoolean("use_bands", useBands);
        if (!Objects.equals(shape, "cube")) config.putDouble("power", Objects.equals(shape, "sphere") ? 2.0 :
                (Objects.equals(shape, "octahedron") ? 1.0 : random.nextDouble(2.0)));
        return feature(config);
    }
}
