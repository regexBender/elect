package com.aleclandow.vote.ledger;

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

//    private getContract(String contractName) {
//        try (Gateway gateway = connect()) {
//
//            // get the network and contract
//            Network network = gateway.getNetwork(NETWORK_NAME);
//            Contract contract = network.getContract(ADMIN_CONTRACT_NAME);
//        } catch (Exception e){
//            System.err.print(ConsoleColors.RED);
//            System.err.println(e.getMessage());
//            System.err.println(Arrays.toString(e.getStackTrace()));
//        }
//    }
    private Gateway connect(String voterId) throws Exception{
        // Load a file system based wallet for managing identities.
        Path walletPath = Paths.get("wallet");
        Wallet wallet = Wallets.newFileSystemWallet(walletPath);
        // load a CCP
        Path networkConfigPath = Paths.get("..", "..", "test-network", "organizations", "peerOrganizations", "org1.example.com", "connection-org1.yaml");

        Gateway.Builder builder = Gateway.createBuilder();
        builder.identity(wallet, voterId)
               .networkConfig(networkConfigPath)
               .discovery(true);
        return builder.connect();
    }
}
