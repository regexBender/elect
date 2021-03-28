package com.aleclandow.vote.ledger;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Transaction {
    INIT_BALLOT("InitBallot"),
    CAST_ONE_VOTE_FOR_CANDIDATE("castOneVoteForCandidate"),
    GET_BALLOT("getBallot");

    private final String transaction;

    public String toString() {
        return transaction;
    }
}
