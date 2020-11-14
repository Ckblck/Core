package uk.lewdev.standmodels.exceptions;

import java.util.HashSet;
import java.util.Set;

public class MaterialMismatchException extends Exception {
    private final Set<String> mismatched = new HashSet<>();

    public void addMismatch(String material) {
        mismatched.add(material);
    }

    public Set<String> getMismatched() {
        return mismatched;
    }

}
