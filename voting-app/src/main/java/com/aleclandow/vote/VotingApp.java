package com.aleclandow.vote;


import com.aleclandow.util.ApplicationProperties;
import com.aleclandow.util.ConsoleColors;
import com.aleclandow.vote.admin.AdminAction;
import com.aleclandow.vote.admin.Admin;
import com.aleclandow.vote.ledger.CertificateAuthority;
import com.aleclandow.vote.voter.Voter;
import com.aleclandow.vote.voter.VoterAction;
import java.util.Scanner;

public class VotingApp {

    static {
        System.setProperty("org.hyperledger.fabric.sdk.service_discovery.as_localhost", "true");
    }

    public static void main(String[] args) throws Exception {

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

        Voter voter = null;
        while (true) {
            System.out.print(ConsoleColors.BLUE);
            System.out.println("Options: " + Mode.ALL_MODES);
            String mode = input.nextLine();

            if (mode.matches("(?i)" + Mode.ADMIN)) {
                System.out.println("Admin Actions: " + AdminAction.ALL_ACTIONS);
                String action = input.nextLine().trim();
                Admin admin = new Admin();

                if (action.matches("(?i)" + AdminAction.CREATE_BALLOTS)) {
                    admin.createAvailableBallotsOnLedger();

                } else if (action.matches("(?i)" + AdminAction.GET_TOTALS)) {
                    admin.getTotals();

                }

            } else if (mode.matches("(?i)" + Mode.REGISTER)) {
                System.out.println("Please enter a unique voter id:");
                voterId = input.nextLine().trim();
                voter = new Voter(voterId);

            } else if (mode.matches("(?i)" + Mode.VOTE)) {
                if (voter == null) {
                    System.out.print(ConsoleColors.YELLOW);
                    System.out.println("Please register to vote first.");
                    continue;
                }

                System.out.println("Voter Actions: " + VoterAction.ALL_ACTIONS);
                String action = input.nextLine();

                if (action.matches("(?i)" + AdminAction.CREATE_BALLOTS)) {
//                    admin.createAvailableBallotsOnLedger();

                } else if (action.matches("(?i)" + VoterAction.GET_TOTALS)) {
                    voter.getTotals();

                }

            } else if (mode.matches("(?i)" + Mode.EXIT)) {
                System.out.println("Exiting");
                System.exit(0);
            } else {
                System.out.print(ConsoleColors.RED);
                System.out.printf("Invalid option: %s%n", mode);
            }
        }


//        System.out.println("To register, please enter your voter ID and password:");
//        voterId = input.nextLine();



    }
}
