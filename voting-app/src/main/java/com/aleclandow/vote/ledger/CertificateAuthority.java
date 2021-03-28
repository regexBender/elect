package com.aleclandow.vote.ledger;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Properties;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric.sdk.security.CryptoSuiteFactory;
import org.hyperledger.fabric_ca.sdk.HFCAClient;

public class CertificateAuthority {

    public static HFCAClient caClient = null;

    public static HFCAClient initCaClient()
        throws MalformedURLException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException,
        InstantiationException, IllegalAccessException, CryptoException, InvalidArgumentException, URISyntaxException {

        if (caClient != null) {
            return caClient;
        }

        Properties props = new Properties();

        // From https://stackoverflow.com/questions/17351043/how-to-get-absolute-path-to-file-in-resources-folder-of-your-project
        URL res = CertificateAuthority.class.getClassLoader().getResource("ca.org1.example.com-cert.pem");
        File file = Paths.get(res.toURI()).toFile();
        String certPemAbsolutePath = file.getAbsolutePath();

        props.put("pemFile", certPemAbsolutePath);
        props.put("allowAllHostNames", "true");
        caClient = HFCAClient.createNewInstance("https://localhost:7054", props);
        CryptoSuite cryptoSuite = CryptoSuiteFactory.getDefault().getCryptoSuite();
        caClient.setCryptoSuite(cryptoSuite);

        return caClient;
    }

}
