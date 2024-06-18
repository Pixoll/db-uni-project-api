package org.dbuniproject.api;

import java.util.ArrayList;
import java.util.List;

public class Util {
    public static ArrayList<Integer> queryListToIntegerList(List<String> list) {
        final ArrayList<Integer> integers = new ArrayList<>();

        for (final String string : list) {
            for (final String number : string.split(",")) {
                try {
                    integers.add(Integer.parseInt(number));
                } catch (NumberFormatException ignored) {
                }
            }
        }

        return integers;
    }
}
