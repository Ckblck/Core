package com.oldust.core.utils.advancement;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerOptions;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.injector.GamePhase;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.oldust.core.Core;
import com.oldust.core.utils.CUtils;
import lombok.Builder;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftNamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 * Clase que permite la creación de mensajes
 * en Advancements.
 * <p>
 * Ejemplo de uso:
 * <p>
 * {@code
 * FakeAdvancement adv = FakeAdvancement.builder()
 * .key(new NamespacedKey("oldustcore", "sub/folder"))
 * .title("Test")
 * .item("heart_of_the_sea")
 * .description("Test")
 * .frame(AdvancementFrameType.GOAL)
 * .announceToChat(false)
 * .build();
 * <p>
 * adv.show(player, true, true);}
 */

@Builder
@SuppressWarnings("UnstableApiUsage")
public class FakeAdvancement implements PacketListener {
    private static final String JSON_MODEL = "{\"parent\":\"minecraft:recipes/root\",\"display\":{\"icon\":{\"item\":\"minecraft:{item}\"},\"title\":{\"text\":\"{title}\"},\"description\":{\"text\":\"{desc}\"},\"frame\":\"{frame}\",\"show_toast\":true,\"announce_to_chat\":{announce},\"hidden\":false},\"criteria\":{\"impossible\":{\"trigger\":\"minecraft:impossible\"}},\"requirements\":[[\"impossible\"]]}";

    private final Set<UUID> receivers = new HashSet<>();
    private final NamespacedKey key; // Ejemplo: new NamespacedKey("oldustcore", "sub/carpeta")
    private final String title;
    private final String description;
    private final String item;
    private final String json;
    private final String frame;
    private final boolean announceToChat;
    private final boolean ready;

    public FakeAdvancement(NamespacedKey key, String title, String description, String item, String json, String frame, boolean announceToChat, boolean ready) {
        this.key = key;
        this.title = title;
        this.description = description;
        this.item = item;
        this.json = json;
        this.frame = frame;
        this.announceToChat = announceToChat;
        this.ready = ready;
    }

    public net.minecraft.server.v1_16_R3.Advancement show(Player player, boolean unshow, boolean remove) {
        Preconditions.checkState(ready);

        receivers.add(player.getUniqueId());
        net.minecraft.server.v1_16_R3.Advancement advancement = load();

        AdvancementProgress progress = player.getAdvancementProgress(advancement.bukkit);
        Collection<String> remaining = progress.getRemainingCriteria();

        for (String criterion : remaining) {
            progress.awardCriteria(criterion);
        }

        if (unshow) {
            Bukkit.getScheduler().runTaskLater(Core.getInstance(), () -> {
                unshow(player, advancement);

                if (remove) remove();
            }, 50);
        } else if (remove) {
            remove(); // TODO: Delete folders as well
        }

        return advancement;
    }

    public void remove() {
        CraftMagicNumbers.INSTANCE.removeAdvancement(key);
        ProtocolLibrary.getProtocolManager().removePacketListener(this);
    }

    public void unshow(Player player, Advancement advancement) {
        Preconditions.checkState(ready);

        receivers.remove(player.getUniqueId());

        Collection<String> awardedCriteria = player.getAdvancementProgress(advancement).getAwardedCriteria();

        for (String criterion : awardedCriteria) {
            player.getAdvancementProgress(advancement).revokeCriteria(criterion);
        }

    }

    public void unshow(Player player, net.minecraft.server.v1_16_R3.Advancement advancement) {
        unshow(player, advancement.bukkit);
    }

    public net.minecraft.server.v1_16_R3.Advancement load() {
        Preconditions.checkState(ready);

        MinecraftServer server = MinecraftServer.getServer();

        ProtocolLibrary.getProtocolManager().addPacketListener(this);

        try {
            loadAdvancement(key, json);
        } catch (IllegalArgumentException ignored) {
        } // Ya está cargado

        AdvancementDataWorld advData = server.getAdvancementData();
        MinecraftKey minecraftKey = CraftNamespacedKey.toMinecraft(key);

        return advData.a(minecraftKey);
    }

    public Advancement loadAdvancement(NamespacedKey key, String advancement) {
        Preconditions.checkState(ready);

        if (Bukkit.getAdvancement(key) != null) {
            throw new IllegalArgumentException("Advancement " + key + " already exists.");
        }

        MinecraftServer server = MinecraftServer.getServer();
        MinecraftKey minecraftkey = CraftNamespacedKey.toMinecraft(key);

        JsonElement jsonelement = AdvancementDataWorld.DESERIALIZER.fromJson(advancement, JsonElement.class);
        JsonObject jsonobject = ChatDeserializer.m(jsonelement, "advancement");

        LootDeserializationContext context = new LootDeserializationContext(minecraftkey,
                server.getLootPredicateManager());

        net.minecraft.server.v1_16_R3.Advancement.SerializedAdvancement nms = net.minecraft.server.v1_16_R3.Advancement.SerializedAdvancement
                .a(jsonobject,
                        context);

        server.getAdvancementData().REGISTRY.a(Maps.newHashMap(Collections.singletonMap(minecraftkey, nms)));
        Advancement bukkit = Bukkit.getAdvancement(key);

        if (bukkit != null) {
            File file = new File(getBukkitDataPackFolder(), "data" + File.separator + key.getNamespace() + File.separator + "advancements" + File.separator + key.getKey() + ".json");
            file.getParentFile().mkdirs();

            try {
                Files.write(advancement, file, Charsets.UTF_8);
            } catch (IOException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "Error saving advancement " + key, ex);
            }

            server.getPlayerList().reload();

            return bukkit;
        }

        return null;
    }

    private static File getBukkitDataPackFolder() {
        return new File(MinecraftServer.getServer().a(SavedFile.DATAPACKS).toFile(), "bukkit");
    }

    /**
     * Evitamos que el Advancement
     * se cargue a los demás jugadores,
     * generando un fps-spike innecesario.
     */

    @Override
    public void onPacketSending(PacketEvent event) {
        Player player = event.getPlayer();
        boolean isNotAwarded = !receivers.contains(player.getUniqueId());

        if (isNotAwarded) {
            event.setCancelled(true);
        }
    }

    @Override
    public void onPacketReceiving(PacketEvent packetEvent) {
        // Not used;
    }

    @Override
    public ListeningWhitelist getSendingWhitelist() {
        return ListeningWhitelist.newBuilder()
                .lowest()
                .gamePhase(GamePhase.PLAYING)
                .options(new ListenerOptions[0])
                .types(PacketType.Play.Server.TAGS)
                .build();
    }

    @Override
    public ListeningWhitelist getReceivingWhitelist() {
        return ListeningWhitelist.newBuilder()
                .lowest()
                .gamePhase(GamePhase.PLAYING)
                .options(new ListenerOptions[0])
                .types(PacketType.Play.Server.TAGS)
                .build();
    }

    @Override
    public Plugin getPlugin() {
        return Core.getInstance();
    }

    public static class FakeAdvancementBuilder {
        public FakeAdvancement build() {
            json = JSON_MODEL;

            json = json.replace("{item}", item);
            json = json.replace("{title}", title);
            json = json.replace("{desc}", description);
            json = json.replace("{frame}", frame);
            json = json.replace("{announce}", Boolean.toString(announceToChat));

            ready = true;

            return new FakeAdvancement(key, title, description, item, json, frame, announceToChat, ready);
        }

        public FakeAdvancementBuilder frame(AdvancementFrameType frame) {
            this.frame = frame.a();

            return this;
        }

        public FakeAdvancementBuilder title(String title) {
            this.title = CUtils.color(title);

            return this;
        }

        private FakeAdvancementBuilder ready() {
            return this;
        }

        private FakeAdvancementBuilder json() {
            return this;
        }

    }

}
