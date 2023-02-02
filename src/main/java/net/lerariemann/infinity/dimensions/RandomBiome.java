package net.lerariemann.infinity.dimensions;

import net.lerariemann.infinity.InfinityMod;
import net.minecraft.nbt.*;

import java.util.*;

public class RandomBiome {
    public RandomDimension parent;
    public final RandomProvider PROVIDER;
    public int id;
    public String name;
    public String fullname;
    public final Random random;
    public List<String> mobs;

    RandomBiome(int i, RandomDimension dim) {
        id = i;
        parent = dim;
        random = new Random(i);
        PROVIDER = dim.PROVIDER;
        name = "biome_" +i;
        fullname = InfinityMod.MOD_ID + ":" + name;
        mobs = new ArrayList<>();
        NbtCompound res = new NbtCompound();
        res.putDouble("temperature", -1 + random.nextFloat()*3);
        res.putString("precipitation", PROVIDER.randomName(random, "precipitation"));
        res.putString("temperature_modifier", RandomProvider.weighedRandom(random,3, 1) ? "none" : "frozen");
        res.putDouble("downfall", random.nextDouble());
        res.put("effects", randomEffects());
        if (random.nextBoolean()) res.putFloat("creature_spawn_probability", Math.min(random.nextFloat(), 0.9999999f));
        res.put("spawners", randomMobs());
        res.put("spawn_costs", spawnCosts());
        res.put("features", (new RandomFeaturesList(this).data));
        res.put("carvers", new NbtCompound());
        CommonIO.write(res, dim.storagePath + "/worldgen/biome", name + ".json");
    }

    public NbtInt randomColor() {
        return NbtInt.of(random.nextInt(16777216));
    }
    public NbtList randomDustColor() {
        NbtList res = new NbtList();
        res.add(NbtDouble.of(random.nextDouble()));
        res.add(NbtDouble.of(random.nextDouble()));
        res.add(NbtDouble.of(random.nextDouble()));
        return res;
    }

    NbtString randomSound(){
        return NbtString.of(PROVIDER.randomName(random, "sounds"));
    }

    NbtCompound randomEffects() {
        NbtCompound res = new NbtCompound();
        res.put("fog_color", randomColor());
        res.put("sky_color", randomColor());
        res.put("water_color", randomColor());
        res.put("water_fog_color", randomColor());
        if (random.nextBoolean()) res.put("foliage_color", randomColor());
        if (random.nextBoolean()) res.put("grass_color", randomColor());
        if (random.nextBoolean()) res.put("grass_color_modifier", NbtString.of(random.nextBoolean() ? "dark_forest" : "swamp"));
        if (random.nextBoolean()) res.put("particle", randomParticle());
        if (RandomProvider.weighedRandom(random, 15, 1)) res.put("ambient_sound", randomSound());
        if (RandomProvider.weighedRandom(random, 7, 1)) res.put("mood_sound", randomMoodSound());
        if (RandomProvider.weighedRandom(random, 7, 1)) res.put("additions_sound", randomAdditionSound());
        res.put("music", randomMusic());
        return res;
    }

    NbtCompound randomMoodSound() {
        NbtCompound res = new NbtCompound();
        res.put("sound", randomSound());
        res.putInt("tick_delay", random.nextInt(6000));
        res.putInt("block_search_extent", random.nextInt(32));
        res.putDouble("offset", random.nextDouble()*8);
        return res;
    }

    NbtCompound randomAdditionSound(){
        NbtCompound res = new NbtCompound();
        res.put("sound", randomSound());
        res.putDouble("tick_chance", random.nextExponential()*0.01);
        return res;
    }

    NbtCompound randomMusic(){
        NbtCompound res = new NbtCompound();
        res.put("sound", NbtString.of(PROVIDER.randomName(random, "music")));
        int a = random.nextInt(0, 24000);
        int b = random.nextInt(0, 24000);
        res.putInt("min_delay", Math.min(a,b));
        res.putInt("max_delay", Math.max(a,b));
        res.putBoolean("replace_current_music", random.nextBoolean());
        return res;
    }

    NbtCompound randomParticle(){
        NbtCompound res = new NbtCompound();
        res.putFloat("probability", (float)(random.nextExponential()*0.01));
        res.put("options", particleOptions());
        return res;
    }

    NbtCompound particleOptions() {
        NbtCompound res = new NbtCompound();
        String particle = PROVIDER.randomName(random, "particles");
        res.putString("type", particle);
        switch(particle) {
            case "minecraft:block", "minecraft:block_marker", "minecraft:falling_dust" -> {
                NbtCompound value = new NbtCompound();
                value.putString("Name", PROVIDER.randomName(random, "all_blocks"));
                res.put("value", value);
                return res;
            }
            case "minecraft:item" -> {
                NbtCompound value = new NbtCompound();
                value.putString("Name", PROVIDER.randomName(random, "items"));
                res.put("value", value);
                return res;
            }
            case "minecraft:dust" -> {
                res.put("color", randomDustColor());
                res.putFloat("scale", random.nextFloat());
                return res;
            }
            case "minecraft:dust_color_transition" -> {
                res.put("fromColor", randomDustColor());
                res.put("toColor", randomDustColor());
                res.putFloat("scale", random.nextFloat());
                return res;
            }
            case "minecraft:sculk_charge" -> {
                res.putFloat("roll", (float)(random.nextFloat()*Math.PI));
                return res;
            }
            case "minecraft:shriek" -> {
                res.putInt("delay", random.nextInt(500));
                return res;
            }
        }
        return res;
    }

    NbtCompound randomMobs() {
        Map<String, NbtList> lists = new HashMap<>();
        String[] titles = {"monster", "creature", "ambient", "water_creature", "underground_water_creature", "water_ambient", "misc", "axolotls"};
        for (int i = 0; i < 8; i++) {
            lists.put(titles[i], new NbtList());
        }
        int mobCount = random.nextInt(20);
        for(int i = 0; i < mobCount; i++) {
            NbtCompound mob = new NbtCompound();
            String mobname = PROVIDER.randomName(random, "mobs");
            mob.putString("type", mobname);
            mobs.add(mobname);
            mob.putInt("weight", 1 + random.nextInt(20));
            int a = 1 + random.nextInt(6);
            int b = 1 + random.nextInt(6);
            mob.putInt("minCount", Math.min(a, b));
            mob.putInt("maxCount", Math.max(a, b));
            lists.get(PROVIDER.randomName(random, "mob_categories")).add(mob);
        }
        NbtCompound res = new NbtCompound();
        for (int i = 0; i < 8; i++) res.put(titles[i], lists.get(titles[i]));
        return res;
    }

    NbtCompound spawnCosts() {
        NbtCompound res = new NbtCompound();
        for (String mob: mobs) {
            NbtCompound mobData = new NbtCompound();
            mobData.putDouble("energy_budget", random.nextDouble()*0.3);
            mobData.putDouble("charge", 0.5 + random.nextDouble());
            res.put(mob, mobData);
        }
        return res;
    }
}
