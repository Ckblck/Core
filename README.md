# OldustCore

OldustCore is the Core of the server. It aims to provide a **wide**, **extensive** and **flexible** codebase for the
future ideas and projects.

## EventsProvider

Due to the verbosely nature of CraftBukkit's events, the class **EventsProvider** was created, with the purpose of not
having to `implement Listener` every time when a recurrent event (such as `PlayerJoinEvent` / `PlayerQuitEvent`) was
needed.

The utility provides two variables at the moment of execution, `? extends PlayerEvent` and `ImmutableWrappedDatabase`.

Usage examples:

```java
provider.newOperation(PlayerQuitEvent.class, (ev, db) -> {
    UUID uuid=ev.getPlayer().getUniqueId();

    cache.remove(uuid);
}, EventPriority.LOWEST);
```

```java
provider.newOperation(PlayerJoinEvent.class, (ev, db) -> {
    Player player=ev.getPlayer();

    ConquerPlayer conquerPlayer=createInstance(player.getUniqueId());

    server.onPlayerJoin(player,conquerPlayer);
}, EventPriority.HIGHEST);
```

Keep in mind the operations are invoked in the server's **main-thread**.

> **Why is the WrappedDatabase Immutable?** These operations are executed procedurally and in a matter of nanoseconds. If we modified the database and then **uploaded it to Redis**, it would cause some major problems, because Redis uploads are **not blocking operations**, as they **has to be done asynchronously**.
> This doesn't happen when the class is modified, though, because the reference is stored in cache, and the **WrappedDatabase** object is **ThreadSafe**.
> To keep it simple, as long as the modifications are NOT uploaded to Redis, all will be OK.
> Either way, **in order to avoid having to deal with these complications**, *the database was marked as **Immutable***.

## Simple considerations

* Don't be lazy, try to document complex code-blocks. (**English**)
* Always keep in mind a human will probably have to dive into your code in the future.
* Have fun, this is a project, not a job (**yet**). But don't mind asking the main author if you have any questions.

## Project-related TODOs

* Translate every Javadoc / comment to English.

---

> This **README** was created to explain some project-related features that might be difficult to understand at first. If you think a new change you have made might need explicit explanation, don't mind extending this **README**.
