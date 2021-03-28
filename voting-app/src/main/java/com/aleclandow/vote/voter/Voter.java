package com.aleclandow.vote.voter;

import static com.aleclandow.util.ApplicationProperties.applicationProperties;
import static com.aleclandow.vote.admin.Admin.ADMIN_ID;
import static com.aleclandow.vote.ledger.CertificateAuthority.caClient;


import com.aleclandow.vote.admin.GhostAdmin;
import com.aleclandow.vote.ledger.HyperLedgerConnector;
import java.nio.file.Paths;
import java.util.Set;
import org.hyperledger.fabric.gateway.Identities;
import org.hyperledger.fabric.gateway.Identity;
import org.hyperledger.fabric.gateway.Wallet;
import org.hyperledger.fabric.gateway.Wallets;
import org.hyperledger.fabric.gateway.X509Identity;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric_ca.sdk.RegistrationRequest;

public class Voter implements User {

    private final HyperLedgerConnector hyperLedgerConnector;

    private final String voterId;

    public Voter(String voterId) throws Exception {
        hyperLedgerConnector = new HyperLedgerConnector();
        this.voterId = voterId;
        registerAndEnrollWithCertificateAuthority();
    }

    public void voteForCandidate(String candidateId) {
        hyperLedgerConnector.castVote(voterId, candidateId, applicationProperties.getVoterContractName());
    }

    public void getBallot() {
        hyperLedgerConnector.getBallotFromLedger(voterId, applicationProperties.getVoterContractName());
    }

    public void getTotals() {
        hyperLedgerConnector.getTotalsFromLedger(voterId, applicationProperties.getVoterContractName());
    }

    public void registerAndEnrollWithCertificateAuthority() throws Exception {

        Wallet wallet = Wallets.newFileSystemWallet(Paths.get("wallet"));

        // Check to see if we've already enrolled the voter
        if (wallet.get(voterId) != null) {
            System.out.printf("An identity for the admin user \"%s\" already exists in the wallet%n", voterId);
            return;
        }

        X509Identity adminIdentity = (X509Identity) wallet.get("admin");
        if (adminIdentity == null) {
            System.out.printf("\"%s\" needs to be enrolled and added to the wallet first%n", ADMIN_ID);
            return;
        }

        // Register the user, enroll the user, and import the new identity into the wallet.
        RegistrationRequest registrationRequest = new RegistrationRequest(voterId);
        registrationRequest.setAffiliation("org1.department1");
        registrationRequest.setEnrollmentID(voterId);

        // The Ghost Admin is a temporary workaround until a proper implementation of an actual admin enrolls the user is built
        GhostAdmin ghostAdmin = new GhostAdmin(ADMIN_ID, "org1.department1", "Org1MSP", adminIdentity);
        String enrollmentSecret = ghostAdmin.registerVoter(registrationRequest);

        Enrollment enrollment = caClient.enroll(voterId, enrollmentSecret);
        Identity voter = Identities.newX509Identity("Org1MSP", enrollment);
        wallet.put(voterId, voter);
        System.out.printf("Successfully enrolled user \"%s\" and imported it into the wallet%n", voterId);
    }

    @Override
    public String getName() {
        return voterId;
    }

    @Override
    public Set<String> getRoles() {
        return null;
    }

    @Override
    public String getAccount() {
        return null;
    }

    @Override
    public String getAffiliation() {
        return null;
    }

    @Override
    public Enrollment getEnrollment() {
        return null;
    }

    @Override
    public String getMspId() {
        return null;
    }
}
