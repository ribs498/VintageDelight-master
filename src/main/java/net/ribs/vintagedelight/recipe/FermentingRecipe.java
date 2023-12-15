package net.ribs.vintagedelight.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.ribs.vintagedelight.VintageDelight;

public class FermentingRecipe implements Recipe<SimpleContainer> {
    private static final int FIRST_INPUT_SLOT = 0;
    private static final int LAST_INPUT_SLOT = 5;
    private static final int CONTAINER_SLOT = 6;
    private final NonNullList<Ingredient> inputItems;
    private final Ingredient containerIngredient;
    private final ItemStack output;
    private final ItemStack secondaryOutput;
    private final int processingTime;
    private final ResourceLocation id;
    private static final int defaultProcessingTime = 100;
    public FermentingRecipe(NonNullList<Ingredient> inputItems, Ingredient containerIngredient, ItemStack output, ItemStack secondaryOutput, int processingTime, ResourceLocation id) {
        this.inputItems = inputItems;
        this.containerIngredient = containerIngredient;
        this.output = output;
        this.secondaryOutput = secondaryOutput;
        this.processingTime = processingTime;
        this.id = id;
    }
    @Override
    public boolean matches(SimpleContainer pContainer, Level pLevel) {
        if (pLevel.isClientSide()) {
            return false;
        }
        NonNullList<Ingredient> requiredIngredients = NonNullList.create();
        requiredIngredients.addAll(inputItems);
        for (int i = FIRST_INPUT_SLOT; i <= LAST_INPUT_SLOT; i++) {
            ItemStack itemInSlot = pContainer.getItem(i);
            if (!itemInSlot.isEmpty()) {
                boolean matched = false;
                for (Ingredient ingredient : requiredIngredients) {
                    if (ingredient.test(itemInSlot)) {
                        requiredIngredients.remove(ingredient);
                        matched = true;
                        break;
                    }
                }
                if (!matched) {
                    return false;
                }
            }
        }
        if (!containerIngredient.isEmpty()) {
            ItemStack containerItem = pContainer.getItem(CONTAINER_SLOT);
            if (!containerIngredient.test(containerItem)) {
                return false;
            }
        }

        return requiredIngredients.isEmpty();
    }

    public ItemStack getSecondaryResultItem() {
        return secondaryOutput != null ? secondaryOutput.copy() : ItemStack.EMPTY;
    }
    public int getProcessingTime() {
        return processingTime;
    }
    @Override
    public NonNullList<Ingredient> getIngredients() {
        return inputItems;
    }
    @Override
    public ItemStack assemble(SimpleContainer pContainer, RegistryAccess pRegistryAccess) {
        return output.copy();
    }
    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return true;
    }
    @Override
    public ItemStack getResultItem(RegistryAccess pRegistryAccess) {
        return output.copy();
    }
    @Override
    public ResourceLocation getId() {
        return id;
    }
    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }
    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }

    public static class Type implements RecipeType<FermentingRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "fermenting";
    }
    public ItemStack getContainerItemStack() {
        return containerIngredient.getItems().length > 0 ? containerIngredient.getItems()[0] : ItemStack.EMPTY;
    }
    public static class Serializer implements RecipeSerializer<FermentingRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID = new ResourceLocation(VintageDelight.MODID, "fermenting");

        @Override
        public FermentingRecipe fromJson(ResourceLocation pRecipeId, JsonObject pSerializedRecipe) {
            ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(pSerializedRecipe, "output"));
            ItemStack secondaryOutput = ItemStack.EMPTY;
            if (pSerializedRecipe.has("secondaryOutput")) {
                secondaryOutput = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(pSerializedRecipe, "secondaryOutput"));
            }
            JsonArray ingredients = GsonHelper.getAsJsonArray(pSerializedRecipe, "ingredients");
            NonNullList<Ingredient> inputs = NonNullList.withSize(ingredients.size(), Ingredient.EMPTY);
            for (int i = 0; i < ingredients.size(); i++) {
                inputs.set(i, Ingredient.fromJson(ingredients.get(i)));
            }
            Ingredient containerIngredient = Ingredient.EMPTY;
            if (pSerializedRecipe.has("container")) {
                containerIngredient = Ingredient.fromJson(pSerializedRecipe.get("container"));
            }
            int processingTime = GsonHelper.getAsInt(pSerializedRecipe, "processingTime", defaultProcessingTime);
            return new FermentingRecipe(inputs, containerIngredient, output, secondaryOutput, processingTime, pRecipeId);
        }

        @Override
        public void toNetwork(FriendlyByteBuf pBuffer, FermentingRecipe pRecipe) {
            pBuffer.writeInt(pRecipe.inputItems.size());
            for (Ingredient ingredient : pRecipe.getIngredients()) {
                ingredient.toNetwork(pBuffer);
            }
            pBuffer.writeItemStack(pRecipe.getResultItem(null), false);
            boolean hasSecondaryOutput = !pRecipe.getSecondaryResultItem().isEmpty();
            pBuffer.writeBoolean(hasSecondaryOutput);
            if (hasSecondaryOutput) {
                pBuffer.writeItemStack(pRecipe.getSecondaryResultItem(), false);
            }
            pBuffer.writeInt(pRecipe.getProcessingTime());
            pRecipe.containerIngredient.toNetwork(pBuffer);
        }

        @Override
        public FermentingRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
            NonNullList<Ingredient> inputs = NonNullList.withSize(pBuffer.readInt(), Ingredient.EMPTY);
            for (int i = 0; i < inputs.size(); i++) {
                inputs.set(i, Ingredient.fromNetwork(pBuffer));
            }
            ItemStack output = pBuffer.readItem();
            ItemStack secondaryOutput = ItemStack.EMPTY;
            boolean hasSecondaryOutput = pBuffer.readBoolean();
            if (hasSecondaryOutput) {
                secondaryOutput = pBuffer.readItem();
            }
            int processingTime = pBuffer.readInt();
            Ingredient containerIngredient = Ingredient.fromNetwork(pBuffer);
            return new FermentingRecipe(inputs, containerIngredient, output, secondaryOutput, processingTime, pRecipeId);
        }
    }
}
