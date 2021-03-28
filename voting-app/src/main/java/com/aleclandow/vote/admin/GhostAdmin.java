package com.aleclandow.vote.admin;

import static com.aleclandow.vote.admin.Admin.ADMIN_ID;
import static com.aleclandow.vote.ledger.CertificateAuthority.caClient;


import com.aleclandow.vote.ledger.UserEnrollment;
import java.security.PrivateKey;
import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.hyperledger.fabric.gateway.Identities;
import org.hyperledger.fabric.gateway.X509Identity;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric_ca.sdk.RegistrationRequest;
import org.hyperledger.fabric_ca.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric_ca.sdk.exception.RegistrationException;

@Getter
@RequiredArgsConstructor
public class GhostAdmin implements User {
    private final String name;
    private final String affiliation;
    private final UserEnrollment enrollment;
    private final String mspId;

    public GhostAdmin(String name, String affiliation, String mspId, X509Identity identity) {
        this.name = name;
        this.affiliation = affiliation;
        this.mspId = mspId;
        this.enrollment = new UserEnrollment(identity.getPrivateKey(), Identities.toPemString(identity.getCertificate()));
    }

    @Override
    public Set<String> getRoles() {
        return null;
    }

    @Override
    public String getAccount() {
        return null;
    }

    public String registerVoter(RegistrationRequest registrationRequest)
        throws RegistrationException, InvalidArgumentException {
        // The Ghost Admin is a temporary workaround until a proper implementation of an actual admin enrolls the user is built
        String enrollmentSecret = caClient.register(registrationRequest, this);

        return enrollmentSecret;
    }

}
