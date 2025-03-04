package com.example.fabricmod.item;

import com.example.fabricmod.data.BaseEntanglementData;
import com.example.fabricmod.effect.EntanglementEffects;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import java.util.List;

public class QuantumEntanglementItem extends Item {
    private static final String MODE_KEY = "mode";
    private static final String TRANSMITTER = "transmitter";
    private static final String RECEIVER = "receiver";

    public QuantumEntanglementItem() {
        super(new FabricItemSettings()
            .maxCount(1)
            .rarity(Rarity.EPIC));
    }

    public QuantumEntanglementItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        
        if (player.isSneaking()) {
            String currentMode = getMode(stack);
            String newMode = TRANSMITTER.equals(currentMode) ? RECEIVER : TRANSMITTER;
            setMode(stack, newMode);
            
            if (!world.isClient) {
                player.sendMessage(Text.translatable("item.fabricmod.quantum_entanglement.mode." + newMode)
                    .formatted(Formatting.GOLD), true);
            }
            
            return TypedActionResult.success(stack);
        }
        
        return TypedActionResult.pass(stack);
    }

    public <T extends BaseEntanglementData> T getEntanglementData(World world) {
        return (T) BaseEntanglementData.get(world);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity player, LivingEntity entity, Hand hand) {
        if (player.getWorld().isClient) return ActionResult.SUCCESS;
        
        // 不能自己绑定自己
        if (entity == player) {
            player.sendMessage(Text.translatable("item.fabricmod.quantum_entanglement.error.self")
                .formatted(Formatting.RED), true);
            return ActionResult.FAIL;
        }

        String mode = getMode(stack);
        BaseEntanglementData data = getEntanglementData(player.getWorld());

        if (data == null) {
            player.sendMessage(Text.translatable("item.fabricmod.quantum_entanglement.error.no_data")
                .formatted(Formatting.RED), true);
            return ActionResult.FAIL;
        }

        if (mode.equals(TRANSMITTER)) {

            // 将点击的实体设置为传递者
            player.sendMessage(Text.translatable("item.fabricmod.quantum_entanglement.select.transmitter", 
                entity.getDisplayName().getString())
                .formatted(Formatting.GREEN), true);

            data.addTransmitter(entity);

            EntanglementEffects.spawnBindingEffect(player.getWorld(), entity, true);
        } else if (mode.equals(RECEIVER)) {
            player.sendMessage(Text.translatable("item.fabricmod.quantum_entanglement.select.receiver",
                entity.getDisplayName().getString())
                .formatted(Formatting.GREEN), true);

            data.addReceiver(entity);

            EntanglementEffects.spawnBindingEffect(player.getWorld(), entity, false);
        }

        return ActionResult.SUCCESS;
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        String mode = getMode(stack);
        tooltip.add(Text.translatable("item.fabricmod.quantum_entanglement.mode." + mode)
            .formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("item.fabricmod.quantum_entanglement.tooltip")
            .formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("item.fabricmod.quantum_entanglement.tooltip.shift")
            .formatted(Formatting.DARK_GRAY));
    }

    private String getMode(ItemStack stack) {
        NbtCompound nbt = stack.getOrCreateNbt();
        return nbt.contains(MODE_KEY) ? nbt.getString(MODE_KEY) : TRANSMITTER;
    }

    private void setMode(ItemStack stack, String mode) {
        NbtCompound nbt = stack.getOrCreateNbt();
        nbt.putString(MODE_KEY, mode);
    }
}