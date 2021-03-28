package com.aleclandow.vote.ledger;

import static com.aleclandow.util.ApplicationProperties.applicationProperties;
import static com.aleclandow.vote.ledger.Transaction.GET_BALLOT;
import static com.aleclandow.vote.ledger.Transaction.INIT_BALLOT;


import com.aleclandow.util.ConsoleColors;
import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.ContractException;
import org.hyperledger.fabric.gateway.Gateway;
import org.hyperledger.fabric.gateway.Network;
import org.hyperledger.fabric.gateway.Wallet;
import org.hyperledger.fabric.gateway.Wallets;

public class HyperLedgerConnector {

    public void createAvailableBallotsOnLedger(String voterId, String contractName) {
//        transact(voterId, contractName, this::createAvailableBallotsOnLedgerTransaction);
    }

    public void getTotalsFromLedger(String voterId, String contractName) {
//        transact(voterId, contractName, this::getTotalsFromLedgerTransaction);

        try (Gateway gateway = connect(voterId)) {

            // get the network and contract
            Network network = gateway.getNetwork(applicationProperties.getNetworkName());
            Contract contract = network.getContract(contractName);

            getTotalsFromLedgerTransaction(contract);

        } catch (Exception e) {
            System.err.print(ConsoleColors.RED);
            System.err.println(e.getMessage());
            System.err.println(Arrays.toString(e.getStackTrace()));
        }

    }

    private void createAvailableBallotsOnLedgerTransaction(Contract contract)
        throws InterruptedException, TimeoutException, ContractException {
        System.out.println("Submit Transaction: InitLedger creates the available ballot(s) on the ledger.");

        contract.submitTransaction(INIT_BALLOT.toString());

    }

    private void getTotalsFromLedgerTransaction(Contract adminContract) throws ContractException {
        byte[] result = adminContract.evaluateTransaction(GET_BALLOT.toString());
        System.out.println("Evaluate Transaction: getBallot, result: " + new String(result));
    }

    private void transact(String voterId, String contractName, Consumer<Contract> transaction) {
        try (Gateway gateway = connect(voterId)) {

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

        // load a CCP
        // From https://stackoverflow.com/questions/17351043/how-to-get-absolute-path-to-file-in-resources-folder-of-your-project
        URL res = CertificateAuthority.class.getClassLoader().getResource(applicationProperties.getNetworkConfigName());
        File file = Paths.get(res.toURI()).toFile();
        String networkConfigPathString = file.getAbsolutePath();

        Path networkConfigPath = Paths.get(networkConfigPathString);


        Gateway.Builder builder = Gateway.createBuilder();
        builder.identity(wallet, voterId)
               .networkConfig(networkConfigPath)
               .discovery(true);
        return builder.connect();
    }
}
