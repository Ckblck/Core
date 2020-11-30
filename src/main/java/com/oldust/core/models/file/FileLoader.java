package com.oldust.core.models.file;

import com.oldust.core.Core;
import com.oldust.core.utils.CUtils;
import uk.lewdev.standmodels.exceptions.MaterialMismatchException;
import uk.lewdev.standmodels.parser.ModelBuildInstruction;
import uk.lewdev.standmodels.parser.ModelSpawnCommandParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.Set;

public class FileLoader {
    private static final File MODELS_FOLDER = new File(Core.getInstance().getDataFolder(), "models");

    static {
        MODELS_FOLDER.mkdirs();
    }

    public Set<ModelBuildInstruction> getInstructions(String fileName) throws NoSuchFileException, IllegalArgumentException, MaterialMismatchException {
        CUtils.warnSyncCall();

        File file = new File(MODELS_FOLDER, fileName);

        if (!file.exists()) throw new NoSuchFileException(fileName);

        String command = parseFile(file);
        ModelSpawnCommandParser parser = new ModelSpawnCommandParser(command);

        Set<ModelBuildInstruction> instructions = parser.getInstructions();

        if (instructions.isEmpty()) throw new IllegalArgumentException();

        return instructions;
    }

    private String parseFile(File file) {
        StringBuilder builder = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return builder.toString();
    }

}
