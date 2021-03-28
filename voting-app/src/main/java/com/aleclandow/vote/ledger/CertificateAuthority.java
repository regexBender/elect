package com.aleclandow.vote.ledger;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.Properties;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric.sdk.security.CryptoSuiteFactory;
import org.hyperledger.fabric_ca.sdk.HFCAClient;

public class CertificateAuthority {

    public static HFCAClient caClient = null;

    public static HFCAClient getCaClient()
        throws MalformedURLException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException,
        InstantiationException, IllegalAccessException, CryptoException, InvalidArgumentException {

        if (caClient != null) {
            return caClient;
        }

        Properties props = new Properties();
        props.put("pemFile", "ca.org1.example.com-cert.pem");
        props.put("allowAllHostNames", "true");
        caClient = HFCAClient.createNewInstance("https://localhost:7054", props);
        CryptoSuite cryptoSuite = CryptoSuiteFactory.getDefault().getCryptoSuite();
        caClient.setCryptoSuite(cryptoSuite);

        return caClient;
    }

}
