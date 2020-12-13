package net.oldust.core.inherited.plugins;

public abstract class Plugin {

    public abstract void onEnable();

    public abstract void onDisable();

    public final String getName() {
        return getClass().getAnnotation(InheritedPlugin.class).name();
    }

}
