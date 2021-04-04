package com.aleclandow.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import lombok.AllArgsConstructor;
import lombok.Getter;

/*
    From https://crunchify.com/java-properties-file-how-to-read-config-properties-values-in-java/
*/
@Getter
@AllArgsConstructor
public class ApplicationProperties {

    public static ApplicationProperties applicationProperties = null; // singleton instance

    private final String networkName;
    private final String networkConfigName;
    private final String networkConfigRelativePath;
    private final String caCertificateRelativePath;
    private final String caClientUrl;
    private final String adminContractName;
    private final String voterContractName;

    private final String adminSecret;

    public static void loadProperties() throws IOException {
        if (applicationProperties != null) {
            return;
        }

        InputStream inputStream = null;
        try {
            Properties prop = new Properties();
            String propFileName = "application.properties";

            inputStream = ApplicationProperties.class.getClassLoader().getResourceAsStream(propFileName);

            if (inputStream != null) {
                prop.load(inputStream);
            } else {
                throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
            }

            String networkName = prop.getProperty("network-name");
            String networkConfigName = prop.getProperty("network-config-name");
            String networkConfigRelativePath = prop.getProperty("network.config.relative-path");
            String caCertificateRelativePath = prop.getProperty("ca.certificate.relative-path");
            String caClientUrl = prop.getProperty("ca.client.url");
            String adminContractName = prop.getProperty("admin-contract-name");
            String voterContractName = prop.getProperty("voter-contract-name");
            String adminSecret = prop.getProperty("admin-secret");

            applicationProperties = new ApplicationProperties(
                networkName,
                networkConfigName,
                networkConfigRelativePath,
                caCertificateRelativePath,
                caClientUrl,
                adminContractName,
                voterContractName,
                adminSecret
            );
        } catch (Exception e) {
            System.err.print(ConsoleColors.RED);
        } finally {
            inputStream.close();
        }
    }

}
