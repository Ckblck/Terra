package com.dfsek.terra.world.generation.generators;

import com.dfsek.terra.api.TerraPlugin;
import com.dfsek.terra.api.math.range.ConstantRange;
import com.dfsek.terra.api.block.BlockData;
import com.dfsek.terra.api.world.BiomeGrid;
import com.dfsek.terra.api.world.TerraWorld;
import com.dfsek.terra.api.world.World;
import com.dfsek.terra.api.world.generator.ChunkData;
import com.dfsek.terra.api.util.PaletteUtil;
import com.dfsek.terra.api.world.biome.TerraBiome;
import com.dfsek.terra.api.world.biome.UserDefinedBiome;
import com.dfsek.terra.api.world.biome.generation.BiomeProvider;
import com.dfsek.terra.api.world.generator.Palette;
import com.dfsek.terra.api.world.generator.SamplerCache;
import com.dfsek.terra.api.world.generator.TerraBlockPopulator;
import com.dfsek.terra.api.world.generator.TerraChunkGenerator;
import com.dfsek.terra.config.pack.ConfigPackImpl;
import com.dfsek.terra.config.templates.BiomeTemplate;
import com.dfsek.terra.api.profiler.ProfileFrame;
import com.dfsek.terra.world.Carver;
import com.dfsek.terra.world.carving.NoiseCarver;
import com.dfsek.terra.api.world.generator.Sampler;
import com.dfsek.terra.world.generation.math.samplers.Sampler2D;
import com.dfsek.terra.world.population.CavePopulator;
import com.dfsek.terra.world.population.OrePopulator;
import com.dfsek.terra.world.population.StructurePopulator;
import com.dfsek.terra.world.population.TreePopulator;
import net.jafama.FastMath;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DefaultChunkGenerator2D implements TerraChunkGenerator {
    private final ConfigPackImpl configPack;
    private final TerraPlugin main;

    private final Carver carver;
    private final List<TerraBlockPopulator> blockPopulators = new ArrayList<>();

    private final SamplerCache cache;

    public DefaultChunkGenerator2D(ConfigPackImpl c, TerraPlugin main, SamplerCache cache) {
        this.configPack = c;
        this.main = main;
        blockPopulators.add(new CavePopulator(main));
        blockPopulators.add(new StructurePopulator(main));
        blockPopulators.add(new OrePopulator(main));
        blockPopulators.add(new TreePopulator(main));
        blockPopulators.add(new TreePopulator(main));
        carver = new NoiseCarver(new ConstantRange(0, 255), main.getWorldHandle().createBlockData("minecraft:air"), main);
        this.cache = cache;
    }

    @Override
    public ConfigPackImpl getConfigPack() {
        return configPack;
    }

    @Override
    public TerraPlugin getMain() {
        return main;
    }

    @Override
    @SuppressWarnings({"try"})
    public ChunkData generateChunkData(@NotNull World world, Random random, int chunkX, int chunkZ, ChunkData chunk) {
        TerraWorld tw = main.getWorld(world);
        BiomeProvider grid = tw.getBiomeProvider();
        try(ProfileFrame ignore = main.getProfiler().profile("chunk_base_2d")) {
            if(!tw.isSafe()) return chunk;
            int xOrig = (chunkX << 4);
            int zOrig = (chunkZ << 4);

            Sampler sampler = cache.getChunk(chunkX, chunkZ);

            for(int x = 0; x < 16; x++) {
                for(int z = 0; z < 16; z++) {
                    int paletteLevel = 0;
                    int seaPaletteLevel = 0;

                    int cx = xOrig + x;
                    int cz = zOrig + z;

                    TerraBiome b = grid.getBiome(xOrig + x, zOrig + z);
                    BiomeTemplate c = ((UserDefinedBiome) b).getConfig();

                    Palette seaPalette = c.getOceanPalette();

                    int height = FastMath.min((int) sampler.sample(x, 0, z), world.getMaxHeight() - 1);

                    for(int y = FastMath.max(height, c.getSeaLevel()); y >= 0; y--) {
                        BlockData data = y > height ? seaPalette.get(seaPaletteLevel++, cx, y, cz) : PaletteUtil.getPalette(x, y, z, c, sampler).get(paletteLevel++, cx, y, cz);
                        chunk.setBlock(x, y, z, data);
                    }
                }
            }
            if(configPack.getTemplate().doBetaCarvers()) {
                carver.carve(world, chunkX, chunkZ, chunk);
            }
            return chunk;
        }
    }

    @Override
    public void generateBiomes(@NotNull World world, @NotNull Random random, int chunkX, int chunkZ, @NotNull BiomeGrid biome) {
        DefaultChunkGenerator3D.biomes(world, chunkX, chunkZ, biome, main);
    }

    @Override
    public Sampler createSampler(int chunkX, int chunkZ, BiomeProvider provider, World world, int elevationSmooth) {
        return new Sampler2D(chunkX, chunkZ, provider, world, elevationSmooth);
    }

    @Override
    public List<TerraBlockPopulator> getPopulators() {
        return blockPopulators;
    }
}
