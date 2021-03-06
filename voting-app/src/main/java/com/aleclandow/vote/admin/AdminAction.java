package com.aleclandow.vote.admin;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum AdminAction {
    CREATE_BALLOTS("create-ballots"),
    GET_TOTALS("get-totals");

    private final String action;

    public static String ALL_ACTIONS = String.join(" | ", getAllActionsList());

    public String toString() {
        return action;
    }

    private static List<String> getAllActionsList() {
        return Arrays.stream(AdminAction.values()).map(AdminAction::toString).collect(Collectors.toList());
    }
}
