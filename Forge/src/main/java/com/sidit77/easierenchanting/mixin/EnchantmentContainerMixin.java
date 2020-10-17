package com.sidit77.easierenchanting.mixin;

import com.sidit77.easierenchanting.EasierEnchanting;
import com.sidit77.easierenchanting.IEnchantmentContainerExtension;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.EnchantmentContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.state.Property;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.IntReferenceHolder;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentContainer.class)
public abstract class EnchantmentContainerMixin extends Container implements IEnchantmentContainerExtension {

    protected EnchantmentContainerMixin(ContainerType<?> type, int id) {
        super(type, id);
    }

    @Final
    private IntReferenceHolder easierenchant_cost;

    @Shadow
    @Final
    private IInventory tableInventory;

    @Shadow
    @Final
    private IWorldPosCallable field_217006_g;

    @Shadow
    @Final
    private IntReferenceHolder xpSeed;

    @Shadow
    @Final
    private int[] enchantLevels;

    @Inject(method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/util/IWorldPosCallable;)V", at = @At(value = "RETURN"))
    public void init(CallbackInfo ci){
        easierenchant_cost = IntReferenceHolder.single();
        this.trackInt(easierenchant_cost).set(EasierEnchanting.lapiscost);
    }

    @Inject(method = "enchantItem", at = @At(value = "HEAD"), cancellable = true)
    public void buttonClick(PlayerEntity player, int id, CallbackInfoReturnable<Boolean> ci){
        if(id == 3){
            ItemStack itemStack2 = this.tableInventory.getStackInSlot(1);
            if((itemStack2.getCount() < getLapisCost() && !player.abilities.isCreativeMode)
                    || this.enchantLevels[0] <= 0){
                ci.setReturnValue(false);
                return;
            }
            this.field_217006_g.consume((world, blockPos) -> {
                if (!player.abilities.isCreativeMode) {
                    itemStack2.shrink(getLapisCost());
                    if (itemStack2.isEmpty()) {
                        this.tableInventory.setInventorySlotContents(1, ItemStack.EMPTY);
                    }
                }

                player.onEnchant(null, 0);
                this.tableInventory.markDirty();
                this.xpSeed.set(player.getXPSeed());
                this.onCraftMatrixChanged(this.tableInventory);
                world.playSound((PlayerEntity)null, blockPos, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 1.0F, world.rand.nextFloat() * 0.1F + 0.9F);
            });
            ci.setReturnValue(true);
        }
    }

    @Override
    public int getLapisCost() {
        return easierenchant_cost.get();
    }
}
