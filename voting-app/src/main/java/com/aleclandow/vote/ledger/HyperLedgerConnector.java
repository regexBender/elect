package com.aleclandow.vote.ledger;

import static com.aleclandow.util.ApplicationProperties.applicationProperties;
import static com.aleclandow.vote.ledger.Transaction.CAST_ONE_VOTE_FOR_CANDIDATE;
import static com.aleclandow.vote.ledger.Transaction.GET_BALLOT;
import static com.aleclandow.vote.ledger.Transaction.INIT_BALLOT;


import com.aleclandow.util.ConsoleColors;
import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import org.hyperledger.fabric.protos.common.Common;
import org.hyperledger.fabric.protos.discovery.DiscoveryGrpc;
import org.hyperledger.fabric.sdk.BlockInfo;
import org.hyperledger.fabric.sdk.Channel;

public class HyperLedgerConnector {

    public void createAvailableBallotsOnLedger(String voterId, String contractName) {
        transactWithConsumer(voterId, contractName, this::createAvailableBallotsOnLedgerTransaction);
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

    private void createAvailableBallotsOnLedgerTransaction(Contract contract) {
        try {
            System.out.println("Submit Transaction: InitLedger creates the available ballot(s) on the ledger.");

            contract.submitTransaction(INIT_BALLOT.toString());
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

            System.out.println("Evaluate Transaction: getBallot, result: " + new String(result));
        } catch (ContractException ce) {
            System.err.print(ConsoleColors.RED);
            System.err.println(ce.getMessage());
            System.err.println(Arrays.toString(ce.getStackTrace()));
        }
    }

    private void getBallotFromLedgerTransaction(Contract adminContract) {
        try {
            byte[] result = adminContract.evaluateTransaction(GET_BALLOT.toString());
            System.out.println("Evaluate Transaction: getBallot, result: " + new String(result));
        } catch (ContractException ce) {
            System.err.print(ConsoleColors.RED);
            System.err.println(ce.getMessage());
            System.err.println(Arrays.toString(ce.getStackTrace()));
        }
    }


    private void transactWithBiConsumer(String voterId, String contractName, BiConsumer<Contract, String> transaction, String arg1) {
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
}
