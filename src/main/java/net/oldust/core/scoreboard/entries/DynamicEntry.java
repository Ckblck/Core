package net.oldust.core.scoreboard.entries;

import com.google.common.base.Preconditions;
import lombok.Getter;
import net.oldust.core.Core;
import net.oldust.core.scoreboard.Line;
import net.oldust.core.scoreboard.ServerScoreboard;
import net.oldust.core.scoreboard.TeamEntry;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * A DynamicEntry is expected to be modified
 * periodically.
 * One DynamicEntry is one line in the scoreboard.
 */

@Getter
public class DynamicEntry extends TeamEntry {
    private final List<DynamicProperty> properties;
    private final BukkitTask task;
    private final Line line;
    private final Predicate<DynamicEntry> cancelPredicate;
    private final Consumer<DynamicEntry> cancelledConsumer;

    private int iteration;

    /**
     * Constructs a new DynamicEntry.
     *
     * @param serverScoreboard  scoreboard to add line to
     * @param line              line to add
     * @param properties        the properties which will modify the line
     * @param async             should the property processing be done async?
     * @param taskDelay         initial delay for the task to run
     * @param taskPeriod        period of the task
     * @param cancelPredicate   nullable, predicate to specify when the task should be cancelled
     * @param cancelledConsumer nullable, consumer which will be executed after the task is cancelled
     */

    private DynamicEntry(ServerScoreboard serverScoreboard, Line line,
                         List<DynamicProperty> properties, boolean async, int taskDelay, int taskPeriod,
                         @Nullable Predicate<DynamicEntry> cancelPredicate,
                         @Nullable Consumer<DynamicEntry> cancelledConsumer) {
        super(serverScoreboard);

        this.line = line;
        this.properties = properties;
        this.cancelPredicate = cancelPredicate;
        this.cancelledConsumer = cancelledConsumer;

        if (async) {
            this.task = Bukkit.getScheduler().runTaskTimerAsynchronously(Core.getInstance(),
                    this::run, taskDelay, taskPeriod);
        } else {
            this.task = Bukkit.getScheduler().runTaskTimer(Core.getInstance(),
                    this::run, taskDelay, taskPeriod);
        }

        setEntry(create()); // after variable initialization
    }

    @Override
    public String getText() {
        return line.getBaseLine();
    }

    public void run() {
        boolean shouldCancel = cancelPredicate.test(this);

        if (shouldCancel) {
            task.cancel();
            cancelledConsumer.accept(this);

            return;
        }

        String text = null;

        for (DynamicProperty property : properties) {
            text = property.modify(line);
        }

        Team team = getEntry();
        assert text != null; // At least one property modifies the text.

        team.setPrefix(text);

        iteration++;
    }

    public static DynamicEntryBuilder builder() {
        return new DynamicEntryBuilder();
    }

    public static class DynamicEntryBuilder {
        private final List<DynamicProperty> properties = new ArrayList<>();

        private ServerScoreboard serverScoreboard;
        private Line line;
        private boolean async = true;
        private int delay, period = -1;
        private Predicate<DynamicEntry> cancelPredicate;
        private Consumer<DynamicEntry> cancelledConsumer;

        private DynamicEntryBuilder() {
        }

        /**
         * Establishes the scoreboard to be modifying.
         */

        public DynamicEntryBuilder scoreboard(ServerScoreboard serverScoreboard) {
            this.serverScoreboard = serverScoreboard;

            return this;
        }

        /**
         * Sets the task's properties.
         *
         * @param async  should the task be async?
         * @param delay  delay of task
         * @param period period of task
         */

        public DynamicEntryBuilder task(boolean async, int delay, int period) {
            this.async = async;
            this.delay = delay;
            this.period = period;

            return this;
        }

        /**
         * Sets the task's properties.
         * NOTE: The task will be async.
         *
         * @param delay  delay of task
         * @param period period of task
         */

        public DynamicEntryBuilder task(int delay, int period) {
            return task(true, delay, period);
        }

        /**
         * Adds an array of properties.
         */

        public DynamicEntryBuilder withProperties(DynamicProperty... properties) {
            this.properties.addAll(Arrays.asList(properties));

            return this;
        }

        /**
         * Adds a property to the list.
         */

        public DynamicEntryBuilder withProperty(DynamicProperty property) {
            properties.add(property);

            return this;
        }

        /**
         * Establishes the line which will be showed
         * in the scoreboard.
         */

        public DynamicEntryBuilder line(Line line) {
            this.line = line;

            return this;
        }

        /**
         * Optional - Provides a Predicate that tells
         * the task when to stop.
         */

        public DynamicEntryBuilder cancelWhen(Predicate<DynamicEntry> predicate) {
            this.cancelPredicate = predicate;

            return this;
        }

        /**
         * Optional - Provides a Consumer that gets
         * executed when the task is cancelled.
         */

        public DynamicEntryBuilder whenCancelled(Consumer<DynamicEntry> consumer) {
            this.cancelledConsumer = consumer;

            return this;
        }

        public DynamicEntry build() {
            Preconditions.checkState(serverScoreboard != null, "serverScoreboard cannot be null.");
            Preconditions.checkState(line != null, "line cannot be null");
            Preconditions.checkState(!properties.isEmpty(), "At least one property must be specified.");
            Preconditions.checkState(delay != -1 || period != -1, "delay or period must be specified.");

            return new DynamicEntry(
                    serverScoreboard, line, properties, async,
                    delay, period, cancelPredicate, cancelledConsumer
            );
        }

    }

}
