package com.heaser.pipeconnector.recipe;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;

public class SupportedPipesRecipe implements Recipe<SimpleContainer> {
    private final ResourceLocation id;
    private final String pattern;

    public SupportedPipesRecipe(ResourceLocation id, String pattern) {
        this.id = id;
        this.pattern = pattern;
    }

    public String getPattern() {
        return pattern;
    }

    public ResourceLocation getId() {
        return id;
    }

    @Override
    public boolean matches(SimpleContainer inv, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(SimpleContainer inv, RegistryAccess registry) {
        return null;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }
    @Override
    public ItemStack getResultItem(RegistryAccess registry) {
        return null;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SupportedPipesRecipe.Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return RecipeType.simple(id);
    }

    public static class Serializer implements RecipeSerializer<SupportedPipesRecipe> {
        public static final SupportedPipesRecipe.Serializer INSTANCE = new Serializer();

        @Override
        public SupportedPipesRecipe fromJson(ResourceLocation id, JsonObject json) {
            return null;
        }

        @Override
        public SupportedPipesRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            return null;
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, SupportedPipesRecipe recipe) {
        }
    }

}
