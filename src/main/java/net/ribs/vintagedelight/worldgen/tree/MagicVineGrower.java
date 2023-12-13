package net.ribs.vintagedelight.worldgen.tree;
import net.minecraft.resources.ResourceLocation;
import net.ribs.vintagedelight.VintageDelight;

public class MagicVineGrower extends ModTreeGrower {
    @Override
    protected ResourceLocation getConfiguredFeatureLocation() {
        return new ResourceLocation(VintageDelight.MODID, "magic_vine_tree");
    }
}





