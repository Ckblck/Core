package net.oldust.core.utils.advancement;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Builder;
import net.minecraft.server.v1_16_R3.*;
import net.oldust.core.Core;
import net.oldust.core.utils.CUtils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftNamespacedKey;
import org.bukkit.entity.Player;

import java.util.*;

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
public class FakeAdvancement {
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
            remove();
        }

        return advancement;
    }

    public void remove() {
        CraftMagicNumbers.INSTANCE.removeAdvancement(key);
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

        try {
            loadAdvancement(key, json);
        } catch (IllegalArgumentException ignored) {
        } // Ya está cargado

        AdvancementDataWorld advData = server.getAdvancementData();
        MinecraftKey minecraftKey = CraftNamespacedKey.toMinecraft(key);

        return advData.a(minecraftKey);
    }

    public void loadAdvancement(NamespacedKey key, String advancement) {
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
    }

    public static class FakeAdvancementBuilder {
        public FakeAdvancement build() {
            Preconditions.checkNotNull(item);
            Preconditions.checkNotNull(title);
            Preconditions.checkNotNull(description);
            Preconditions.checkNotNull(frame);
            Preconditions.checkNotNull(key);

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
