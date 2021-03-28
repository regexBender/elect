package com.aleclandow.vote;


import com.aleclandow.util.ApplicationProperties;
import com.aleclandow.util.ConsoleColors;
import com.aleclandow.vote.admin.Action;
import com.aleclandow.vote.admin.Admin;
import com.aleclandow.vote.ledger.CertificateAuthority;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.security.cert.CertificateException;
import java.util.Scanner;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric_ca.sdk.exception.EnrollmentException;
import org.hyperledger.fabric_ca.sdk.exception.InvalidArgumentException;

public class VotingApp {

    static {
        System.setProperty("org.hyperledger.fabric.sdk.service_discovery.as_localhost", "true");
    }

    public static void main(String[] args)
        throws IOException, CertificateException, EnrollmentException, InvalidArgumentException, NoSuchMethodException,
        org.hyperledger.fabric.sdk.exception.InvalidArgumentException, InstantiationException, CryptoException,
        IllegalAccessException, InvocationTargetException, ClassNotFoundException, URISyntaxException {

        System.out.print(ConsoleColors.CYAN);
        System.out.println("* Welcome to the Secure Voting Program using a Permissioned BlockChain *");
        System.out.println("- Using IBM's HyperLedger Fabric");
        System.out.println("Author: Alec Landow");
        System.out.println("For: CSC 724 at NCSU, Spring 2021");
        System.out.print(ConsoleColors.RESET);

        String voterId;
        Scanner input = new Scanner(System.in);

        ApplicationProperties.loadProperties();
        CertificateAuthority.initCaClient();

        while (true) {
            System.out.println(ConsoleColors.BLUE);
            System.out.println("Options: " + Mode.ALL_MODES);
            String mode = input.nextLine();

            if (mode.matches("(?i)" + Mode.ADMIN)) {
                System.out.println("Admin Actions: " + Action.ALL_ACTIONS);
                String action = input.nextLine();
                Admin admin = new Admin();

                if (action.matches("(?i)" + Action.CREATE_BALLOTS)) {
                    admin.createAvailableBallotsOnLedger();
                } else if (action.matches("(?i)" + Action.GET_TOTALS)) {
                    admin.getTotals();
                }
            } else if (mode.matches("(?i)" + Mode.REGISTER)) {

            } else if (mode.matches("(?i)" + Mode.VOTE)) {

            } else if (mode.matches("(?i)" + Mode.EXIT)) {
                System.out.println("Exiting");
                System.exit(0);
            } else {
                System.out.printf("Invalid option: %s%n", mode);
            }
        }


//        System.out.println("To register, please enter your voter ID and password:");
//        voterId = input.nextLine();



    }
}
