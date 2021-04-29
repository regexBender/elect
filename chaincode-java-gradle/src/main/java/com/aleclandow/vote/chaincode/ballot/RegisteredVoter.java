package com.aleclandow.vote.chaincode.ballot;

import com.owlike.genson.annotation.JsonIgnore;
import com.owlike.genson.annotation.JsonProperty;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@DataType()
public class RegisteredVoter {
    @JsonIgnore
    public static final String GROUP_NAME = "registeredVoters";

    @JsonIgnore
    public static String createKey(String voterId) {
        return GROUP_NAME + "::" + voterId;
    }

    @Property()
    private final String voterId;

    @Property()
    private final String key;

    @Property()
    private Boolean hasVoted;

    public RegisteredVoter(@JsonProperty("voterId") String voterId, @JsonProperty("hasVoted") Boolean hasVoted) {
        this.voterId = voterId;
        this.key = createKey(voterId);
        this.hasVoted = hasVoted;
    }

    public RegisteredVoter(
        @JsonProperty("voterId") String voterId,
        @JsonProperty("key") String key,
        @JsonProperty("hasVoted") Boolean hasVoted
    ) {
        this.voterId = voterId;
        this.key = key;
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
