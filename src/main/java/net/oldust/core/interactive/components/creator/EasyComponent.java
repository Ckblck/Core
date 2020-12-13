package net.oldust.core.interactive.components.creator;

import com.google.gson.JsonObject;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import net.oldust.core.utils.CUtils;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

/**
 * Credits: https://github.com/Syr0ws/EasyComponent
 */

public class EasyComponent<T extends BaseComponent, SELF extends EasyComponent<T, SELF>> {

    private final T component;

    public EasyComponent(T component) {
        this.component = component;
    }

    public SELF setColor(ChatColor color) {
        this.component.setColor(color);

        return this.self();
    }

    public SELF setBold(boolean bold) {
        this.component.setBold(bold);

        return this.self();
    }

    public SELF setItalic(boolean italic) {
        this.component.setItalic(italic);

        return this.self();
    }

    public SELF setUnderlined(boolean underlined) {
        this.component.setUnderlined(underlined);

        return this.self();
    }

    public SELF setObfuscated(boolean obfuscated) {
        this.component.setObfuscated(obfuscated);

        return this.self();
    }

    public SELF setStrikethrough(boolean strikethrough) {
        this.component.setStrikethrough(strikethrough);

        return this.self();
    }

    public SELF showText(String text) {
        BaseComponent[] base = new ComponentBuilder(parseColors(text)).create();
        this.component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, base));

        return this.self();
    }

    public SELF runCommand(String command) {
        this.component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + command));

        return this.self();
    }

    public SELF suggestCommand(String command) {
        this.component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/" + command));

        return this.self();
    }

    public SELF openUrl(String url) {
        this.component.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));

        return this.self();
    }

    public SELF addExtra(EasyComponent<?, ?> component) {
        this.component.addExtra(component.getComponent());

        return this.self();
    }

    public SELF addExtra(TextComponent component) {
        this.component.addExtra(component);

        return this.self();
    }

    public SELF setExtra(List<BaseComponent> extra) {
        if (extra != null && extra.size() > 0) this.component.setExtra(extra);

        return this.self();
    }

    protected SELF getFromJson(JsonObject object) {
        if (object.has("showText"))
            this.showText(object.get("showText").getAsString());

        if (object.has("color"))
            this.setColor(ChatColor.valueOf(object.get("color").getAsString()));

        if (object.has("suggestCommand"))
            this.suggestCommand(object.get("suggestCommand").getAsString());

        if (object.has("runCommand"))
            this.runCommand(object.get("runCommand").getAsString());

        if (object.has("openUrl"))
            this.openUrl(object.get("openUrl").getAsString());

        this.setBold(object.has("bold") && object.get("bold").getAsBoolean());
        this.setItalic(object.has("italic") && object.get("italic").getAsBoolean());
        this.setObfuscated(object.has("obfuscated") && object.get("obfuscated").getAsBoolean());
        this.setStrikethrough(object.has("strikethrough") && object.get("strikethrough").getAsBoolean());

        return this.self();
    }

    protected SELF getFromYaml(ConfigurationSection section) {
        if (section.contains("show-text"))
            this.showText(section.getString("show-text"));

        if (section.contains("color"))
            this.setColor(ChatColor.valueOf(section.getString("color")));

        if (section.contains("suggest-command"))
            this.suggestCommand(section.getString("suggest-command"));

        if (section.contains("run-command"))
            this.runCommand(section.getString("run-command"));

        if (section.contains("open-url"))
            this.openUrl(section.getString("open-url"));

        this.setBold(section.contains("bold") && section.getBoolean("bold"));
        this.setItalic(section.contains("italic") && section.getBoolean("italic"));
        this.setObfuscated(section.contains("obfuscated") && section.getBoolean("obfuscated"));
        this.setStrikethrough(section.contains("strikethrough") && section.getBoolean("strikethrough"));

        return this.self();
    }

    protected String parseColors(String message) {
        return CUtils.color(message);
    }

    @SuppressWarnings("unchecked")
    private SELF self() {
        return (SELF) this;
    }

    public List<BaseComponent> getExtra() {
        return this.component.getExtra() != null ? this.component.getExtra() : new ArrayList<>();
    }

    public T getComponent() {
        return this.component;
    }
}