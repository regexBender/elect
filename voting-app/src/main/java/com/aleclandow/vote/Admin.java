package com.aleclandow.vote;

import static com.aleclandow.util.ApplicationProperties.APPLICATION_PROPERTIES;


import com.aleclandow.vote.ledger.HyperLedgerConnector;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.hyperledger.fabric.gateway.Contract;

public class Admin {
    @AllArgsConstructor
    public enum Action {
        CREATE_BALLOTS("create-ballots"),
        GET_TOTALS("get-totals");

        private final String action;

        public static String ALL_ACTIONS = String.join(" | ", getAllActionsList());

        public String toString() {
            return action;
        }

        private static List<String> getAllActionsList() {
            return Arrays.stream(Action.values()).map(Action::toString).collect(Collectors.toList());
        }
    }

    private final static String ADMIN_ID = "admin";

    private final HyperLedgerConnector hyperLedgerConnector;
    private final Contract adminContract;

    public Admin() {
        hyperLedgerConnector = new HyperLedgerConnector();
        adminContract = getAdminContract();
    }

    public void createAvailableBallotsOnLedger() {
        hyperLedgerConnector.createAvailableBallotsOnLedger(adminContract);
    }

    public void getTotals() {
        hyperLedgerConnector.getTotalsFromLedger(adminContract);
    }

    private Contract getAdminContract() {
        return hyperLedgerConnector.getContract(ADMIN_ID, APPLICATION_PROPERTIES.getAdminContractName());
    }
}
