package com.frnc.create_better_liquid_fuel;

import com.frnc.create_better_liquid_fuel.liquidburner.RecipeRegistry;
import com.mojang.logging.LogUtils;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

/**
 * Create: Better Liquid Fuel 主入口。
 *
 * <p>本 mod 是 Create（机械动力）6.0.x 的附属：让烈焰人燃烧器支持液体燃料。
 * 燃烧器带 1000 mB 储罐（暴露 FLUID_HANDLER 能力），满罐后整罐消耗作为一次燃料；
 * 优先匹配 {@code create_better_liquid_fuel:liquidburning} 配方，未命中则按
 * {@code create_better_liquid_fuel:blaze_burner_fuel_special/regular} 标签回退。
 * 这里只负责注册表、事件总线和通用初始化，具体功能在各自的功能包里实现。
 */
@Mod(CreateBetterLiquidFuel.MOD_ID)
public class CreateBetterLiquidFuel {

    /** 本 mod 的命名空间，需与 META-INF/mods.toml 中的 modId 一致。 */
    public static final String MOD_ID = "create_better_liquid_fuel";

    private static final Logger LOGGER = LogUtils.getLogger();

    /** 快捷创建本 mod 的 ResourceLocation。 */
    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    public CreateBetterLiquidFuel(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        modEventBus.addListener(this::commonSetup);

        // liquidburning 配方类型与序列化器（烈焰人燃烧器烧液体）
        RecipeRegistry.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);

        LOGGER.info("[CreateBetterLiquidFuel] initialized");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("[CreateBetterLiquidFuel] common setup done");
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("[CreateBetterLiquidFuel] server starting");
    }
}
