/*
 * SPDX-License-Identifier: Apache-2.0
 */

package com.aleclandow.vote.chaincode.ballot;

import com.owlike.genson.Genson;
import java.util.Date;
import org.apache.commons.collections.CollectionUtils;
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
import org.hyperledger.fabric.shim.ledger.CompositeKey;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

import java.util.ArrayList;
import java.util.List;

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
    private static final String POLLS_OPEN = "pollsOpen";
    private static final String POLLS_CLOSE = "pollsClose";

    private final Genson genson = new Genson();

    private enum BallotTransferErrors {
        CANDIDATE_NOT_FOUND,
        VOTER_NOT_REGISTERED,
        CANDIDATE_ALREADY_EXISTS,
        REGISTERED_VOTER_ALREADY_EXISTS,
        BALLOT_ALREADY_INITIALIZED,
        POLLS_NOT_YET_OPEN,
        POLLS_HAVE_CLOSED,
        VOTER_HAS_ALREADY_VOTED
    }

    /**
     * Creates some initial candidates on the ledger.
     *
     * @param ctx the transaction context
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void InitBallot(final Context ctx, final String pollsOpen, String pollsClose) {
        ChaincodeStub stub = ctx.getStub();
        String pollsOpenString = stub.getStringState(POLLS_OPEN);
        if (pollsOpenString != null && !pollsOpenString.isEmpty()) {
            String errorMessage = "Ballot has already been initialized, pollsOpen = " + pollsOpenString;
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, BallotTransferErrors.BALLOT_ALREADY_INITIALIZED.toString());
        }
        createCandidate(ctx, "T001", "Candidate A", "The Turquoise Party");
        createCandidate(ctx, "M001", "Candidate B", "The Maroon Party");

        stub.putStringState("pollsOpen", pollsOpen);
        stub.putStringState("pollsClose", pollsClose);
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
        String candidateJSON = genson.serialize(candidate);
        stub.putStringState(candidateId, candidateJSON);

        return candidate;
    }

    /**
     * Creates a new registered voter on the ledger.
     *
     * @param ctx the transaction context
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void registerVoter(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();

        String voterId = getVoterIdFromContext(ctx);
        if (registeredVoterExists(ctx)) {
            String errorMessage = String.format("Voter with id = %s has already registered", voterId);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, BallotTransferErrors.REGISTERED_VOTER_ALREADY_EXISTS.toString());
        }

        RegisteredVoter registeredVoter = new RegisteredVoter(voterId, false);
        String registeredVoterJSON = genson.serialize(registeredVoter);
        stub.putStringState(registeredVoter.getKey(), registeredVoterJSON);

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

        Candidate candidate = genson.deserialize(candidateJSON, Candidate.class);
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

        Candidate newCandidate = new Candidate(candidateId, candidateName, candidateParty);
        String newCandidateJSON = genson.serialize(newCandidate);
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
     * Checks the existence of the registered voter on the ledger
     *
     * @param ctx the transaction context
     * @return boolean indicating the existence of the registered voter
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public boolean registeredVoterExists(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();
        String voterId = getVoterIdFromContext(ctx);
        String voterKey = RegisteredVoter.createKey(voterId);
        String candidateJSON = stub.getStringState(voterKey);

        return (candidateJSON != null && !candidateJSON.isEmpty());
    }

    private String getVoterIdFromContext(final Context ctx) {
        return ctx.getClientIdentity().getX509Certificate().getSubjectX500Principal().getName();
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
        Date now = new Date();
        Candidate candidate;
        Candidate newCandidate;

//        try {
            RegisteredVoter registeredVoter = canVoterVote(ctx, now);

            ChaincodeStub stub = ctx.getStub();

            String candidateJSON = stub.getStringState(candidateID);

            if (candidateJSON == null || candidateJSON.isEmpty()) {
                String errorMessage = String.format("Candidate %s does not exist", candidateID);
                System.out.println(errorMessage);
                throw new ChaincodeException(errorMessage, BallotTransferErrors.CANDIDATE_NOT_FOUND.toString());
            }



            candidate = genson.deserialize(candidateJSON, Candidate.class);

            int currentVotes = candidate.getVotes();
            int newVotes = currentVotes + 1;

            // Update the candidate's votes on the ledger
            newCandidate = new Candidate(candidate.getId(), candidate.getName(), candidate.getParty());
            newCandidate.setVotes(newVotes);

            String newCandidateJSON = genson.serialize(newCandidate);
            stub.putStringState(candidateID, newCandidateJSON);

            // Set the registered voter's hasVoted field to true on the ledger
            RegisteredVoter registeredVoterAfterVote = new RegisteredVoter(registeredVoter.getVoterId(), true);

            String registeredVoterAfterVoteJSON = genson.serialize(registeredVoterAfterVote);
            stub.putStringState(registeredVoterAfterVote.getKey(), registeredVoterAfterVoteJSON);
//        } catch (Exception e) {
//            throw new ChaincodeException(e.getMessage(), e.getCause());
//        }

        return newCandidate;
    }

    private RegisteredVoter canVoterVote(Context ctx, Date now) {
        ChaincodeStub stub = ctx.getStub();
        RegisteredVoter registeredVoter;
        String tester = "start";
        try {
            tester += " | " + ctx.toString();
            String pollsOpenString = stub.getStringState(POLLS_OPEN);
            tester += " Onyx | pollsOpenString = " + pollsOpenString;
            tester += " Magmar | " + Long.parseLong(pollsOpenString);

            Date pollsOpen = new Date(Long.parseLong(pollsOpenString));

            tester += " Geodude | pollsOpen = " + pollsOpen.toString();
            tester += " Pikachu | now = " + now.toString();
            if (now.before(pollsOpen)) {
                String errorMessage = "Polls are not yet open. Polls open at " + pollsOpen;
                throw new ChaincodeException(errorMessage, BallotTransferErrors.POLLS_NOT_YET_OPEN.toString());
            }

            tester += "Charizard | pollsOpenString = " + pollsOpenString;

            String pollsCloseString = stub.getStringState(POLLS_CLOSE);
            Date pollsClose = new Date(Long.parseLong(pollsCloseString));
            if (now.after(pollsClose)) {
                String errorMessage = "Polls are now closed. Polls closed at " + pollsClose;
                throw new ChaincodeException(errorMessage, BallotTransferErrors.POLLS_HAVE_CLOSED.toString());
            }
            tester += "Snorlax | pollsCloseString = " + pollsCloseString;
            String voterId = getVoterIdFromContext(ctx);
            String voterKey = RegisteredVoter.createKey(voterId);
            String voterJson = stub.getStringState(voterKey);
            if (voterJson == null || voterJson.isEmpty()) {
                String errorMessage = String.format("Voter %s has not registered", voterId);
                System.out.println(errorMessage);
                throw new ChaincodeException(errorMessage, BallotTransferErrors.VOTER_NOT_REGISTERED.toString());
            }

            tester += " Marowak | voterJson = " + voterJson;

            // At this point, we have verified that the voter has registered
            registeredVoter = genson.deserialize(voterJson, RegisteredVoter.class);

            if (registeredVoter.getHasVoted()) {
                String errorMessage = String.format("Voter %s has already voted", voterId);
                System.out.println(errorMessage);
                throw new ChaincodeException(errorMessage, BallotTransferErrors.VOTER_HAS_ALREADY_VOTED.toString());
            }

            // At this point, we have verified that the registered voter has not yet voted
        } catch (Exception e) {
            throw new ChaincodeException(e.getMessage() + " " + tester);
        }
        return registeredVoter;
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

        final String response;
        try {
            // To retrieve all candidates from the ledger use getStateByRange with empty startKey & endKey.
            // Giving empty startKey & endKey is interpreted as all the keys from beginning to end.
            // As another example, if you use startKey = 'candidate0', endKey = 'candidate9' ,
            // then getStateByRange will retrieve candidate with keys between candidate0 (inclusive) and candidate9 (exclusive) in lexical order.
            QueryResultsIterator<KeyValue> results = stub.getStateByRange("", "");

            for (KeyValue result : results) {
                if (result.getKey().equals("T001") || result.getKey().equals("M001")) {
                    Candidate candidate = genson.deserialize(result.getStringValue(), Candidate.class);
                    queryResults.add(candidate);
                    System.out.println(candidate.toString());
                }
            }

            response = genson.serialize(queryResults);
        } catch (Exception e) {
            throw new ChaincodeException(e.getMessage(), e.getCause());
        }

        return response;
    }
}
