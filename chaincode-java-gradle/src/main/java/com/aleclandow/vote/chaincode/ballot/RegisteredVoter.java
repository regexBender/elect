package com.aleclandow.vote.chaincode.ballot;

import com.owlike.genson.annotation.JsonIgnore;

public class RegisteredVoter {
    @JsonIgnore
    public static final String GROUP_NAME = "registeredVoters";

    @JsonIgnore
    public static String createKey(String voterId) {
        return GROUP_NAME + "::" + voterId;
    }

    private final String voterId;

    private final String key;

    private Boolean hasVoted;

    public RegisteredVoter(String voterId, Boolean hasVoted) {
        this.voterId = voterId;
        this.key = createKey(voterId);
        this.hasVoted = hasVoted;
    }

    public String getVoterId() {
        return voterId;
    }

    public String getKey() {
        return key;
    }

    public Boolean getHasVoted() {
        return hasVoted;
    }
}
