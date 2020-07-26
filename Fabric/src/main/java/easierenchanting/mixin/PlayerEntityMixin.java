package easierenchanting.mixin;

import easierenchanting.EasierEnchanting;
import net.minecraft.entity.player.PlayerEntity;
import org.apache.logging.log4j.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {

    @Inject(method = "getEnchantmentTableSeed", at = @At("TAIL"), cancellable = true)
    public void patchSeedSpace(CallbackInfoReturnable<Integer> ci){
        ci.setReturnValue((int)((short)ci.getReturnValueI()));
    }

}
