package com.true_eternalfriend;

import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.Equippable;

public final class CompanionItems {
    private CompanionItems() { }
    /** Forces item class initialization during the mod entrypoint, before registries freeze. */
    public static void bootstrap() { }
    public static final Item DOG_TEASER = register("dog_teaser", p -> new DogTeaserItem(p.stacksTo(1)));
    public static final Item IRON_WOLF_ARMOR = armor("iron_wolf_armor", 5, 500);
    public static final Item GOLDEN_WOLF_ARMOR = armor("golden_wolf_armor", 3, 300);
    public static final Item DIAMOND_WOLF_ARMOR = armor("diamond_wolf_armor", 9, 2000);
    public static final Item BONE_WOLF_CLAW = claw("bone_wolf_claw", 3);
    public static final Item STONE_WOLF_CLAW = claw("stone_wolf_claw", 5);
    public static final Item MAGMA_WOLF_CLAW = claw("magma_wolf_claw", 4);

    private static Item register(String path, Function<Item.Properties, Item> factory) {
        Identifier id = Identifier.fromNamespaceAndPath(TrueEternalFriend.MOD_ID, path);
        return Registry.register(BuiltInRegistries.ITEM, id,
            factory.apply(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
    }
    private static Item armor(String path, int defense, int durability) {
        return register(path, p -> {
            Identifier id = Identifier.fromNamespaceAndPath(TrueEternalFriend.MOD_ID, path);
            ResourceKey<EquipmentAsset> asset = ResourceKey.create(EquipmentAssets.ROOT_ID, id);
            Equippable equip = Equippable.builder(EquipmentSlot.BODY).setAsset(asset)
                .setAllowedEntities(EntityTypes.WOLF).setDamageOnHurt(true)
                .setEquipOnInteract(true).setCanBeSheared(true).build();
            ItemAttributeModifiers attributes = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR, new AttributeModifier(id, defense, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.BODY)
                .build();
            return new CompanionGearItem(p.stacksTo(1).durability(durability).component(DataComponents.EQUIPPABLE, equip).attributes(attributes),
                "item.true_eternal_friend." + path + ".tooltip");
        });
    }
    private static Item claw(String path, int damage) {
        return register(path, p -> {
            Identifier id = Identifier.fromNamespaceAndPath(TrueEternalFriend.MOD_ID, path);
            ItemAttributeModifiers attributes = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(id, damage, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath(TrueEternalFriend.MOD_ID, path + "_speed"), 2,
                    AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND).build();
            return new CompanionGearItem(p.stacksTo(1).attributes(attributes),
                "item.true_eternal_friend." + path + ".tooltip");
        });
    }
    public static boolean isArmor(Item item) {
        return item == IRON_WOLF_ARMOR || item == GOLDEN_WOLF_ARMOR || item == DIAMOND_WOLF_ARMOR;
    }
    public static boolean isClaw(Item item) {
        return item == BONE_WOLF_CLAW || item == STONE_WOLF_CLAW || item == MAGMA_WOLF_CLAW;
    }
    public static int clawDamage(Item item) {
        if (item == BONE_WOLF_CLAW) return 3;
        if (item == STONE_WOLF_CLAW) return 5;
        if (item == MAGMA_WOLF_CLAW) return 4;
        return 0;
    }
}
