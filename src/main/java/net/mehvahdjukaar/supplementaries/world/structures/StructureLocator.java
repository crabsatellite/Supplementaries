package net.mehvahdjukaar.supplementaries.world.structures;

import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.SectionPos;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraft.world.gen.settings.StructureSeparationSettings;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StructureLocator {

    private static final List<Structure<?>> TARGETS = new ArrayList<>();

    public static void init(){
        for(String name : ServerConfigs.spawn.SIGNS_VILLAGES.get()){
            ResourceLocation res = new ResourceLocation(name);
            if(ForgeRegistries.STRUCTURE_FEATURES.containsKey(res))
            TARGETS.add(ForgeRegistries.STRUCTURE_FEATURES.getValue(res));
        }
    }


    private static int dist(BlockPos pos1, BlockPos pos2) {
        int i = pos2.getX() - pos1.getX();
        int j = pos2.getZ() - pos1.getZ();
        return MathHelper.floor(MathHelper.sqrt((float)(i * i + j * j)));
    }

    public static Pair<List<Pair<Integer, BlockPos>>,Boolean> find(ServerWorld world, BlockPos pos, int range, int count){
        //TreeMap<Integer,BlockPos> found = new TreeMap<>();
        List<Pair<Integer,BlockPos>> found = new ArrayList<>();

        //TODO: add to structure biome event

        boolean inVillage = false;

        if(world.getServer().getWorldData().worldGenSettings().generateFeatures()){


            ChunkGenerator gen = world.getChunkSource().getGenerator();
            BiomeProvider biomeSource = gen.getBiomeSource();

            List<Structure<?>> possibleTargets = new ArrayList<>();
            List<StructureSeparationSettings>  sepSettings = new ArrayList<>();


            //TODO: cache some of this
            for(Structure<?> str : TARGETS){
                if(biomeSource.canGenerateStructure(str)){
                    StructureSeparationSettings sep = gen.getSettings().getConfig(str);
                    if(sep!=null){
                        possibleTargets.add(str);
                        sepSettings.add(sep);
                    }
                }
            }


            if (sepSettings != null) {

                long seed = world.getSeed();

                StructureManager manager = world.structureFeatureManager();


                int chunkX = pos.getX() >> 4;
                int chunkY = pos.getZ() >> 4;
                int r = 0;

                //checks in ever growing circles by increasing radius r
                for (SharedSeedRandom sharedseedrandom = new SharedSeedRandom(); r <= range; ++r) {

                    for (int ind = 0; ind < possibleTargets.size(); ind++) {

                        for (int x = -r; x <= r; ++x) {
                            boolean edgeX = x == -r || x == r;

                            for (int y = -r; y <= r; ++y) {
                                boolean edgeY = y == -r || y == r;
                                if (edgeX || edgeY) {

                                    StructureSeparationSettings settings = sepSettings.get(ind);
                                    Structure<?> s = possibleTargets.get(ind);

                                    int spacing = settings.spacing();

                                    int k1 = chunkX + spacing * x;
                                    int l1 = chunkY + spacing * y;
                                    ChunkPos chunkpos = s.getPotentialFeatureChunk(settings, seed, sharedseedrandom, k1, l1);
                                    IChunk ichunk = world.getChunk(chunkpos.x, chunkpos.z, ChunkStatus.STRUCTURE_STARTS);
                                    StructureStart<?> structureStart = manager.getStartForFeature(SectionPos.of(ichunk.getPos(), 0), s, ichunk);
                                    if (structureStart != null && structureStart.isValid()) {
                                        BlockPos p = structureStart.getLocatePos();
                                        int distance = dist(pos, p);
                                        //discard one spawning in a village
                                        if (distance > 90) found.add(new ImmutablePair<>(distance, p));
                                        else inVillage = true;
                                        //checking all nearby villages to find the closest
                                    }

                                    if (r == 0) {
                                        break;
                                    }
                                    //less precision at long distances for performance
                                    if (r > 5 && found.size() >= count) break;
                                }
                            }

                            if (r == 0) {
                                break;
                            }
                            if (r > 8 && found.size() >= count) break;

                        }
                    }
                    //exit loop
                    if (found.size() >= count) break;
                }

            }
        }

        //sort

        Collections.sort(found);
        return new ImmutablePair<>(found,inVillage);
    }



}
