package net.oldust.core.actionbar.components;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.oldust.core.Core;
import org.apache.commons.lang.StringEscapeUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.NavigableMap;
import java.util.TreeMap;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Space {
    private static final String COMMA = ";";
    private static final String INVERTED_SLASH = "\\\\";

    private static final NavigableMap<Integer, Space> SPACES = new TreeMap<>();

    private final int number;
    private final String unicode;

    public static void init() {
        try (InputStream resource = Core.class.getResourceAsStream("/spaces.csv");
             BufferedReader br = new BufferedReader(new InputStreamReader(resource))) {
            String line;

            while ((line = br.readLine()) != null) {
                String[] values = line.split(COMMA);

                int number = Integer.parseInt(values[0]);

                String unicodeStr = values[1];
                String[] unicodes = unicodeStr.split(INVERTED_SLASH);

                StringBuilder builder = new StringBuilder();

                for (String unicode : unicodes) {
                    if (unicode.isEmpty()) continue;

                    builder.append(StringEscapeUtils.unescapeJava("\\" + unicode));
                }

                SPACES.put(number, new Space(number, builder.toString()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Space from(int number) {
        Space space = SPACES.get(number);

        if (space != null) {
            return space;
        }

        int nearestKey = findNearestKey(number);
        int difference = number - nearestKey;

        Space nearestSpace = from(nearestKey);

        if (SPACES.containsKey(difference)) {
            Space differenceSpace = from(difference);

            assert nearestSpace != null;
            assert differenceSpace != null;

            String nearestSpaceUnicode = nearestSpace.getUnicode();
            String differenceSpaceUnicode = differenceSpace.getUnicode();

            return new Space(number, nearestSpaceUnicode + differenceSpaceUnicode);
        }

        return null;
    }

    private static int findNearestKey(int number) {
        Integer before = SPACES.floorKey(number);
        Integer after = SPACES.ceilingKey(number);

        if (before == null) return after;
        if (after == null) return before;

        return (number - before < after - number
                || after - number < 0)
                && number - before > 0 ? before : after;
    }

}
