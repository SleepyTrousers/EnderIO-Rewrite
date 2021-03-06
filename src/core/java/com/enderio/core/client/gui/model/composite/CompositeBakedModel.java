package com.enderio.core.client.gui.model.composite;

import com.enderio.core.client.gui.model.ItemTransformUtil;
import com.enderio.core.data.model.EIOModel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class CompositeBakedModel implements IDynamicBakedModel {

    private final List<BakedModel> components;

    @Nullable
    private final Supplier<TextureAtlasSprite> particleSupplier;

    public CompositeBakedModel(List<BakedModel> components, @Nullable Supplier<TextureAtlasSprite> particleSupplier) {
        this.components = components;
        this.particleSupplier = particleSupplier;
    }

    @NotNull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull IModelData extraData) {
        // Get all component quads
        List<BakedQuad> quads = new ArrayList<>();
        for (BakedModel model : components) {
            quads.addAll(model.getQuads(state, side, rand, extraData));
        }
        return quads;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

    @Override
    public boolean usesBlockLight() {
        return true;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        if (particleSupplier != null)
            return particleSupplier.get();
        return EIOModel.getMissingTexture();
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }

    @Override
    public ItemTransforms getTransforms() {
        return ItemTransformUtil.DEFAULT;
    }
}
