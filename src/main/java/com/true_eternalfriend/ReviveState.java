package com.true_eternalfriend;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/** Overworld-backed queue. The codec stores a compact JSON array inside SavedData. */
public final class ReviveState extends SavedData {
    private static final Identifier ID = Identifier.fromNamespaceAndPath(TrueEternalFriend.MOD_ID, "revivals");
    private static final Codec<ReviveState> CODEC = Codec.STRING.xmap(ReviveState::decode, ReviveState::encode);
    private static final SavedDataType<ReviveState> TYPE = new SavedDataType<>(ID, ReviveState::new, CODEC, DataFixTypes.LEVEL);
    private final List<Snapshot> pending = new ArrayList<>();
    private final Set<UUID> revivalAwards = new LinkedHashSet<>();
    private int dirtyClock;

    private ReviveState() { }
    public static ReviveState get(MinecraftServer server) { return server.overworld().getDataStorage().computeIfAbsent(TYPE); }

    public void enqueue(Wolf wolf) {
        // The same original UUID cannot enter the queue twice.
        for (Snapshot s : pending) if (s.original.equals(wolf.getUUID())) return;
        UUID owner = wolf.getOwnerReference() == null ? null : wolf.getOwnerReference().getUUID();
        JsonElement name = wolf.getCustomName() == null ? null : ComponentSerialization.CODEC.encodeStart(JsonOps.INSTANCE, wolf.getCustomName()).result().orElse(null);
        pending.add(new Snapshot(UUID.randomUUID(), wolf.getUUID(), owner, name, wolf.getCollarColor().getId(),
            wolf.isOrderedToSit(), wolf.getHealth(), wolf.getX(), wolf.getY(), wolf.getZ(),
            wolf.level().dimension().identifier().toString(), TrueEternalFriend.REVIVE_TICKS, System.currentTimeMillis()));
        setDirty();
    }

    public void tick(MinecraftServer server) {
        Iterator<Snapshot> iterator = pending.iterator();
        while (iterator.hasNext()) {
            Snapshot s = iterator.next();
            if (s.remaining > 0) { --s.remaining; continue; }
            ServerLevel level = null;
            for (ServerLevel candidate : server.getAllLevels())
                if (candidate.dimension().identifier().toString().equals(s.dimension)) { level = candidate; break; }
            if (level == null) { s.remaining = 20; setDirty(); continue; }
            BlockPos pos = BlockPos.containing(s.x, s.y, s.z);
            if (!level.hasChunkAt(pos)) { s.remaining = 20; setDirty(); continue; }
            // Keep the original UUID. If a save crashes after spawn but before queue flush,
            // this check prevents a second wolf when the queue is replayed.
            if (level.getEntityInAnyDimension(s.original) != null) { iterator.remove(); setDirty(); continue; }
            Wolf wolf = EntityTypes.WOLF.create(level, EntitySpawnReason.EVENT);
            if (wolf == null) { s.remaining = 20; setDirty(); continue; }
            wolf.setUUID(s.original);
            wolf.setPos(s.x, s.y, s.z);
            wolf.setTame(true, true);
            if (s.owner != null) wolf.setOwnerReference(EntityReference.of(s.owner));
            if (s.name != null) ComponentSerialization.CODEC.parse(JsonOps.INSTANCE, s.name).result().ifPresent(wolf::setCustomName);
            wolf.setComponent(DataComponents.WOLF_COLLAR, DyeColor.byId(s.collar));
            wolf.setOrderedToSit(s.sitting);
            wolf.setInSittingPose(s.sitting);
            ((EternalWolf) wolf).trueEternalFriend$setEternal(true);
            TrueEternalFriend.applyAttack(wolf);
            wolf.setHealth(wolf.getMaxHealth());
            if (!level.addFreshEntity(wolf)) { s.remaining = 20; setDirty(); continue; }
            iterator.remove();
            setDirty();
            level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, s.x, s.y + 0.6, s.z, 48, 0.6, 0.8, 0.6, 0.15);
            level.playSound(null, pos, SoundEvents.TOTEM_USE, SoundSource.NEUTRAL, 1, 1);
            if (s.owner != null) {
                revivalAwards.add(s.owner);
                ServerPlayer owner = server.getPlayerList().getPlayer(s.owner);
                if (owner != null) owner.sendSystemMessage(Component.translatable("message.true_eternal_friend.revived"));
            }
        }
        Iterator<UUID> awards = revivalAwards.iterator();
        while (awards.hasNext()) {
            UUID ownerId = awards.next();
            ServerPlayer owner = server.getPlayerList().getPlayer(ownerId);
            if (owner != null && ModAdvancements.grant(owner, ModAdvancements.FIRST_REVIVAL)) {
                awards.remove();
                setDirty();
            }
        }
        if (++dirtyClock >= 20) { dirtyClock = 0; if (!pending.isEmpty()) setDirty(); }
    }

    private String encode() {
        JsonArray array = new JsonArray();
        for (Snapshot s : pending) {
            JsonObject o = new JsonObject();
            o.addProperty("reviveId", s.reviveId.toString()); o.addProperty("original", s.original.toString());
            if (s.owner != null) o.addProperty("owner", s.owner.toString());
            if (s.name != null) o.add("name", s.name);
            o.addProperty("collar", s.collar); o.addProperty("sitting", s.sitting); o.addProperty("health", s.health);
            o.addProperty("x", s.x); o.addProperty("y", s.y); o.addProperty("z", s.z);
            o.addProperty("dimension", s.dimension); o.addProperty("remaining", s.remaining);
            o.addProperty("deathMillis", s.deathMillis); o.addProperty("attackBonus", TrueEternalFriend.ATTACK_BONUS);
            array.add(o);
        }
        JsonObject root = new JsonObject();
        root.add("pending", array);
        JsonArray awards = new JsonArray();
        for (UUID owner : revivalAwards) awards.add(owner.toString());
        root.add("revivalAwards", awards);
        return root.toString();
    }

    private static ReviveState decode(String json) {
        ReviveState state = new ReviveState();
        JsonElement parsed = JsonParser.parseString(json);
        JsonArray entries = parsed.isJsonArray() ? parsed.getAsJsonArray() : parsed.getAsJsonObject().getAsJsonArray("pending");
        if (!parsed.isJsonArray() && parsed.getAsJsonObject().has("revivalAwards")) {
            for (JsonElement owner : parsed.getAsJsonObject().getAsJsonArray("revivalAwards"))
                state.revivalAwards.add(UUID.fromString(owner.getAsString()));
        }
        for (JsonElement item : entries) {
            JsonObject o = item.getAsJsonObject();
            state.pending.add(new Snapshot(UUID.fromString(o.get("reviveId").getAsString()), UUID.fromString(o.get("original").getAsString()),
                o.has("owner") ? UUID.fromString(o.get("owner").getAsString()) : null, o.get("name"),
                o.get("collar").getAsInt(), o.get("sitting").getAsBoolean(), o.get("health").getAsFloat(),
                o.get("x").getAsDouble(), o.get("y").getAsDouble(), o.get("z").getAsDouble(),
                o.get("dimension").getAsString(), o.get("remaining").getAsInt(), o.get("deathMillis").getAsLong()));
        }
        return state;
    }

    private static final class Snapshot {
        final UUID reviveId, original, owner;
        final JsonElement name;
        final int collar;
        final boolean sitting;
        final float health;
        final double x, y, z;
        final String dimension;
        final long deathMillis;
        int remaining;
        Snapshot(UUID reviveId, UUID original, UUID owner, JsonElement name, int collar, boolean sitting, float health,
                 double x, double y, double z, String dimension, int remaining, long deathMillis) {
            this.reviveId=reviveId; this.original=original; this.owner=owner; this.name=name; this.collar=collar;
            this.sitting=sitting; this.health=health; this.x=x; this.y=y; this.z=z; this.dimension=dimension;
            this.remaining=remaining; this.deathMillis=deathMillis;
        }
    }
}
