package com.aleclandow.vote;

import com.aleclandow.util.ConsoleColors;
import java.util.Scanner;

public class VotingApp {
    public static void main(String[] args) {
        System.out.print(ConsoleColors.BLUE);
        System.out.println("*** Welcome to Alec's Blockchain-Based Voting Program ***");
        System.out.print(ConsoleColors.RESET);

        String voterId;
        Scanner input = new Scanner(System.in);
        System.out.println(ConsoleColors.RED);
        System.out.println("To register, please enter your voter ID and password:");
        voterId = input.nextLine();



    }
}
