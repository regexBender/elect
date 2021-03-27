package com.aleclandow.vote;

import com.aleclandow.util.ConsoleColors;
import java.util.Scanner;

public class VotingApp {

    public static void main(String[] args) {
        System.out.print(ConsoleColors.CYAN);
        System.out.println("* Welcome to the Secure Voting Program using a Permissioned BlockChain *");
        System.out.println("- Using IBM's HyperLedger Fabric");
        System.out.println("Author: Alec Landow");
        System.out.println("For: CSC 724 at NCSU, Spring 2021");
        System.out.print(ConsoleColors.RESET);

        String voterId;
        Scanner input = new Scanner(System.in);

        while(true) {
            System.out.println(ConsoleColors.BLUE);
            System.out.println("Options: " + Mode.ALL_MODES);
            String mode = input.nextLine();

            if (mode.matches("(?i)" + Mode.ADMIN)) {

            } else if (mode.matches("(?i)" + Mode.REGISTER)) {

            } else if (mode.matches("(?i)" + Mode.VOTE)) {

            } else if (mode.matches("(?i)" + Mode.EXIT)) {
                System.out.println("Exiting");
            } else {
                System.out.printf("Invalid option: %s%n", mode);
            }
        }


//        System.out.println("To register, please enter your voter ID and password:");
//        voterId = input.nextLine();



    }
}
