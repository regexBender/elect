package com.aleclandow.vote.admin;

import static com.aleclandow.util.ApplicationProperties.APPLICATION_PROPERTIES;


import com.aleclandow.vote.ledger.HyperLedgerConnector;
import org.hyperledger.fabric.gateway.Contract;

public class Admin {

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
