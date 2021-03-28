package com.aleclandow.vote.ledger;

import static com.aleclandow.util.ApplicationProperties.APPLICATION_PROPERTIES;
import static com.aleclandow.vote.ledger.Transaction.GET_BALLOT;
import static com.aleclandow.vote.ledger.Transaction.INIT_BALLOT;


import com.aleclandow.util.ConsoleColors;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.Gateway;
import org.hyperledger.fabric.gateway.Network;
import org.hyperledger.fabric.gateway.Wallet;
import org.hyperledger.fabric.gateway.Wallets;

public class HyperLedgerConnector {

    public void createAvailableBallotsOnLedger(Contract contract) {
        System.out.println("Submit Transaction: InitLedger creates the available ballot(s) on the ledger.");
        try {
            contract.submitTransaction(INIT_BALLOT.name());

        } catch (Exception e) {
            System.err.print(ConsoleColors.RED);
            System.err.println(e.getMessage());
            System.err.println(Arrays.toString(e.getStackTrace()));
        }
    }

    public void getTotalsFromLedger(Contract adminContract) {
        byte[] result;
        try {
            result = adminContract.evaluateTransaction(GET_BALLOT.name());
            System.out.println("Evaluate Transaction: getBallot, result: " + new String(result));

        } catch (Exception e) {
            System.err.print(ConsoleColors.RED);
            System.err.println(e.getMessage());
            System.err.println(Arrays.toString(e.getStackTrace()));
        }

    }

    public Contract getContract(String voterId, String contractName) {
        Contract contract = null;
        try (Gateway gateway = connect(voterId)) {

            // get the network and contract
            Network network = gateway.getNetwork(APPLICATION_PROPERTIES.getNetworkName());
            contract = network.getContract(contractName);
        } catch (Exception e){
            System.err.print(ConsoleColors.RED);
            System.err.println(e.getMessage());
            System.err.println(Arrays.toString(e.getStackTrace()));
        }

        return contract;
    }

    private Gateway connect(String voterId) throws Exception{
        // Load a file system based wallet for managing identities.
        Path walletPath = Paths.get("wallet");
        Wallet wallet = Wallets.newFileSystemWallet(walletPath);
        // load a CCP
        Path networkConfigPath = Paths.get(APPLICATION_PROPERTIES.getNetworkConfigName());

        Gateway.Builder builder = Gateway.createBuilder();
        builder.identity(wallet, voterId)
               .networkConfig(networkConfigPath)
               .discovery(true);
        return builder.connect();
    }
}
