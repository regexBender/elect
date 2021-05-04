package com.aleclandow.vote;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Mode {

    REGISTER("register"),
    VOTE("vote"),
    ADMIN("admin"),
    TEST("test"),
    EXIT("exit");

    private final String mode;

    public static String ALL_MODES = String.join(" | ", getAllModesList());

    public String toString() {
        return mode;
    }

    private static List<String> getAllModesList() {
        return Arrays.stream(Mode.values()).map(Mode::toString).collect(Collectors.toList());
    }

}
