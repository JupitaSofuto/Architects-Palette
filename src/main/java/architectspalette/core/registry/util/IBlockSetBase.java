package architectspalette.core.registry.util;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

public interface IBlockSetBase {
    default Block getBlockForPart(StoneBlockSet.SetComponent part, BlockBehaviour.Properties properties, Block base) {
        return StoneBlockSet.getBlockForPart(part, properties, base);
    }
}
