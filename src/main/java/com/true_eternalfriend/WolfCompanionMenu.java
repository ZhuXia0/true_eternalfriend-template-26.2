package com.true_eternalfriend;

import java.util.UUID;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.DyeColor;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class WolfCompanionMenu extends AbstractContainerMenu {
    public record OpenData(int entityId, UUID uuid, Component name, int collar, ItemStack armor, float health) {
        public static final StreamCodec<RegistryFriendlyByteBuf, OpenData> CODEC = StreamCodec.of(
            (buf, data) -> {
                buf.writeVarInt(data.entityId); buf.writeUUID(data.uuid);
                ComponentSerialization.STREAM_CODEC.encode(buf, data.name);
                buf.writeVarInt(data.collar);
                ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, data.armor);
                buf.writeFloat(data.health);
            },
            buf -> new OpenData(buf.readVarInt(), buf.readUUID(), ComponentSerialization.STREAM_CODEC.decode(buf),
                buf.readVarInt(), ItemStack.OPTIONAL_STREAM_CODEC.decode(buf), buf.readFloat()));
    }
    public static final MenuType<WolfCompanionMenu> TYPE = Registry.register(BuiltInRegistries.MENU,
        Identifier.fromNamespaceAndPath(TrueEternalFriend.MOD_ID, "wolf_companion"),
        new ExtendedMenuType<>(WolfCompanionMenu::new, OpenData.CODEC));

    private final Wolf wolf;
    private final OpenData openData;
    private final Container gear;
    private final ContainerData data;

    public static ExtendedMenuProvider<OpenData> provider(Wolf wolf) {
        return new ExtendedMenuProvider<>() {
            @Override public OpenData getScreenOpeningData(ServerPlayer player) { return openingData(wolf); }
            @Override public Component getDisplayName() {
                return wolf.hasCustomName() ? wolf.getCustomName() : Component.translatable("screen.true_eternal_friend.tamed_wolf");
            }
            @Override public AbstractContainerMenu createMenu(int syncId, Inventory inventory, Player player) {
                return new WolfCompanionMenu(syncId, inventory, wolf);
            }
        };
    }
    public WolfCompanionMenu(int syncId, Inventory inventory, OpenData payload) {
        this(syncId, inventory, inventory.player.level().getEntity(payload.entityId()), payload);
    }
    private WolfCompanionMenu(int syncId, Inventory inventory, net.minecraft.world.entity.Entity entity, OpenData payload) {
        this(syncId, inventory, resolve(inventory, entity, payload), payload);
    }
    public WolfCompanionMenu(int syncId, Inventory inventory, Wolf wolf) {
        this(syncId, inventory, wolf, openingData(wolf));
    }
    private static OpenData openingData(Wolf wolf) {
        return new OpenData(wolf.getId(), wolf.getUUID(), wolf.hasCustomName() ? wolf.getCustomName()
            : Component.translatable("screen.true_eternal_friend.tamed_wolf"), wolf.getCollarColor().getId(),
            wolf.getItemBySlot(EquipmentSlot.BODY).copy(), wolf.getHealth());
    }
    private static Wolf resolve(Inventory inventory, net.minecraft.world.entity.Entity entity, OpenData payload) {
        if (entity instanceof Wolf w && w.getUUID().equals(payload.uuid())) return w;
        if (!inventory.player.level().isClientSide()) return null;
        Wolf mirror = EntityTypes.WOLF.create(inventory.player.level(), EntitySpawnReason.EVENT);
        if (mirror != null) {
            mirror.setTame(true, true);
            mirror.setCustomName(payload.name());
            mirror.setComponent(DataComponents.WOLF_COLLAR, DyeColor.byId(payload.collar()));
            mirror.setItemSlot(EquipmentSlot.BODY, payload.armor().copy());
            mirror.setHealth(payload.health());
        }
        return mirror;
    }
    private WolfCompanionMenu(int syncId, Inventory inventory, Wolf wolf, OpenData payload) {
        super(TYPE, syncId);
        this.wolf = wolf;
        this.openData = payload;
        this.gear = wolf == null ? new SimpleContainer(3) : new WolfGearContainer(wolf);
        // The client needs writable storage for menu data packets from the server.
        this.data = wolf == null || inventory.player.level().isClientSide() ? new SimpleContainerData(10) : new ContainerData() {
            @Override public int get(int index) {
                CompanionWolf state = (CompanionWolf) wolf;
                return switch (index) {
                    case 0 -> Math.round(wolf.getHealth() * 10);
                    case 1 -> Math.round(wolf.getMaxHealth() * 10);
                    case 2 -> state.trueEternalFriend$eatCooldown();
                    case 3 -> CompanionLogic.nextHeal(gear.getItem(1));
                    case 4 -> gear.getItem(0).getMaxDamage() - gear.getItem(0).getDamageValue();
                    case 5 -> gear.getItem(0).getMaxDamage();
                    case 6 -> ((EternalWolf) wolf).trueEternalFriend$isEternal() ? 10 : 0;
                    case 7 -> CompanionItems.clawDamage(gear.getItem(2).getItem());
                    case 8 -> Math.toIntExact(Math.round(wolf.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR) * 10));
                    case 9 -> Math.toIntExact(Math.round(wolf.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE) * 10));
                    default -> 0;
                };
            }
            @Override public void set(int index, int value) { }
            @Override public int getCount() { return 10; }
        };
        addSlot(new Slot(gear, 0, 35, 31) {
            @Override public boolean mayPlace(ItemStack stack) { return stack.is(Items.WOLF_ARMOR) || CompanionItems.isArmor(stack.getItem()); }
            @Override public int getMaxStackSize() { return 1; }
        });
        addSlot(new Slot(gear, 1, 35, 55) {
            @Override public boolean mayPlace(ItemStack stack) { return stack.is(CompanionLogic.DOG_FOOD); }
        });
        addSlot(new Slot(gear, 2, 35, 79) {
            @Override public boolean mayPlace(ItemStack stack) { return CompanionItems.isClaw(stack.getItem()); }
            @Override public int getMaxStackSize() { return 1; }
        });
        addStandardInventorySlots(inventory, 8, 119);
        addDataSlots(data);
    }
    public OpenData openData() { return openData; }
    public Wolf wolf() { return wolf; }
    public int value(int index) { return data.get(index); }
    @Override public boolean stillValid(Player player) {
        return wolf != null && wolf.isAlive() && wolf.level() == player.level() && wolf.isOwnedBy(player)
            && player.distanceToSqr(wolf) < 64 && wolf.getUUID().equals(openData.uuid());
    }
    @Override public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index < 3) {
            if (!moveItemStackTo(stack, 3, slots.size(), true)) return ItemStack.EMPTY;
        } else {
            int target = (stack.is(Items.WOLF_ARMOR) || CompanionItems.isArmor(stack.getItem())) ? 0
                : stack.is(CompanionLogic.DOG_FOOD) ? 1 : CompanionItems.isClaw(stack.getItem()) ? 2 : -1;
            if (target < 0 || !moveItemStackTo(stack, target, target + 1, false)) return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        return original;
    }

    private static final class WolfGearContainer implements Container {
        private final Wolf wolf;
        WolfGearContainer(Wolf wolf) { this.wolf = wolf; }
        private Container custom() { return ((CompanionWolf) wolf).trueEternalFriend$gear(); }
        @Override public int getContainerSize() { return 3; }
        @Override public boolean isEmpty() { return getItem(0).isEmpty() && getItem(1).isEmpty() && getItem(2).isEmpty(); }
        @Override public ItemStack getItem(int slot) { return slot == 0 ? wolf.getItemBySlot(EquipmentSlot.BODY) : custom().getItem(slot - 1); }
        @Override public ItemStack removeItem(int slot, int count) {
            ItemStack result = getItem(slot).split(count);
            setChanged();
            return result;
        }
        @Override public ItemStack removeItemNoUpdate(int slot) {
            ItemStack result = getItem(slot);
            setItem(slot, ItemStack.EMPTY);
            return result;
        }
        @Override public void setItem(int slot, ItemStack stack) {
            if (slot == 0) wolf.setItemSlot(EquipmentSlot.BODY, stack);
            else custom().setItem(slot - 1, stack);
            if (slot == 0 && !stack.isEmpty()) TrueEternalFriend.awardOwner(wolf, ModAdvancements.FIRST_ARMOR);
            setChanged();
        }
        @Override public void setChanged() { custom().setChanged(); }
        @Override public boolean stillValid(Player player) { return wolf.isAlive() && wolf.isOwnedBy(player) && player.distanceToSqr(wolf) < 64; }
        @Override public void clearContent() { for (int i = 0; i < 3; i++) setItem(i, ItemStack.EMPTY); }
    }
}
