package com.aleclandow.vote.ledger;

import static com.aleclandow.util.ApplicationProperties.applicationProperties;
import static com.aleclandow.util.ConsoleColors.BLUE;
import static com.aleclandow.util.ConsoleColors.MAGENTA;
import static com.aleclandow.util.ConsoleColors.YELLOW;
import static com.aleclandow.vote.ledger.Transaction.CAST_ONE_VOTE_FOR_CANDIDATE;
import static com.aleclandow.vote.ledger.Transaction.GET_BALLOT;
import static com.aleclandow.vote.ledger.Transaction.INIT_BALLOT;
import static com.aleclandow.vote.ledger.Transaction.REGISTER_VOTER;


import com.aleclandow.util.ConsoleColors;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.Gateway;
import org.hyperledger.fabric.gateway.Network;
import org.hyperledger.fabric.gateway.Wallet;
import org.hyperledger.fabric.gateway.Wallets;

public class HyperLedgerConnector {

    public void registerVoterOnLedger(String voterId, String contractName) {
        transactWithConsumer(voterId, contractName, this::registerVoterOnLedgerTransaction);
    }

    public void createAvailableBallotsOnLedger(String voterId, Duration durationOfOpenPolls, String contractName) {
        transactWithBiFunction(voterId,
                               contractName,
                               this::createAvailableBallotsOnLedgerTransaction,
                               durationOfOpenPolls);
    }

    public void getTotalsFromLedger(String voterId, String contractName) {
        transactWithConsumer(voterId, contractName, this::getTotalsFromLedgerTransaction);
    }

    public void getBallotFromLedger(String voterId, String contractName) {
        transactWithConsumer(voterId, contractName, this::getBallotFromLedgerTransaction);
    }

    public long castVote(String voterId, String candidateId, String contractName) {
        return transactWithBiFunction(voterId, contractName, this::voteTransaction, candidateId);
    }

    private void registerVoterOnLedgerTransaction(Contract contract) {
        System.out.println(BLUE + "Submit Transaction: Register voter on the ledger.");

        time("Time to write a registered voter's info on the ledger",
             () -> {
                 try {
                     contract.submitTransaction(REGISTER_VOTER.toString());
                 } catch (Exception e) {
                     System.err.print(ConsoleColors.RED);
                     System.err.println(e.getMessage());
                     System.err.println(Arrays.toString(e.getStackTrace()));
                 }
        });

    }

    private long voteTransaction(Contract contract, String candidateId) {
        System.out.printf(BLUE + "Submit Transaction: Vote for %s.%n", candidateId);

        return time("Time to write one vote to the ledger",
             () -> {
                 try {
                     contract.submitTransaction(CAST_ONE_VOTE_FOR_CANDIDATE.toString(), candidateId);
                     System.out.println(BLUE + "You have successfully cast your vote.");
                 } catch (Exception e) {
                     System.err.print(ConsoleColors.RED);
                     System.err.println(e.getMessage());
                     System.err.println(Arrays.toString(e.getStackTrace()));
                 }
             });

    }

    private long createAvailableBallotsOnLedgerTransaction(Contract contract, Duration durationOfOpenPolls) {
        System.out.println(BLUE + "Submit Transaction: InitLedger creates the available ballot(s) on the ledger.");

        Date now = new Date();
        String nowInMillis = Long.toString(now.getTime());
        String endInMillis = Long.toString(now.getTime() + durationOfOpenPolls.toMillis());

        return time("Time to write the ballot and start/stop times to the ledger",
             () -> {
                 try {
                     contract.submitTransaction(INIT_BALLOT.toString(), nowInMillis, endInMillis);
                 } catch (Exception e) {
                     System.err.print(ConsoleColors.RED);
                     System.err.println(e.getMessage());
                     System.err.println(Arrays.toString(e.getStackTrace()));
                 }
        });

    }

    private void getTotalsFromLedgerTransaction(Contract adminContract) {

        String resultString = time("Time to get the world state totals",
             () -> {
                 try {
                     byte[] result = adminContract.evaluateTransaction(GET_BALLOT.toString());
                     return new String(result);
                 } catch (Exception e) {
                     System.err.print(ConsoleColors.RED);
                     System.err.println(e.getMessage());
                     System.err.println(Arrays.toString(e.getStackTrace()));
                     return "Transaction unsuccessful";
                 }
             });
        String formattedResult = convertToFormattedJson(resultString);
        System.out.println(BLUE + "Evaluate Transaction: getBallot, result: " + MAGENTA + formattedResult);

    }

    private void getBallotFromLedgerTransaction(Contract adminContract) {
        getTotalsFromLedgerTransaction(adminContract);
    }

    private <T> long transactWithBiFunction(
        String voterId,
        String contractName,
        BiFunction<Contract, T, Long> transaction,
        T arg1
    ) {
        List<Long> singletonTimeList = new ArrayList<>();
        time("Time to establish a gateway to the network, and then close that connection",
             () -> {
                 Long startTime = (new Date()).getTime();
                 try (Gateway gateway = connect(voterId)) {
                     Long endTimeEstablishConnection = (new Date()).getTime();
                     System.out.printf(YELLOW + "Time to establish a gateway to the network: %d ms%n", endTimeEstablishConnection - startTime);

                     // get the network and contract
                     Network network = gateway.getNetwork(applicationProperties.getNetworkName());
                     Contract contract = network.getContract(contractName);

                     singletonTimeList.add(transaction.apply(contract, arg1));

                 } catch (Exception e) {
                     System.err.print(ConsoleColors.RED);
                     System.err.println(e.getMessage());
                     System.err.println(Arrays.toString(e.getStackTrace()));
                 }
        });

        return singletonTimeList.get(0);
    }

    private void transactWithConsumer(String voterId, String contractName, Consumer<Contract> transaction) {
        time("Time to establish a gateway to the network, and then close that connection",
             () -> {
                 Long startTime = (new Date()).getTime();
                 try (Gateway gateway = connect(voterId)) {
                     Long endTimeEstablishConnection = (new Date()).getTime();
                     System.out.printf(YELLOW + "Time to establish a gateway to the network: %d ms%n", endTimeEstablishConnection - startTime);
                     // get the network and contract
                     Network network = gateway.getNetwork(applicationProperties.getNetworkName());
                     Contract contract = network.getContract(contractName);

                     transaction.accept(contract);

                 } catch (Exception e) {
                     System.err.print(ConsoleColors.RED);
                     System.err.println(e.getMessage());
                     System.err.println(Arrays.toString(e.getStackTrace()));
                 }
        });

    }

    private Gateway connect(String voterId) throws Exception {
        // Load a file system based wallet for managing identities.
        Path walletPath = Paths.get("wallet");
        Wallet wallet = Wallets.newFileSystemWallet(walletPath);

        // load a CCP (Connection Configuration Profile?)
        Path networkConfigPath = Paths.get(applicationProperties.getNetworkConfigRelativePath());

        Gateway.Builder builder = Gateway.createBuilder();
        builder.identity(wallet, voterId)
               .networkConfig(networkConfigPath)
               .discovery(true);
        return builder.connect();
    }

    private String convertToFormattedJson(String resultString) {
        // https://coderwall.com/p/ab5qha/convet-json-string-to-pretty-print-java-gson
        JsonParser parser = new JsonParser();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        JsonArray jsonArray = parser.parse(resultString).getAsJsonArray();
        String formattedResult = gson.toJson(jsonArray);

        return formattedResult;
    }

    private long time(String message, Runnable transaction) {
        Long startTime = (new Date()).getTime();
        transaction.run();
        Long endTime = (new Date()).getTime();

        long timeInMillis = endTime - startTime;
        System.out.printf(YELLOW + message + ": %d ms%n", timeInMillis);

        return timeInMillis;
    }

    private <T> T time(String message, Supplier<T> transaction) {
        Long startTime = (new Date()).getTime();
        T result = transaction.get();
        Long endTime = (new Date()).getTime();
        System.out.printf(YELLOW + message + ": %d ms%n", endTime - startTime);

        return result;
    }
}
