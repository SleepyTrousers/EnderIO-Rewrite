package com.enderio.decoration.client.model.painted;

import com.enderio.core.client.RenderUtil;
import com.enderio.core.data.model.EIOModel;
import com.enderio.decoration.common.blockentity.IPaintableBlockEntity;
import com.enderio.decoration.common.blockentity.SinglePaintedBlockEntity;
import com.enderio.decoration.common.util.PaintUtils;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.IDynamicBakedModel;
import net.minecraftforge.client.model.IQuadTransformer;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.*;

// TODO: 1.19: Delete after checks.
public abstract class PaintedModel implements IDynamicBakedModel {

    private final Map<Block, List<BakedModel>> itemRenderCache = new HashMap<>();

    private final ItemTransforms transforms;

    @Nullable
    private final Direction rotateItemTo;

    protected PaintedModel(ItemTransforms transforms, @Nullable Direction rotateItemTo) {
        this.transforms = transforms;
        this.rotateItemTo = rotateItemTo;
    }

    protected abstract Block copyModelFromBlock();

    protected BakedModel getModel(BlockState state) {
        return Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
    }

    protected BakedModel getModelFromOwn(BlockState ownBlockState) {
        return getModel(copyBlockState(ownBlockState));
    }

    /**
     * @param paint
     * @param shape    quads you want to copy
     * @param side     side to render
     * @param rand
     * @param rotation rotation you want to have applied to the block
     * @return a List of BakedQuads from a shape using the paint as a texture
     */
    protected List<BakedQuad> getQuadsUsingShape(@Nullable Block paint, List<BakedQuad> shape, @Nullable Direction side, @Nonnull RandomSource rand,
        @Nullable Direction rotation, RenderType renderType) {
        if (paint != null) {
            BakedModel model = getModel(paintWithRotation(paint, rotation));
            Optional<Pair<TextureAtlasSprite, Boolean>> spriteOptional = getSpriteData(paint, side, rand, rotation, renderType);
            List<BakedQuad> returnQuads = new ArrayList<>();
            for (BakedQuad shapeQuad : shape) {
                Pair<TextureAtlasSprite, Boolean> spriteData = spriteOptional.orElseGet(() -> getSpriteFromModel(shapeQuad, model, paint, rotation));
                returnQuads.add(copyQuad(shapeQuad, spriteData.getFirst(), spriteData.getSecond()));
            }
            return returnQuads;
        }
        return List.of();
    }

    private BlockState paintWithRotation(Block paint, Direction rotation) {
        BlockState state = paint.defaultBlockState();
        if (rotation != null) {
            for (Property<?> property : state.getProperties()) {
                if (property instanceof DirectionProperty directionProperty && directionProperty.getPossibleValues().contains(rotation)) {
                    state = state.setValue(directionProperty, rotation);
                }
            }
        }
        return state;
    }

    /**
     * @param paint
     * @param side     the side you want the texture from
     * @param rand
     * @param rotation a rotation value, so that if both blocks support rotation, the correct texture is gathered
     * @return an Optional of a Pair of the texture of the Block and if the texture is tinted at that side
     */
    private Optional<Pair<TextureAtlasSprite, Boolean>> getSpriteData(Block paint, Direction side, RandomSource rand, Direction rotation, RenderType renderType) {
        BlockState state = paintWithRotation(paint, rotation);
        List<BakedQuad> quads = getModel(state).getQuads(state, side, rand, ModelData.EMPTY, renderType);
        return quads.isEmpty() ? Optional.empty() : Optional.of(Pair.of(quads.get(0).getSprite(), quads.get(0).isTinted()));
    }

    /**
     * A fallback for {@link this.getSpriteData}. Mostly used for if the Original Block doesn't have a texture for the null side.
     * That is the case for all textures not on the faces of a full block.
     * This method uses the BakedQuad of the Shape to unpack it's VertextData. This time at Element 4 which represents NormalData (Direction Data).
     * For more information of the Unpacking of data take a look at {@link this.copyQuad}. After that the nearest direction for the normal data is used to query the blockmodel again to hopefully get the correct texturedata.
     * If it can't find a correct Quad for that direction the missing texture is returned.
     *
     * @param shape
     * @param model
     * @param paint
     * @param rotation
     * @return Returns TextureData from baked model information. Is slower than the primary method, so this is just a fallback.
     */
    protected Pair<TextureAtlasSprite, Boolean> getSpriteFromModel(BakedQuad shape, BakedModel model, Block paint, Direction rotation) {
        BlockState state = paintWithRotation(paint, rotation);
        List<BakedQuad> quads = model.getQuads(state, shape.getDirection(), RandomSource.create());
        return quads.isEmpty() ? Pair.of(EIOModel.getMissingTexture(), false) : Pair.of(quads.get(0).getSprite(), quads.get(0).isTinted());
    }


    /**
     * copies Painted BlockState Properties to shape for enum Properties and BooleanProperties to use the correct stair for shape etc.
     *
     * @param ownBlockState paint
     * @return shape
     */
    protected BlockState copyBlockState(BlockState ownBlockState) {
        BlockState toState = copyModelFromBlock().defaultBlockState();
        if (ownBlockState == null)
            return toState;
        for (Property<?> property : ownBlockState.getProperties()) {
            if (property instanceof BooleanProperty booleanProperty && toState.hasProperty(booleanProperty)) {
                toState = toState.setValue(booleanProperty, ownBlockState.getValue(booleanProperty));
            }
            if (property instanceof EnumProperty enumProperty && toState.hasProperty(enumProperty)) {
                toState = toState.setValue(enumProperty, ownBlockState.getValue(enumProperty));
            }
        }
        return toState;
    }

    /**
     * This method copies a quad from the shape and modifies it to create one, that display the new texture.
     * First it copies the quad with the values of the shape quad. The new Sprite and tintValues are added to the quad.
     * After that this method gets UV-Data out of the shape quad and modifies it to match the texture of the paint with their offsets. This Data is packed into an Int Array in the vertex data.
     * The unpacked representation of the data uses a float[4][6][4] where the first dimension is for each vertex, the second is for the dataType and the third is for different types of values like x1,y1,x2,y2 of uvdata
     * To use the data this method unpacks the UV-Data for each vertex and get element 2 which is for UV-Start-Data (UV0). To get the other element types you can look at {@link DefaultVertexFormat#BLOCK}
     * I modify the unpacked UV-Data by getting the relative offset to the texturestart-coordinates by subtracting it from the texturestart and dividing it by the width of the texture.
     * To get the values for the new textures I multiply that value by the size of the new texture and adding the texture start data pack. I then pack that float data back into the int form and put that into the cloned quad.
     *
     * @param toCopy     shapeQuad you want to copy
     * @param sprite     sprite that should be used
     * @param shouldTint is that quad on the texture tinted
     * @return a new Quad with the same coordinates but a different texture
     */
    protected BakedQuad copyQuad(BakedQuad toCopy, TextureAtlasSprite sprite, boolean shouldTint) {
        BakedQuad copied = new BakedQuad(Arrays.copyOf(toCopy.getVertices(), 32), shouldTint ? 1 : -1, toCopy.getDirection(), sprite, toCopy.isShade());

        for (int i = 0; i < 4; i++) {
            float[] uv0 = RenderUtil.unpackVertices(copied.getVertices(), i, IQuadTransformer.UV0,2);
            uv0[0] = (uv0[0] - toCopy.getSprite().getU0()) * sprite.getWidth() / toCopy.getSprite().getWidth() + sprite.getU0();
            uv0[1] = (uv0[1] - toCopy.getSprite().getV0()) * sprite.getHeight() / toCopy.getSprite().getHeight() + sprite.getV0();
            int[] packedTextureData = RenderUtil.packUV(uv0[0], uv0[1]);
            copied.getVertices()[4 + i * 8] = packedTextureData[0];
            copied.getVertices()[5 + i * 8] = packedTextureData[1];
        }
        return copied;
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return false;
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

    /**
     * @param data
     * @return the particleIcon used for the breaking animation and other. Currently not used properly by Forge. A PR is in the works
     */
    @Override
    public TextureAtlasSprite getParticleIcon(@Nonnull ModelData data) {
        Block paint = data.get(SinglePaintedBlockEntity.PAINT);
        if (paint != null) {
            BakedModel model = getModel(paint.defaultBlockState());
            return model.getParticleIcon(ModelData.EMPTY);
        }
        return EIOModel.getMissingTexture();
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return EIOModel.getMissingTexture();
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(BlockState paintedBlockState, RandomSource rand, ModelData data) {
        ChunkRenderTypeSet chunkRenderTypeSet = ChunkRenderTypeSet.union(
            IPaintableBlockEntity.PAINT_DATA_PROPERTIES.stream()
                    .map(data::get)
                    //remove all unset paints
                    .filter(Objects::nonNull)
                    .map(Block::defaultBlockState)
                    //do original ChunkRenderType lookup
                    .map(state -> getModel(state).getRenderTypes(state, rand, ModelData.EMPTY))
                    .toList());

        if (chunkRenderTypeSet.isEmpty()) {
            return ChunkRenderTypeSet.of(RenderType.solid());
        }
        return chunkRenderTypeSet;
    }

    @Override
    public List<RenderType> getRenderTypes(ItemStack itemStack, boolean fabulous) {
        @Nullable
        Block paint = PaintUtils.getPaint(itemStack);
        if (paint != null) {
            return List.of(ItemBlockRenderTypes.getRenderType(paint.defaultBlockState(), fabulous));
        }
        return List.of(fabulous ? Sheets.translucentCullBlockSheet() : Sheets.translucentItemSheet());
    }

    @Override
    public List<BakedModel> getRenderPasses(ItemStack itemStack, boolean fabulous) {
        Block paint = PaintUtils.getPaint(itemStack);
        if (paint != null) {
            return itemRenderCache.computeIfAbsent(paint,
                paintKey -> List.of(new ItemPaintedModel(paint, copyModelFromBlock(), rotateItemTo))
            );
        }
        return List.of(this);
    }

    @Override
    public ItemTransforms getTransforms() {
        return transforms;
    }

    private class ItemPaintedModel implements IDynamicBakedModel {

        private final Block paint;
        private final Block shapeFrom;
        private final Map<Direction, List<BakedQuad>> bakedQuads = new HashMap<>();
        @Nullable
        private final Direction rotateItemTo;

        private ItemPaintedModel(Block paint, Block shapeFrom, @Nullable Direction rotateItemTo) {
            this.paint = paint;
            this.shapeFrom = shapeFrom;
            this.rotateItemTo = rotateItemTo;
        }

        @Nonnull
        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData extraData, RenderType renderType) {
            return bakedQuads.computeIfAbsent(side, side1 -> PaintedModel.this.getQuadsUsingShape(
                paint,
                Minecraft
                    .getInstance()
                    .getItemRenderer()
                    .getModel(shapeFrom.asItem().getDefaultInstance(), null, null, 0)
                    .getQuads(state, side, rand, ModelData.EMPTY, renderType),
                side, rand, rotateItemTo, renderType));
        }

        @Override
        public boolean useAmbientOcclusion() {
            return false;
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
            return PaintedModel.this.getParticleIcon();
        }

        @Override
        public ItemOverrides getOverrides() {
            return ItemOverrides.EMPTY;
        }

        @Override
        public ItemTransforms getTransforms() {
            return transforms;
        }
    }
}
