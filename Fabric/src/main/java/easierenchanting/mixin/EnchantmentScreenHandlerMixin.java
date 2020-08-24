package easierenchanting.mixin;

import easierenchanting.EasierEnchanting;
import easierenchanting.IEnchantmentScreenHandlerExtension;
import net.fabricmc.loader.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.*;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import org.apache.logging.log4j.Level;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentScreenHandler.class)
public abstract class EnchantmentScreenHandlerMixin extends ScreenHandler implements IEnchantmentScreenHandlerExtension {
    protected EnchantmentScreenHandlerMixin(ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }

    @Final
    private Property easierenchant_cost;

    @Dynamic
    int hardmodeCost = 0;

    @Shadow
    @Final
    private Inventory inventory;

    @Shadow
    @Final
    private ScreenHandlerContext context;

    @Shadow
    @Final
    private Property seed;

    @Shadow
    @Final
    private int[] enchantmentPower;

    @Inject(method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/screen/ScreenHandlerContext;)V", at = @At(value = "RETURN"))
    public void init(CallbackInfo ci){
        easierenchant_cost = Property.create();
        this.addProperty(easierenchant_cost).set(EasierEnchanting.lapiscost);
    }

    @Inject(method = "onButtonClick", at = @At(value = "HEAD"), cancellable = true)
    public void buttonClick(PlayerEntity player, int id, CallbackInfoReturnable<Boolean> ci){
        if(id == 3){
            ItemStack itemStack2 = this.inventory.getStack(1);
            if((itemStack2.getCount() < getLapisCost() && !player.abilities.creativeMode)
               || this.enchantmentPower[0] <= 0){
                ci.setReturnValue(false);
                return;
            }
            this.context.run((world, blockPos) -> {
                if (!player.abilities.creativeMode) {
                    itemStack2.decrement(getLapisCost());
                    if (itemStack2.isEmpty()) {
                        this.inventory.setStack(1, ItemStack.EMPTY);
                    }
                }

                player.applyEnchantmentCosts(null, 0);
                this.inventory.markDirty();
                this.seed.set(player.getEnchantmentTableSeed());
                this.onContentChanged(this.inventory);
                world.playSound((PlayerEntity)null, blockPos, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 1.0F, world.random.nextFloat() * 0.1F + 0.9F);
            });
            ci.setReturnValue(true);
        }else{
            hardmodeCost = this.enchantmentPower[id]-(id+1);
        }
    }
    @Inject(method = "onButtonClick", at = @At(value = "RETURN"), cancellable = true)
    public void hardmodeLevels(PlayerEntity player, int id, CallbackInfoReturnable ci){
        if(EasierEnchanting.hardmode && ci.getReturnValueZ()){
            player.applyEnchantmentCosts(null, hardmodeCost);
        }
    }

    @Override
    public int getLapisCost() {
        return easierenchant_cost.get();
    }

}
