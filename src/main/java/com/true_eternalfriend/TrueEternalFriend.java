package com.true_eternalfriend;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;

public final class TrueEternalFriend implements ModInitializer {
    public static final String MOD_ID = "true_eternal_friend";
    public static final String NBT_KEY = "true_eternal_friend:eternal";
    public static final Identifier ATTACK_ID = Identifier.fromNamespaceAndPath(MOD_ID, "eternal_friend_attack");
    public static final double ATTACK_BONUS = 10.0;
    public static final int REVIVE_TICKS = 12_000;
    public static final Identifier TAG_ID = Identifier.fromNamespaceAndPath(MOD_ID, "eternal_friend_tag");
    public static final Item TAG = Registry.register(BuiltInRegistries.ITEM, TAG_ID,
        new EternalFriendTag(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, TAG_ID)).stacksTo(1).rarity(Rarity.RARE)));
    public static final CreativeModeTab CREATIVE_TAB = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
        Identifier.fromNamespaceAndPath(MOD_ID, "main"), FabricCreativeModeTab.builder()
            .title(Component.translatable("itemGroup.true_eternal_friend.main"))
            .icon(() -> new ItemStack(TAG))
            .displayItems((parameters, output) -> {
                output.accept(TAG);
                output.accept(CompanionItems.DOG_TEASER);
                output.accept(CompanionItems.IRON_WOLF_ARMOR);
                output.accept(CompanionItems.GOLDEN_WOLF_ARMOR);
                output.accept(CompanionItems.DIAMOND_WOLF_ARMOR);
                output.accept(CompanionItems.BONE_WOLF_CLAW);
                output.accept(CompanionItems.STONE_WOLF_CLAW);
                output.accept(CompanionItems.MAGMA_WOLF_CLAW);
            })
            .build());

    @Override public void onInitialize() {
        CompanionItems.bootstrap();
        CompanionConfig.load();
        CompanionCommands.register();
        MenuType<?> ignored = WolfCompanionMenu.TYPE;
        UseEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (world.isClientSide() || player.getItemInHand(hand).isEmpty() ||
                (!player.getItemInHand(hand).is(TAG) && !player.getItemInHand(hand).is(CompanionItems.DOG_TEASER))) return InteractionResult.PASS;
            if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.PASS;
            if (!(entity instanceof Wolf wolf) || !wolf.isTame()) {
                serverPlayer.sendSystemMessage(Component.translatable("message.true_eternal_friend.not_tamed"), true);
                return InteractionResult.SUCCESS;
            }
            if (!wolf.isOwnedBy(serverPlayer)) {
                serverPlayer.sendSystemMessage(Component.translatable("message.true_eternal_friend.not_owner"), true);
                return InteractionResult.SUCCESS;
            }
            if (player.getItemInHand(hand).is(CompanionItems.DOG_TEASER)) {
                serverPlayer.openMenu(WolfCompanionMenu.provider(wolf));
                ModAdvancements.grant(serverPlayer, ModAdvancements.COMPANION_SCREEN);
                return InteractionResult.SUCCESS;
            }
            if (((EternalWolf) wolf).trueEternalFriend$isEternal()) {
                serverPlayer.sendSystemMessage(Component.translatable("message.true_eternal_friend.already"), true);
                return InteractionResult.SUCCESS;
            }
            ((EternalWolf) wolf).trueEternalFriend$setEternal(true);
            applyAttack(wolf);
            if (!serverPlayer.getAbilities().instabuild) player.getItemInHand(hand).shrink(1);
            ServerLevel level = (ServerLevel) world;
            level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, wolf.getX(), wolf.getY() + 0.6, wolf.getZ(), 24, 0.5, 0.5, 0.5, 0.1);
            level.playSound(null, wolf.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.NEUTRAL, 1, 1);
            serverPlayer.sendSystemMessage(Component.translatable("message.true_eternal_friend.bound"), true);
            ModAdvancements.grant(serverPlayer, ModAdvancements.FIRST_FRIEND);
            return InteractionResult.SUCCESS;
        });
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, amount) -> {
            if (!(entity instanceof Wolf wolf) || !(entity.level() instanceof ServerLevel level) ||
                !wolf.isTame() || !((EternalWolf) wolf).trueEternalFriend$isEternal()) return true;
            ReviveState.get(level.getServer()).enqueue(wolf);
            wolf.discard(); // ALLOW_DEATH runs before vanilla loot and experience handling.
            return false;
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerLevel level : server.getAllLevels()) {
                for (net.minecraft.world.entity.Entity entity : level.getAllEntities()) {
                    if (entity instanceof Wolf wolf && wolf.isAlive()) CompanionLogic.tick(wolf);
                }
            }
            ReviveState.get(server).tick(server);
        });
        ServerLivingEntityEvents.AFTER_DAMAGE.register((target, source, baseDamage, damageTaken, blocked) -> {
            if (blocked || damageTaken <= 0 || !(source.getEntity() instanceof LivingEntity attacker)) return;
            Item claw = attacker instanceof Wolf wolf ? ((CompanionWolf) wolf).trueEternalFriend$gear().getItem(1).getItem()
                : attacker instanceof Player ? attacker.getMainHandItem().getItem() : null;
            if (attacker instanceof ServerPlayer player && CompanionItems.isClaw(claw)) {
                ModAdvancements.grant(player, ModAdvancements.FIRST_CLAW_HIT);
            } else if (attacker instanceof Wolf wolf && CompanionItems.isClaw(claw)) {
                awardOwner(wolf, ModAdvancements.FIRST_CLAW_HIT);
            }
            if (claw == CompanionItems.BONE_WOLF_CLAW) {
                target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 0), attacker);
            } else if (claw == CompanionItems.MAGMA_WOLF_CLAW && !target.fireImmune() && !target.isInWaterOrRain()) {
                target.igniteForSeconds(4);
            }
        });
    }

    static void awardOwner(Wolf wolf, Identifier advancement) {
        if (wolf.getOwnerReference() != null && wolf.level() instanceof ServerLevel level) {
            ServerPlayer owner = level.getServer().getPlayerList().getPlayer(wolf.getOwnerReference().getUUID());
            if (owner != null) ModAdvancements.grant(owner, advancement);
        }
    }

    public static void applyAttack(Wolf wolf) {
        AttributeInstance attack = wolf.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack != null) {
            attack.removeModifier(ATTACK_ID);
            attack.addPermanentModifier(new AttributeModifier(ATTACK_ID, ATTACK_BONUS, AttributeModifier.Operation.ADD_VALUE));
        }
    }
}
