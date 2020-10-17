package com.sidit77.easierenchanting.mixin;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.sidit77.easierenchanting.IEnchantmentContainerExtension;
import net.minecraft.client.gui.screen.EnchantmentScreen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.EnchantmentContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
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
public abstract class EnchantmentScreenMixin extends ContainerScreen<EnchantmentContainer> {

    public EnchantmentScreenMixin(EnchantmentContainer screenContainer, PlayerInventory inv, ITextComponent titleIn) {
        super(screenContainer, inv, titleIn);
    }

    @Shadow
    @Final
    private Random random;

    @Shadow
    private ItemStack last;

    @Inject(method = "func_231044_a_", at = @At(value = "TAIL"), cancellable = true)
    public void mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> ci){
        int i = (this.field_230708_k_ - this.xSize) / 2;
        int j = (this.field_230709_l_ - this.ySize) / 2;

        double d = mouseX - (double)(i + 13);
        double e = mouseY - (double)(j + 18);
        if (d >= 0.0D && e >= 0.0D && d < 37.0D && e < 21.0D && this.container.enchantItem(this.field_230706_i_.player, 3)) {
            this.field_230706_i_.playerController.sendEnchantPacket((this.container).windowId, 3);
            ci.setReturnValue(true);
        }
    }

    @Inject(method = "func_230430_a_", at = @At(value = "TAIL"))
    public void bookButton(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci){
        boolean bookopen = false;
        for(int i2 = 0; i2 < 3; ++i2) {
            if (this.container.enchantLevels[i2] != 0) {
                bookopen = true;
                break;
            }
        }
        if (bookopen && this.isPointInRegion(13, 18, 37, 21, (double)mouseX, (double)mouseY)) {
            int cost = ((IEnchantmentContainerExtension)this.container).getLapisCost();
            List<ITextComponent> list = Lists.newArrayList();
            list.add(new TranslationTextComponent("container.enchant.reroll"));
            TranslationTextComponent lapiscost = new TranslationTextComponent("container.enchant.lapis.many", cost);
            list.add(new StringTextComponent(""));
            list.add(lapiscost.func_240699_a_(this.container.getLapisAmount() >= cost ? TextFormatting.GRAY : TextFormatting.RED));
            this.func_243308_b(matrices, list, mouseX, mouseY);
        }
    }

    @Inject(method = "func_230430_a_", locals = LocalCapture.CAPTURE_FAILHARD, at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target =
            "Lnet/minecraft/client/gui/screen/EnchantmentScreen;func_243308_b(Lcom/mojang/blaze3d/matrix/MatrixStack;Ljava/util/List;II)V"))
    public void fullText(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci, boolean bl, int i, int j, int k, Enchantment enchantment, int l, int m, List<ITextComponent> list){
        list.remove(0);
        list.addAll(0,
                this.generateEnchantments(j, k).stream().map(e -> e.enchantment.getDisplayName(e.enchantmentLevel)).collect(Collectors.toList()));
    }

    private List<EnchantmentData> generateEnchantments(int slot, int level) {
        this.random.setSeed((long)(this.container.func_217005_f() + slot));
        List<EnchantmentData> list = EnchantmentHelper.buildEnchantmentList(this.random, last, level, false);
        if (last.getItem() == Items.BOOK && list.size() > 1) {
            list.remove(this.random.nextInt(list.size()));
        }

        return list;
    }

}
