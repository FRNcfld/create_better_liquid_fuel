package com.frnc.create_better_liquid_fuel.compat.jei;

import com.frnc.create_better_liquid_fuel.CreateBetterLiquidFuel;
import com.frnc.create_better_liquid_fuel.liquidburner.LiquidBurning;
import com.simibubi.create.AllBlocks;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.gui.widgets.ITextWidget;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * JEI 分类:{@code create_better_liquid_fuel:liquidburning} 配方 —— 液体燃料在烈焰人燃烧器里烧多久。
 *
 * <p>左槽显示配方流体(一整罐 1000 mB),右半部分文字:燃烧强度(是否超级燃烧)与燃烧时间;
 * 超级燃烧的配方再拆出喷火时间与余烬时间两行。
 *
 * <p>API 取舍: 只使用 JEI 15.20 就已存在的接口,且不使用 {@code IRecipeCategory#draw}
 * (自 15.20 起标记 forRemoval)。这样 mods.toml 里 jei 的 {@code versionRange="*"} 才名副其实
 * ——玩家装任意 JEI 15.x 都不会因为 NoSuchMethodError 崩在分类注册上。
 */
public class LiquidBurningCategory implements IRecipeCategory<LiquidBurning>
{
	/** 与 {@link com.frnc.create_better_liquid_fuel.liquidburner.RecipeRegistry} 注册的配方类型同 ID。 */
	public static final RecipeType<LiquidBurning> TYPE =
			new RecipeType<>(CreateBetterLiquidFuel.id("liquidburning"), LiquidBurning.class);

	private static final int WIDTH = 148;
	/** 高度按最多的四行(强度/总时长/喷火/余烬)留,分类底板只能有一个尺寸 */
	private static final int HEIGHT = 57;
	private static final int SLOT_X = 5;
	private static final int SLOT_Y = 20;
	private static final int TEXT_X = 30;
	private static final int TEXT_Y = 5;
	/** 行距(叠加在字体行高之上):9 + 4 = 13 像素一行,四行落在 y=5/18/31/44 */
	private static final int LINE_SPACING = 4;
	/** 深灰:与 JEI 浅色配方底板对比足够,和原版 GUI 文字观感一致 */
	private static final int TEXT_COLOR = 0xFF404040;

	/** 制表符图标用"有烈焰人的燃烧器": 空燃烧器没有方块实体,液体燃料对它不生效 */
	private final IDrawable background;
	private final IDrawable icon;

	public LiquidBurningCategory(IGuiHelper guiHelper)
	{
		this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
		this.icon = guiHelper.createDrawableItemLike(AllBlocks.BLAZE_BURNER);
	}

	@Override
	public RecipeType<LiquidBurning> getRecipeType()
	{
		return TYPE;
	}

	@Override
	public Component getTitle()
	{
		return Component.translatable("create_better_liquid_fuel.jei.liquid_burning");
	}

	@Override
	public IDrawable getBackground()
	{
		return background;
	}

	@Override
	public IDrawable getIcon()
	{
		return icon;
	}

	/** 输入:一整罐(1000 mB)流体,槽内按容量显示流体量 */
	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, LiquidBurning recipe, IFocusGroup focuses)
	{
		builder.addInputSlot(SLOT_X, SLOT_Y)
				.setStandardSlotBackground()
				.setFluidRenderer(recipe.getFluid().getAmount(), true, 16, 16)
				.addIngredient(ForgeTypes.FLUID_STACK, recipe.getFluid())
				.setSlotName("fluid");
	}

	/**
	 * 右半部分的文字信息。
	 *
	 * <p>燃烧时间按"这罐液体总共能烧多久"给出,所以超级燃烧配方是喷火 + 余烬之和,
	 * 再单独列出喷火与余烬各占多少 —— 与 mixin 里的实际行为对齐:
	 * 喷火阶段用 {@code superheattime},喷火结束后 Create 维持"烈焰"状态的常量被
	 * {@code liquidFuel$addBurntime} 换成 {@code burntime}。
	 *
	 * <p>注意 {@code addText(text, a, b)} 的两个 int 是<b>可用区域宽高</b>(文字默认落在 0,0,
	 * 超宽会被自动换行/截断),真正的位置要另外 {@code setPosition} —— 见 JEI 自带
	 * {@code AnvilRecipeCategory} 的用法。
	 */
	@Override
	public void createRecipeExtras(IRecipeExtrasBuilder builder, LiquidBurning recipe, IFocusGroup focuses)
	{
		boolean superheat = recipe.getSuperheattime() > 0;
		int superheattime = recipe.getSuperheattime();
		int burntime = recipe.getBurntime();

		List<FormattedText> lines = new ArrayList<>(4);
		lines.add(Component.translatable("create_better_liquid_fuel.jei.intensity",
				Component.translatable(superheat
						? "create_better_liquid_fuel.jei.intensity.superheat"
						: "create_better_liquid_fuel.jei.intensity.normal")));
		lines.add(Component.translatable("create_better_liquid_fuel.jei.burn_time",
				seconds(superheat ? superheattime + burntime : burntime)));
		if (superheat)
		{
			lines.add(Component.translatable("create_better_liquid_fuel.jei.superheat_time",
					seconds(superheattime)));
			lines.add(Component.translatable("create_better_liquid_fuel.jei.ember_time",
					seconds(burntime)));
		}

		ITextWidget text = builder.addText(lines, WIDTH - TEXT_X, HEIGHT)
				.setPosition(TEXT_X, TEXT_Y)
				.setColor(TEXT_COLOR)
				.setShadow(true);
		text.setLineSpacing(LINE_SPACING);
	}

	/** 配方里的时间是 tick(20 tick = 1 秒),展示成秒;不能整除时保留一位小数 */
	private static String seconds(int ticks)
	{
		if (ticks % 20 == 0)
		{
			return Integer.toString(ticks / 20);
		}
		return String.format(Locale.ROOT, "%.1f", ticks / 20.0);
	}
}
