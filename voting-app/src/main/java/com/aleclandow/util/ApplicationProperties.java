package com.aleclandow.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;
import lombok.Getter;

/*
    From https://crunchify.com/java-properties-file-how-to-read-config-properties-values-in-java/
*/
@Getter
public class ApplicationProperties {

    private String networkName;
    private String networkConfigName;
    private String adminContractName;
    private String voterContractName;

    public void loadProperties() throws IOException {
        InputStream inputStream = null;
        try {
            Properties prop = new Properties();
            String propFileName = "application.properties";

            inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);

            if (inputStream != null) {
                prop.load(inputStream);
            } else {
                throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
            }

            networkName = prop.getProperty("network-name");
            networkConfigName = prop.getProperty("network-config-name");
            adminContractName = prop.getProperty("admin-contract-name");
            voterContractName = prop.getProperty("voter-contract-name");

        } catch (Exception e) {
            System.err.print(ConsoleColors.RED);
        } finally {
            inputStream.close();
        }
    }

}
