package net.oldust.core.actions.reliable.ack;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import net.oldust.core.utils.lang.Lang;

@Getter
@AllArgsConstructor
public enum Acknowledgment implements Cloneable {
    @SerializedName("proper") PROPER(() -> null),
    @SerializedName("timed_out") TIMED_OUT(() -> null),
    @SerializedName("invalid") INVALID(() -> String.format(Lang.ERROR_FORMATTABLE, "TIMED-OUT"));

    private ReasonCreator reasonCreator;

    public boolean hasReason() {
        return reasonCreator.getReason() != null;
    }

    @SneakyThrows
    public Acknowledgment set(String reason) {
        Acknowledgment clone = (Acknowledgment) this.clone();

        clone.reasonCreator = () -> reason;

        return clone;
    }

}
