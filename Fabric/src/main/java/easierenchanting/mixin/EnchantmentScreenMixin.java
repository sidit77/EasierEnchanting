package easierenchanting.mixin;

import com.google.common.collect.Lists;
import easierenchanting.EasierEnchanting;
import easierenchanting.IEnchantmentScreenHandlerExtension;
import net.fabricmc.loader.FabricLoader;
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
import org.apache.logging.log4j.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

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

    @Inject(method = "mouseClicked", at = @At(value = "TAIL"), cancellable = true)
    public void mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> ci){
        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;

        double d = mouseX - (double)(i + 13);
        double e = mouseY - (double)(j + 18);
        if (d >= 0.0D && e >= 0.0D && d < 37.0D && e < 21.0D && this.handler.onButtonClick(this.client.player, 3)) {
            this.client.interactionManager.clickButton(this.handler.syncId, 3);
            ci.setReturnValue(true);
        }
    }

    @Inject(method = "render", at = @At(value = "TAIL"))
    public void bookButton(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci){
        boolean bookopen = false;
        for(int i2 = 0; i2 < 3; ++i2) {
            if (this.handler.enchantmentPower[i2] != 0) {
                bookopen = true;
                break;
            }
        }
        if (bookopen && this.isPointWithinBounds(13, 18, 37, 21, (double)mouseX, (double)mouseY)) {
            int cost = ((IEnchantmentScreenHandlerExtension)this.handler).getLapisCost();
            List<Text> list = Lists.newArrayList();
            list.add(new TranslatableText("container.enchant.reroll"));
            if (!EasierEnchanting.cleantooltips) {
                TranslatableText lapiscost = new TranslatableText("container.enchant.lapis.many", cost);
                list.add(LiteralText.EMPTY);
                list.add(lapiscost.formatted(this.handler.getLapisCount() >= cost ? Formatting.GRAY : Formatting.RED));
            }
            this.renderTooltip(matrices, list, mouseX, mouseY);
        }
    }

    @Inject(method = "render", locals = LocalCapture.CAPTURE_FAILHARD, at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target =
            "Lnet/minecraft/client/gui/screen/ingame/EnchantmentScreen;renderTooltip(Lnet/minecraft/client/util/math/MatrixStack;Ljava/util/List;II)V"))
    public void fullText(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci, boolean bl, int i, int j, int k, Enchantment enchantment, int l, int m, List<Text> list){
        if (EasierEnchanting.hardmode || EasierEnchanting.cleantooltips)
            list.clear();
        else
            list.remove(0);
        list.addAll(0,
                this.generateEnchantments(j, k).stream().map(e -> e.enchantment.getName(e.level)).collect(Collectors.toList()));
    }

    private List<EnchantmentLevelEntry> generateEnchantments(int slot, int level) {
        this.random.setSeed((long)(this.handler.getSeed() + slot));
        List<EnchantmentLevelEntry> list = EnchantmentHelper.generateEnchantments(this.random, stack, level, false);
        if (stack.getItem() == Items.BOOK && list.size() > 1) {
            list.remove(this.random.nextInt(list.size()));
        }
        return list;
    }

}
