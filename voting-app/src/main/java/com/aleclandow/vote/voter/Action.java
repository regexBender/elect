package com.aleclandow.vote.voter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Action {
    CREATE_BALLOTS("vote"),
    GET_TOTALS("get-totals");

    private final String action;

    public static String ALL_ACTIONS = String.join(" | ", getAllActionsList());

    public String toString() {
        return action;
    }

    private static List<String> getAllActionsList() {
        return Arrays.stream(Action.values()).map(Action::toString).collect(Collectors.toList());
    }
}
