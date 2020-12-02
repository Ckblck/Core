package com.oldust.core.utils.advancement;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.oldust.core.Core;
import lombok.Builder;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftNamespacedKey;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;

@Builder
@SuppressWarnings("UnstableApiUsage")
public class FakeAdvancement {
    private static final String JSON_MODEL = "{\"parent\":\"minecraft:recipes/root\",\"display\":{\"icon\":{\"item\":\"minecraft:{item}\"},\"title\":{\"text\":\"{title}\"},\"description\":{\"text\":\"{desc}\"},\"frame\":\"{frame}\",\"show_toast\":true,\"announce_to_chat\":{announce},\"hidden\":false},\"criteria\":{\"impossible\":{\"trigger\":\"minecraft:impossible\"}},\"requirements\":[[\"impossible\"]]}";

    private final NamespacedKey key; // Ejemplo: new NamespacedKey("oldustcore", "sub/carpeta")
    private final String title;
    private final String description;
    private final String item;
    private final String json;
    private final String frame;
    private final boolean announceToChat;
    private final boolean ready;

    public void show(Player player, boolean remove) {
        Preconditions.checkState(ready);

        net.minecraft.server.v1_16_R3.Advancement advancement = load();

        AdvancementProgress progress = player.getAdvancementProgress(advancement.bukkit);
        Collection<String> remaining = progress.getRemainingCriteria();

        for (String criterion : remaining) {
            progress.awardCriteria(criterion);
        }

        if (remove) {
            Bukkit.getScheduler().runTaskLater(Core.getInstance(), () -> remove(player, advancement), 50);
        }

    }

    public void remove(Player player, Advancement advancement) {
        Preconditions.checkState(ready);

        Collection<String> awardedCriteria = player.getAdvancementProgress(advancement).getAwardedCriteria();

        for (String criterion : awardedCriteria) {
            player.getAdvancementProgress(advancement).revokeCriteria(criterion);
        }

    }

    public void remove(Player player, net.minecraft.server.v1_16_R3.Advancement advancement) {
        remove(player, advancement.bukkit);
    }

    public net.minecraft.server.v1_16_R3.Advancement load() {
        Preconditions.checkState(ready);

        MinecraftServer server = MinecraftServer.getServer();

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

        private FakeAdvancementBuilder ready() {
            return this;
        }

        private FakeAdvancementBuilder json() {
            return this;
        }

    }

}
