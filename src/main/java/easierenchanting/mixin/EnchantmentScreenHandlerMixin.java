package easierenchanting.mixin;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.screen.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.Random;

@Mixin(EnchantmentScreenHandler.class)
public abstract class EnchantmentScreenHandlerMixin  extends ScreenHandler {
    protected EnchantmentScreenHandlerMixin(ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }

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
    private Random random;

    @Shadow
    @Final
    private int[] enchantmentPower;

    @Shadow
    @Final
    private int[] enchantmentId;

    @Shadow
    @Final
    private int[] enchantmentLevel;

    @Overwrite
    public boolean onButtonClick(PlayerEntity player, int id) {
        ItemStack itemStack = this.inventory.getStack(0);
        ItemStack itemStack2 = this.inventory.getStack(1);
        if(id == 3){
            if(itemStack2.getCount() < 6 && !player.abilities.creativeMode)
                return false;
            if(this.enchantmentPower[0] <= 0)
                return false;
            this.context.run((world, blockPos) -> {
                if (!player.abilities.creativeMode) {
                    itemStack2.decrement(6);
                    if (itemStack2.isEmpty()) {
                        this.inventory.setStack(1, ItemStack.EMPTY);
                    }
                }

                player.applyEnchantmentCosts(itemStack, 0);
                this.inventory.markDirty();
                this.seed.set(player.getEnchantmentTableSeed());
                this.onContentChanged(this.inventory);
                world.playSound((PlayerEntity)null, blockPos, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 1.0F, world.random.nextFloat() * 0.1F + 0.9F);
            });
            return true;
        }
        int i = id + 1;
        if ((itemStack2.isEmpty() || itemStack2.getCount() < i) && !player.abilities.creativeMode) {
            return false;
        } else if (this.enchantmentPower[id] <= 0 || itemStack.isEmpty() || (player.experienceLevel < i || player.experienceLevel < this.enchantmentPower[id]) && !player.abilities.creativeMode) {
            return false;
        } else {
            this.context.run((world, blockPos) -> {
                ItemStack itemStack3 = itemStack;
                List<EnchantmentLevelEntry> list = this.generateEnchantments(itemStack, id, this.enchantmentPower[id]);
                if (!list.isEmpty()) {
                    player.applyEnchantmentCosts(itemStack, i);
                    boolean bl = itemStack.getItem() == Items.BOOK;
                    if (bl) {
                        itemStack3 = new ItemStack(Items.ENCHANTED_BOOK);
                        CompoundTag compoundTag = itemStack.getTag();
                        if (compoundTag != null) {
                            itemStack3.setTag(compoundTag.copy());
                        }

                        this.inventory.setStack(0, itemStack3);
                    }

                    for(int k = 0; k < list.size(); ++k) {
                        EnchantmentLevelEntry enchantmentLevelEntry = (EnchantmentLevelEntry)list.get(k);
                        if (bl) {
                            EnchantedBookItem.addEnchantment(itemStack3, enchantmentLevelEntry);
                        } else {
                            itemStack3.addEnchantment(enchantmentLevelEntry.enchantment, enchantmentLevelEntry.level);
                        }
                    }

                    if (!player.abilities.creativeMode) {
                        itemStack2.decrement(i);
                        if (itemStack2.isEmpty()) {
                            this.inventory.setStack(1, ItemStack.EMPTY);
                        }
                    }

                    player.incrementStat(Stats.ENCHANT_ITEM);
                    if (player instanceof ServerPlayerEntity) {
                        Criteria.ENCHANTED_ITEM.trigger((ServerPlayerEntity)player, itemStack3, i);
                    }

                    this.inventory.markDirty();
                    this.seed.set(player.getEnchantmentTableSeed());
                    this.onContentChanged(this.inventory);
                    world.playSound((PlayerEntity)null, blockPos, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 1.0F, world.random.nextFloat() * 0.1F + 0.9F);
                }

            });
            return true;
        }
    }

    private List<EnchantmentLevelEntry> generateEnchantments(ItemStack stack, int slot, int level) {
        this.random.setSeed((long)(this.seed.get() + slot));
        List<EnchantmentLevelEntry> list = EnchantmentHelper.generateEnchantments(this.random, stack, level, false);
        if (stack.getItem() == Items.BOOK && list.size() > 1) {
            list.remove(this.random.nextInt(list.size()));
        }

        return list;
    }
}
