package architectspalette.content.worldgen.features;

import architectspalette.content.worldgen.features.configs.CrystalClusterConfig;
import com.mojang.math.Vector3f;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

import java.util.Random;

public class CrystalClusterFeature extends Feature<CrystalClusterConfig> {

    public CrystalClusterFeature(Codec<CrystalClusterConfig> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<CrystalClusterConfig> context) {

        //Check for free space
        if (!context.level().isEmptyBlock(context.origin())) return false;
        if (context.config().hanging) {
            //Check for being on a ceiling
            if (context.level().isEmptyBlock(context.origin().above())) return false;
        }
        else {
            //Check for being on a floor
            if (context.level().isEmptyBlock(context.origin().below())) return false;
        }

        CrystalClusterConfig config = context.config();
        Random random = context.random();

        //Set horizontal angle used to skew the shelves of crystals
        Vector3f shelfAngle = Vector3f.XN.copy();
        shelfAngle.transform(Vector3f.YP.rotationDegrees(random.nextFloat(360)));

        //Set horizontal angle that determines the spaces between shelves
        Vector3f formationAngle = shelfAngle.copy();
        formationAngle.transform(Vector3f.YP.rotationDegrees(fRandomRange(random,-15, 15) + 90));

        Vector3f placePos = new Vector3f(context.origin().getX(), context.origin().getY(), context.origin().getZ());

        //4-7 shelves
        int shelves = iRandomRange(random, 4, 7);
        for (int i = 0; i < shelves; i++) {
            float scale = ((float)i+1)/shelves;
            //1-7 pillars each shelf
            placeShelf(new BlockPos(placePos.x(), placePos.y(), placePos.z()), iRandomRange(random,1, 7), shelfAngle, scale, context.level(), config, random);

            //Offset next shelf position
            formationAngle.normalize();
            formationAngle.mul(fRandomRange(random, .5f, 2));
            placePos.add(formationAngle);
        }

        return true;
    }

    private static void placeShelf(BlockPos startPos, int crystals, Vector3f shelfAngle, float shelfScale, WorldGenLevel world, CrystalClusterConfig config, Random random) {
        int flip = config.hanging ? 1 : -1;

        Vector3f placePos = new Vector3f(startPos.getX(), startPos.getY(), startPos.getZ());
        placePos.add(0, -2 * flip, 0);

        for (int i = 0; i < crystals; i++) {

            Vector3f offset = shelfAngle.copy();
            offset.mul(fRandomRange(random, .5f, 2.5f));

            placePos.add(offset);

//            int pillarLength = config.minLength + (int)(Math.floor(random.nextInt((config.maxLength - config.minLength) + 1) * shelfScale));
            int pillarLength = iRandomRange(random, 0, (int)(Math.floor((config.maxLength - config.minLength) * shelfScale))) + config.minLength;
            placePillar(new BlockPos.MutableBlockPos(placePos.x(), placePos.y(), placePos.z()), pillarLength, world, config.crystalState, flip);
            if (pillarLength > (config.maxLength - config.minLength)/2 && random.nextBoolean()) {
//                placePillar(new BlockPos.MutableBlockPos(placePos.x() + random.nextInt(2), placePos.y() + random.nextInt(2), placePos.z() + random.nextInt(2)), pillarLength / 2, world, config.crystalState);
                placePillar(new BlockPos.MutableBlockPos(placePos.x() + iRandomRange(random, -1, 1), placePos.y() + iRandomRange(random, -1, 1), placePos.z() + iRandomRange(random, -1, 1)), pillarLength / 2, world, config.crystalState, flip);
            }
        }
    }

    private static void placePillar(BlockPos.MutableBlockPos placePos, int length, WorldGenLevel world, BlockState crystal, int flip) {
        // Go up until a ceiling is found, give up if no ceiling found
        int tries = 10;
        while (canReplaceAt(world, placePos)) {
            placePos.setY(placePos.getY() + flip);
            if (world.getBlockState(placePos).is(Blocks.LAVA)) return;
            if (tries-- == 0) {
                return;
            }
        }
        //Move out of ceilings
        tries = 5;
        while (!canReplaceAt(world, placePos)) {
            placePos.setY(placePos.getY() - flip);
            if (tries-- == 0) {
                return;
            }
        }
        //Place Pillar
        while (--length >= 0) {
            if (!canReplaceAt(world, placePos)) return;
            world.setBlock(placePos, crystal, 2);
            placePos.setY(placePos.getY() - flip);
        }
    }

    private static int iRandomRange(Random random, int min, int max) {
        return min + (random.nextInt((max - min) + 1));
    }

    private static float fRandomRange(Random random, float min, float max) {
        return min + (random.nextFloat((max - min)));
    }

    private static boolean canReplaceAt(WorldGenLevel level, BlockPos pos) {
        return canReplace(level.getBlockState(pos));
    }

    private static boolean canReplace(BlockState state) {
        return state.isAir() || state.getMaterial().isReplaceable();
    }
}
