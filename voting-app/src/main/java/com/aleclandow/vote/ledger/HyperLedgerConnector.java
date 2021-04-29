package com.aleclandow.vote.ledger;

import static com.aleclandow.util.ApplicationProperties.applicationProperties;
import static com.aleclandow.util.ConsoleColors.MAGENTA;
import static com.aleclandow.util.ConsoleColors.RESET;
import static com.aleclandow.vote.ledger.Transaction.CAST_ONE_VOTE_FOR_CANDIDATE;
import static com.aleclandow.vote.ledger.Transaction.GET_BALLOT;
import static com.aleclandow.vote.ledger.Transaction.INIT_BALLOT;
import static com.aleclandow.vote.ledger.Transaction.REGISTER_VOTER;


import com.aleclandow.util.ConsoleColors;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.Date;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.ContractException;
import org.hyperledger.fabric.gateway.Gateway;
import org.hyperledger.fabric.gateway.Network;
import org.hyperledger.fabric.gateway.Wallet;
import org.hyperledger.fabric.gateway.Wallets;

public class HyperLedgerConnector {

    public void registerVoterOnLedger(String voterId, String contractName) {
        transactWithConsumer(voterId, contractName, this::registerVoterOnLedgerTransaction);
    }

    public void createAvailableBallotsOnLedger(String voterId, Duration durationOfOpenPolls, String contractName) {
        transactWithBiConsumer(voterId, contractName, this::createAvailableBallotsOnLedgerTransaction, durationOfOpenPolls);
    }

    public void getTotalsFromLedger(String voterId, String contractName) {
        transactWithConsumer(voterId, contractName, this::getTotalsFromLedgerTransaction);
    }

    public void getBallotFromLedger(String voterId, String contractName) {
        transactWithConsumer(voterId, contractName, this::getBallotFromLedgerTransaction);
    }

    public void castVote(String voterId, String candidateId, String contractName) {
        transactWithBiConsumer(voterId, contractName, this::voteTransaction, candidateId);
    }

    private void registerVoterOnLedgerTransaction(Contract contract) {
        try {
            System.out.println("Submit Transaction: Register voter on the ledger.");

            contract.submitTransaction(REGISTER_VOTER.toString());
        } catch (Exception e) {
            System.err.print(ConsoleColors.RED);
            System.err.println(e.getMessage());
            System.err.println(Arrays.toString(e.getStackTrace()));
        }

    }

    private void voteTransaction(Contract contract, String candidateId) {
        try {
            System.out.printf("Submit Transaction: Vote for %s.%n", candidateId);

            Long startTime = (new Date()).getTime();
            contract.submitTransaction(CAST_ONE_VOTE_FOR_CANDIDATE.toString(), candidateId);
            Long endTime = (new Date()).getTime();
            System.out.printf("Time to write one vote to the ledger: %d ms%n", endTime - startTime);

            System.out.println("You have successfully cast your vote.");
        } catch (Exception e) {
            System.err.print(ConsoleColors.RED);
            System.err.println(e.getMessage());
            System.err.println(Arrays.toString(e.getStackTrace()));
        }

    }

    private void createAvailableBallotsOnLedgerTransaction(Contract contract, Duration durationOfOpenPolls) {
        try {
            System.out.println("Submit Transaction: InitLedger creates the available ballot(s) on the ledger.");

            Date now = new Date();
            String nowInMillis = Long.toString(now.getTime());

            String endInMillis = Long.toString(now.getTime() + durationOfOpenPolls.toMillis());

            Long startTime = (new Date()).getTime();
            contract.submitTransaction(INIT_BALLOT.toString(), nowInMillis, endInMillis);
            Long endTime = (new Date()).getTime();
            System.out.printf("Time to write the ballot and start/stop times to the ledger: %d ms%n", endTime - startTime);
        } catch (Exception e) {
            System.err.print(ConsoleColors.RED);
            System.err.println(e.getMessage());
            System.err.println(Arrays.toString(e.getStackTrace()));
        }

    }

    private void getTotalsFromLedgerTransaction(Contract adminContract) {
        try {
            Long startTime = (new Date()).getTime();
            byte[] result = adminContract.evaluateTransaction(GET_BALLOT.toString());
            Long endTime = (new Date()).getTime();
            System.out.printf("Time to get the world state totals: %d ms%n", endTime - startTime);

            String formattedResult = convertToFormattedJson(result);
            System.out.println("Evaluate Transaction: getBallot, result: " + MAGENTA + formattedResult);
        } catch (ContractException ce) {
            System.err.print(ConsoleColors.RED);
            System.err.println(ce.getMessage());
            System.err.println(Arrays.toString(ce.getStackTrace()));
        }
    }

    private void getBallotFromLedgerTransaction(Contract adminContract) {
        try {
            byte[] result = adminContract.evaluateTransaction(GET_BALLOT.toString());
            String formattedResult = convertToFormattedJson(result);
            System.out.println("Evaluate Transaction: getBallot, result: " + MAGENTA + formattedResult);
        } catch (ContractException ce) {
            System.err.print(ConsoleColors.RED);
            System.err.println(ce.getMessage());
            System.err.println(Arrays.toString(ce.getStackTrace()));
        }
    }


    private <T> void transactWithBiConsumer(
        String voterId,
        String contractName,
        BiConsumer<Contract, T > transaction,
        T arg1
    ) {
        try (Gateway gateway = connect(voterId)) {

            // get the network and contract
            Network network = gateway.getNetwork(applicationProperties.getNetworkName());
            Contract contract = network.getContract(contractName);

            transaction.accept(contract, arg1);

        } catch (Exception e) {
            System.err.print(ConsoleColors.RED);
            System.err.println(e.getMessage());
            System.err.println(Arrays.toString(e.getStackTrace()));
        }
    }

    private void transactWithConsumer(String voterId, String contractName, Consumer<Contract> transaction) {
        Long startTime = (new Date()).getTime();
        try (Gateway gateway = connect(voterId)) {
            Long endTime = (new Date()).getTime();
            System.out.printf("Time to establish a gateway to the network: %d ms%n", endTime - startTime);
            // get the network and contract
            Network network = gateway.getNetwork(applicationProperties.getNetworkName());
            Contract contract = network.getContract(contractName);

            transaction.accept(contract);

        } catch (Exception e) {
            System.err.print(ConsoleColors.RED);
            System.err.println(e.getMessage());
            System.err.println(Arrays.toString(e.getStackTrace()));
        }
    }

    private Gateway connect(String voterId) throws Exception {
        // Load a file system based wallet for managing identities.
        Path walletPath = Paths.get("wallet");
        Wallet wallet = Wallets.newFileSystemWallet(walletPath);

        // load a CCP (Connection Configuration Profile?)
        // From https://stackoverflow.com/questions/17351043/how-to-get-absolute-path-to-file-in-resources-folder-of-your-project
//        URL res = CertificateAuthority.class.getClassLoader().getResource(applicationProperties.getNetworkConfigName());
//        File file = Paths.get(res.toURI()).toFile();
//        String networkConfigPathString = file.getAbsolutePath();

//        Path networkConfigPath = Paths.get(networkConfigPathString);
        Path networkConfigPath = Paths.get(applicationProperties.getNetworkConfigRelativePath());

        Gateway.Builder builder = Gateway.createBuilder();
        builder.identity(wallet, voterId)
               .networkConfig(networkConfigPath)
               .discovery(true);
        return builder.connect();
    }

    private String convertToFormattedJson(byte[] byteArray) {
        // https://coderwall.com/p/ab5qha/convet-json-string-to-pretty-print-java-gson
        JsonParser parser = new JsonParser();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        String resultString = new String(byteArray);
        JsonArray jsonArray = parser.parse(resultString).getAsJsonArray();
        String formattedResult = gson.toJson(jsonArray);

        return formattedResult;
    }
}
