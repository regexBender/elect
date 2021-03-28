package com.aleclandow.vote.ledger;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Transaction {
    INIT_BALLOT("InitBallot"),
    GET_BALLOT("getBallot");

    private final String transaction;

    public String toString() {
        return transaction;
    }
}
