package com.enderio.decoration.common.block.painted;

import com.enderio.decoration.common.init.DecorBlocks;
import com.enderio.decoration.common.init.DecorBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PaintedStairBlock extends StairBlock implements EntityBlock {

    public PaintedStairBlock(Properties properties) {
        super(() -> DecorBlocks.PAINTED_SAND.get().defaultBlockState(), properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        return DecorBlockEntities.SINGLE_PAINTED.create(pos, state);
    }
}
