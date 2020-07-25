package easierenchanting.mixin;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.screen.ingame.EnchantmentScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.EnchantmentScreenHandler;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.Random;

@Mixin(EnchantmentScreen.class)
public abstract class EnchantmentScreenMixin extends HandledScreen<EnchantmentScreenHandler>  {
    public EnchantmentScreenMixin(EnchantmentScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Shadow
    @Final
    private Random random;

    @Shadow
    private ItemStack stack;

    private List<EnchantmentLevelEntry> generateEnchantments(ItemStack stack, long seed, int level) {
        this.random.setSeed(seed);
        List<EnchantmentLevelEntry> list = EnchantmentHelper.generateEnchantments(this.random, stack, level, false);
        if (stack.getItem() == Items.BOOK && list.size() > 1) {
            list.remove(this.random.nextInt(list.size()));
        }

        return list;
    }

    @Overwrite
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;

        for(int k = 0; k < 3; ++k) {
            double d = mouseX - (double)(i + 60);
            double e = mouseY - (double)(j + 14 + 19 * k);
            if (d >= 0.0D && e >= 0.0D && d < 108.0D && e < 19.0D && ((EnchantmentScreenHandler)this.handler).onButtonClick(this.client.player, k)) {
                this.client.interactionManager.clickButton(((EnchantmentScreenHandler)this.handler).syncId, k);
                return true;
            }
        }

        double d = mouseX - (double)(i + 13);
        double e = mouseY - (double)(j + 18);
        if (d >= 0.0D && e >= 0.0D && d < 37.0D && e < 21.0D && ((EnchantmentScreenHandler)this.handler).onButtonClick(this.client.player, 3)) {
            this.client.interactionManager.clickButton(((EnchantmentScreenHandler)this.handler).syncId, 3);
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Overwrite
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        delta = this.client.getTickDelta();
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(matrices, mouseX, mouseY);
        boolean bl = this.client.player.abilities.creativeMode;
        int i = ((EnchantmentScreenHandler)this.handler).getLapisCount();

        for(int j = 0; j < 3; ++j) {
            int k = ((EnchantmentScreenHandler)this.handler).enchantmentPower[j];
            Enchantment enchantment = Enchantment.byRawId(((EnchantmentScreenHandler)this.handler).enchantmentId[j]);
            int l = ((EnchantmentScreenHandler)this.handler).enchantmentLevel[j];
            int m = j + 1;
            if (this.isPointWithinBounds(60, 14 + 19 * j, 108, 17, (double)mouseX, (double)mouseY) && k > 0 && l >= 0 && enchantment != null) {
                List<Text> list = Lists.newArrayList();
                List<EnchantmentLevelEntry> el = this.generateEnchantments(stack, (long)(((EnchantmentScreenHandler)this.handler).getSeed() + j), k);
                for(EnchantmentLevelEntry eel : el){
                    list.add(eel.enchantment.getName(eel.level));
                }
                if (!bl) {
                    list.add(LiteralText.EMPTY);
                    if (this.client.player.experienceLevel < k) {
                        list.add((new TranslatableText("container.enchant.level.requirement", new Object[]{((EnchantmentScreenHandler)this.handler).enchantmentPower[j]})).formatted(Formatting.RED));
                    } else {
                        TranslatableText mutableText2;
                        if (m == 1) {
                            mutableText2 = new TranslatableText("container.enchant.lapis.one");
                        } else {
                            mutableText2 = new TranslatableText("container.enchant.lapis.many", new Object[]{m});
                        }

                        list.add(mutableText2.formatted(i >= m ? Formatting.GRAY : Formatting.RED));
                        TranslatableText mutableText4;
                        if (m == 1) {
                            mutableText4 = new TranslatableText("container.enchant.level.one");
                        } else {
                            mutableText4 = new TranslatableText("container.enchant.level.many", new Object[]{m});
                        }

                        list.add(mutableText4.formatted(Formatting.GRAY));
                    }
                }

                this.renderTooltip(matrices, list, mouseX, mouseY);
                break;
            }

        }
        boolean bookopen = false;
        for(int i2 = 0; i2 < 3; ++i2) {
            if (((EnchantmentScreenHandler)this.handler).enchantmentPower[i2] != 0) {
                bookopen = true;
            }
        }
        if (bookopen && this.isPointWithinBounds(13, 18, 37, 21, (double)mouseX, (double)mouseY)) {
            List<Text> list = Lists.newArrayList();
            list.add(new TranslatableText("container.enchant.reroll"));
            TranslatableText lapiscost = new TranslatableText("container.enchant.lapis.many", new Object[]{6});
            list.add(lapiscost.formatted(((EnchantmentScreenHandler)this.handler).getLapisCount() >= 6 ? Formatting.GRAY : Formatting.RED));
            this.renderTooltip(matrices, list, mouseX, mouseY);
        }

    }

}
