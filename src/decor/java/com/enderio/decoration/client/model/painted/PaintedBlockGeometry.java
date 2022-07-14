package com.enderio.decoration.client.model.painted;

import com.enderio.decoration.common.util.PaintUtils;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Function;

public class PaintedBlockGeometry implements IUnbakedGeometry<PaintedBlockGeometry> {
    private final Block reference;

    @Nullable
    private final Direction rotateItemTo;

    public PaintedBlockGeometry(Block reference, @Nullable Direction rotateItemTo) {
        this.reference = reference;
        this.rotateItemTo = rotateItemTo;
    }

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform,
        ItemOverrides overrides, ResourceLocation modelLocation) {
        return new PaintedBlockModel(reference, rotateItemTo);
    }

    @Override
    public Collection<Material> getMaterials(IGeometryBakingContext context, Function<ResourceLocation, UnbakedModel> modelGetter,
        Set<Pair<String, String>> missingTextureErrors) {
        return Collections.singletonList(new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("minecraft", "missingno")));
    }

    public static class Loader implements IGeometryLoader<PaintedBlockGeometry> {
        @Override
        public PaintedBlockGeometry read(JsonObject jsonObject, JsonDeserializationContext deserializationContext) throws JsonParseException {
            return new PaintedBlockGeometry(PaintUtils.getBlockFromRL(jsonObject.get("reference").getAsString()), getItemTextureRotation(jsonObject));
        }

        @Nullable
        private static Direction getItemTextureRotation(JsonObject jsonObject) {
            if (jsonObject.has("item_texture_rotation")) {
                return Arrays.stream(Direction.values())
                    .filter(dir -> dir.getName().equals(jsonObject.get("item_texture_rotation").getAsString()))
                    .findFirst()
                    .orElse(null);
            }
            return null;
        }
    }
}
