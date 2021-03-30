/*
 * SPDX-License-Identifier: Apache-2.0
 */

package com.aleclandow.vote.chaincode;

import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contact;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.License;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

@Contract(
        name = "basic-ballot",
        info = @Info(
                title = "Ballot Transfer",
                description = "Basic ballot transfer",
                version = "0.0.1-SNAPSHOT",
                license = @License(
                        name = "Apache 2.0 License",
                        url = "http://www.apache.org/licenses/LICENSE-2.0.html"),
                contact = @Contact(
                        email = "ajlandow@ncsu.edu",
                        name = "Alec Landow",
                        url = "https://hyperledger.example.com")))
@Default
public final class BallotTransfer implements ContractInterface {

    private final Gson gson = new Gson();

    private enum BallotTransferErrors {
        CANDIDATE_NOT_FOUND,
        CANDIDATE_ALREADY_EXISTS
    }

    /**
     * Creates some initial candidates on the ledger.
     *
     * @param ctx the transaction context
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void InitBallot(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();

        createCandidate(ctx, "T001", "Candidate A", "The Turquoise Party");
        createCandidate(ctx, "M001", "Candidate B", "The Maroon Party");

    }

    /**
     * Creates a new candidate on the ledger.
     *
     * @param ctx the transaction context
     * @param candidateId the ID of the new candidate
     * @param candidateName the name of the new candidate
     * @param candidateParty the party to which the new candidate is affiliated
     * @return the created candidate
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Candidate createCandidate(
        final Context ctx,
        final String candidateId,
        final String candidateName,
        final String candidateParty
    ) {
        ChaincodeStub stub = ctx.getStub();

        if (candidateExists(ctx, candidateId) ) {
            String errorMessage = String.format("Candidate with id = %s already exists", candidateId);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, BallotTransferErrors.CANDIDATE_ALREADY_EXISTS.toString());
        }

        Candidate candidate = new Candidate(candidateId, candidateName, candidateParty);
        String candidateJSON = gson.toJson(candidate);
        stub.putStringState(candidateId, candidateJSON);

        return candidate;
    }

    /**
     * Retrieves an candidate with the specified ID from the ledger.
     *
     * @param ctx the transaction context
     * @param candidateID the ID of the candidate
     * @return the candidate found on the ledger if there was one
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public Candidate ReadCandidate(final Context ctx, final String candidateID) {
        ChaincodeStub stub = ctx.getStub();
        String candidateJSON = stub.getStringState(candidateID);

        if (candidateJSON == null || candidateJSON.isEmpty()) {
            String errorMessage = String.format("Candidate %s does not exist", candidateID);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, BallotTransferErrors.CANDIDATE_NOT_FOUND.toString());
        }

        Candidate
            candidate = gson.fromJson(candidateJSON, Candidate.class);
        return candidate;
    }

    /**
     * Updates the properties of an candidate on the ledger.
     *
     * @param ctx the transaction context
     * @param candidateId the ID of the new candidate
     * @param candidateName the name of the new candidate
     * @param candidateParty the party to which the new candidate is affiliated
     * @return the transferred candidate
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Candidate UpdateCandidate(final Context ctx, final String candidateId, final String candidateName, final String candidateParty) {
        ChaincodeStub stub = ctx.getStub();

        if (!candidateExists(ctx, candidateId)) {
            String errorMessage = String.format("Candidate with id = %s does not exist", candidateId);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, BallotTransferErrors.CANDIDATE_NOT_FOUND.toString());
        }

        Candidate
            newCandidate = new Candidate(candidateId, candidateName, candidateParty);
        String newCandidateJSON = gson.toJson(newCandidate);
        stub.putStringState(candidateId, newCandidateJSON);

        return newCandidate;
    }

    /**
     * Deletes candidate on the ledger.
     *
     * @param ctx the transaction context
     * @param candidateID the ID of the candidate being deleted
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void deleteCandidate(final Context ctx, final String candidateID) {
        ChaincodeStub stub = ctx.getStub();

        if (!candidateExists(ctx, candidateID)) {
            String errorMessage = String.format("Candidate with id = %s does not exist", candidateID);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, BallotTransferErrors.CANDIDATE_NOT_FOUND.toString());
        }

        stub.delState(candidateID);
    }

    /**
     * Checks the existence of the candidate on the ledger
     *
     * @param ctx the transaction context
     * @param candidateId the ID of the candidate
     * @return boolean indicating the existence of the candidate
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public boolean candidateExists(final Context ctx, final String candidateId) {
        ChaincodeStub stub = ctx.getStub();
        String candidateJSON = stub.getStringState(candidateId);

        return (candidateJSON != null && !candidateJSON.isEmpty());
    }

    /**
     * Changes the owner of a candidate on the ledger.
     *
     * @param ctx the transaction context
     * @param candidateID the ID of the candidate being transferred
     * @return the updated candidate
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Candidate castOneVoteForCandidate(final Context ctx, final String candidateID) {
        ChaincodeStub stub = ctx.getStub();
        String candidateJSON = stub.getStringState(candidateID);

        if (candidateJSON == null || candidateJSON.isEmpty()) {
            String errorMessage = String.format("Candidate %s does not exist", candidateID);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, BallotTransferErrors.CANDIDATE_NOT_FOUND.toString());
        }

        Candidate
            candidate = gson.fromJson(candidateJSON, Candidate.class);

        int currentVotes = candidate.getVotes();
        int newVotes = currentVotes + 1;

        Candidate
            newCandidate = new Candidate(candidate.getId(), candidate.getName(), candidate.getParty());
        newCandidate.setVotes(newVotes);

        String newCandidateJSON = gson.toJson(newCandidate);
        stub.putStringState(candidateID, newCandidateJSON);

        return newCandidate;
    }

    /**
     * Retrieves all candidates from the ledger.
     *
     * @param ctx the transaction context
     * @return array of candidates found on the ledger
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String getBallot(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();

        List<Candidate> queryResults = new ArrayList<>();

        // To retrieve all candidates from the ledger use getStateByRange with empty startKey & endKey.
        // Giving empty startKey & endKey is interpreted as all the keys from beginning to end.
        // As another example, if you use startKey = 'candidate0', endKey = 'candidate9' ,
        // then getStateByRange will retrieve candidate with keys between candidate0 (inclusive) and candidate9 (exclusive) in lexical order.
        QueryResultsIterator<KeyValue> results = stub.getStateByRange("", "");

        for (KeyValue result: results) {
            Candidate candidate = gson.fromJson(result.getStringValue(), Candidate.class);
            queryResults.add(candidate);
            System.out.println(candidate.toString());
        }

        final String response = gson.toJson(queryResults);

        return response;
    }
}
