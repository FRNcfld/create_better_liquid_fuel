package com.frnc.create_better_liquid_fuel.compat.jei;

import com.frnc.create_better_liquid_fuel.CreateBetterLiquidFuel;
import com.frnc.create_better_liquid_fuel.liquidburner.RecipeRegistry;
import com.simibubi.create.AllBlocks;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

/**
 * JEI 联动入口:把 {@code liquidburning} 配方作为独立分类展示。
 *
 * <p>软依赖的安全边界: 本类只由 JEI 自己通过 {@code @JeiPlugin} 加载,未安装 JEI 时
 * 不会被类加载,整合包照常启动(mods.toml 中 jei 为 mandatory=false)。
 * 因此 <b>其它任何类都不应直接 import 本包或 mezz.jei 的类</b>,否则软依赖就破了。
 */
@JeiPlugin
public class CreateBetterLiquidFuelJeiPlugin implements IModPlugin
{
	@Override
	public ResourceLocation getPluginUid()
	{
		return CreateBetterLiquidFuel.id("jei_plugin");
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registration)
	{
		registration.addRecipeCategories(
				new LiquidBurningCategory(registration.getJeiHelpers().getGuiHelper()));
	}

	/** 配方来自客户端已同步的配方管理器,单机/服务器都由原版配方同步提供 */
	@Override
	public void registerRecipes(IRecipeRegistration registration)
	{
		Level level = Minecraft.getInstance().level;
		if (level == null)
		{
			return;
		}
		registration.addRecipes(LiquidBurningCategory.TYPE,
				level.getRecipeManager().getAllRecipesFor(RecipeRegistry.LIQUIDBURNING.get()));
	}

	/**
	 * 催化剂用"有烈焰人的燃烧器"物品({@code create:blaze_burner})。
	 * 空燃烧器({@code create:empty_blaze_burner})没有方块实体、也不接受液体,不能当催化剂。
	 */
	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration)
	{
		registration.addRecipeCatalyst(AllBlocks.BLAZE_BURNER, LiquidBurningCategory.TYPE);
	}
}
