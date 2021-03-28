package com.aleclandow.vote.ledger;

import java.security.PrivateKey;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.hyperledger.fabric.sdk.Enrollment;

@Getter
@RequiredArgsConstructor
public class UserEnrollment implements Enrollment {

    private final PrivateKey key;

    private final String cert;

}
