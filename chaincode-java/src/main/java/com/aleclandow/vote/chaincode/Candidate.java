/*
 * SPDX-License-Identifier: Apache-2.0
 */

package com.aleclandow.vote.chaincode;

import com.owlike.genson.annotation.JsonProperty;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@DataType()
public final class Candidate {

    @Property()
    private final String id;

    @Property()
    private final String name;

    @Property()
    private final String party;

    @Property()
    private int votes;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getParty() {
        return party;
    }

    public int getVotes() {
        return votes;
    }

    public void setVotes(int votes) {
        this.votes = votes;
    }

    public Candidate(
        @JsonProperty("id") final String id,
        @JsonProperty("name") final String name,
        @JsonProperty("party") final String party
    ) {
        this.id = id;
        this.name = name;
        this.party = party;

        this.votes = 0;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()
            + "@" + Integer.toHexString(hashCode())
            + " [id=" + id
            + ", name=" + name
            + ", party=" + party
            + ", votes=" + votes + "]";
    }
}
