package com.aleclandow.vote.test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum TestAction {
    BLOCKING("blocking"),
    PARALLEL("parallel");

    private final String action;

    public static String ALL_ACTIONS = String.join(" | ", getAllActionsList());

    public String toString() {
        return action;
    }

    private static List<String> getAllActionsList() {
        return Arrays.stream(TestAction.values()).map(TestAction::toString).collect(Collectors.toList());
    }
}
