package com.aleclandow.vote.admin;

import static com.aleclandow.util.ApplicationProperties.applicationProperties;
import static com.aleclandow.vote.ledger.CertificateAuthority.caClient;


import com.aleclandow.vote.ledger.HyperLedgerConnector;
import java.io.IOException;
import java.nio.file.Paths;
import java.security.cert.CertificateException;
import org.hyperledger.fabric.gateway.Identities;
import org.hyperledger.fabric.gateway.Identity;
import org.hyperledger.fabric.gateway.Wallet;
import org.hyperledger.fabric.gateway.Wallets;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric_ca.sdk.EnrollmentRequest;
import org.hyperledger.fabric_ca.sdk.exception.EnrollmentException;
import org.hyperledger.fabric_ca.sdk.exception.InvalidArgumentException;

public class Admin {

    private final static String ADMIN_ID = "admin";

    private final HyperLedgerConnector hyperLedgerConnector;

    public Admin() throws InvalidArgumentException, CertificateException, EnrollmentException, IOException {
        hyperLedgerConnector = new HyperLedgerConnector();
        enrollWithCertificateAuthority();
    }

    public void createAvailableBallotsOnLedger() {
        hyperLedgerConnector.createAvailableBallotsOnLedger(ADMIN_ID, applicationProperties.getAdminContractName());
    }

    public void getTotals() {
        hyperLedgerConnector.getTotalsFromLedger(ADMIN_ID, applicationProperties.getAdminContractName());
    }

    public void enrollWithCertificateAuthority()
        throws IOException, EnrollmentException, InvalidArgumentException, CertificateException {

        // Create a wallet for managing identities
        Wallet wallet = Wallets.newFileSystemWallet(Paths.get("wallet"));

        // Check to see if we've already enrolled the admin user.
        if (wallet.get(ADMIN_ID) != null) {
            System.out.println("An identity for the admin user \"admin\" already exists in the wallet");
            return;
        }

        // Enroll the admin user, and import the new identity into the wallet.
        final EnrollmentRequest enrollmentRequestTLS = new EnrollmentRequest();
        enrollmentRequestTLS.addHost("localhost");
        enrollmentRequestTLS.setProfile("tls");

        String adminSecret = applicationProperties.getAdminSecret();
        Enrollment enrollment = caClient.enroll(ADMIN_ID, adminSecret, enrollmentRequestTLS);
        Identity user = Identities.newX509Identity("Org1MSP", enrollment);
        wallet.put(ADMIN_ID, user);
        System.out.println("Successfully enrolled user \"admin\" and imported it into the wallet");
    }
}
