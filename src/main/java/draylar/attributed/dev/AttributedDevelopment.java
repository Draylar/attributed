package draylar.attributed.dev;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import draylar.attributed.Attributed;
import draylar.attributed.CustomEntityAttributes;
import draylar.attributed.event.CriticalHitEvents;
import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.registry.Registry;

import java.util.UUID;

public class AttributedDevelopment implements ModInitializer {

    private static final UUID TEST_UUID = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CA"); // note: last char differs from the item class UUID to prevent tooltip things

    @Override
    public void onInitialize() {
        // Register development testing items
        Registry.register(Registry.ITEM, Attributed.id("critical_chance"), new CriticalChanceItem(new Item.Settings()));
        Registry.register(Registry.ITEM, Attributed.id("critical_damage"), new CriticalDamageItem(new Item.Settings()));

        // Give sticks a bonus Critical Chance value...
        CriticalHitEvents.BEFORE.register((player, target, stack, chance) -> {
            if(stack.getItem().equals(Items.STICK)) {
                return TypedActionResult.success(1.0);
            }

            return TypedActionResult.pass(chance);
        });

        // Diamonds land Critical Hits with a lot of power!
        CriticalHitEvents.CALCULATE_MODIFIER.register((player, target, stack, chance) -> {
            if(stack.getItem().equals(Items.DIAMOND)) {
                return 10.0;
            }

            return chance;
        });

        // If the player lands a critical hit with an Emerald, play a sound...
        CriticalHitEvents.AFTER.register((player, target, stack) -> {
            if(stack.getItem().equals(Items.EMERALD)) {
                player.world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1.0f, 1.0f);
            }
        });
    }

    public static class CriticalChanceItem extends Item {

        public CriticalChanceItem(Settings settings) {
            super(settings);
        }

        @Override
        public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot slot) {
            if(slot.equals(EquipmentSlot.MAINHAND)) {
                ImmutableMultimap.Builder<EntityAttribute, EntityAttributeModifier> builder = ImmutableMultimap.builder();
                builder.put(CustomEntityAttributes.CRIT_CHANCE, new EntityAttributeModifier(TEST_UUID, "Critical Chance Modifier", 0.5, EntityAttributeModifier.Operation.ADDITION));
                return builder.build();
            }
            return super.getAttributeModifiers(slot);
        }
    }

    public static class CriticalDamageItem extends Item {

        public CriticalDamageItem(Settings settings) {
            super(settings);
        }

        @Override
        public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot slot) {
            if(slot.equals(EquipmentSlot.MAINHAND)) {
                ImmutableMultimap.Builder<EntityAttribute, EntityAttributeModifier> builder = ImmutableMultimap.builder();
                builder.put(CustomEntityAttributes.CRIT_DAMAGE, new EntityAttributeModifier(TEST_UUID, "Critical Damage Modifier", 2.0, EntityAttributeModifier.Operation.ADDITION));
                return builder.build();
            }
            return super.getAttributeModifiers(slot);
        }
    }
}
