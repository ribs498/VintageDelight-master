package net.ribs.vintagedelight.block.entity;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.ribs.vintagedelight.VintageDelight;
import net.ribs.vintagedelight.block.ModBlocks;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, VintageDelight.MODID);



    public static final RegistryObject<BlockEntityType<FermentingJarBlockEntity>> FERMENTING_JAR_BE =
            BLOCK_ENTITIES.register("fermenting_be", () ->
                    BlockEntityType.Builder.of(FermentingJarBlockEntity::new,
                            ModBlocks.FERMENTING_JAR.get()).build(null));


    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
