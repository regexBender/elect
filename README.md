# csc724-adv-distributed-systems

1. [Start the Network](#start-the-network)
2. [Add a Peer](#add-a-peer)
3. [Run the App](#run-the-app)

## Start the Network
### To start the network with two peers belonging to two separate organizations (Org1 and Org2):

1. Navigate to the root directory of this repository.
2. Navigate to the `test-network` directory:
```bash
cd test-network/
```
2. Initialize the network with a channel called `test-ballot-channel`, and use Hyperledger's built-in certificate authority:
```bash
./network.sh up createChannel -c test-ballot-channel -ca
```
3. Deploy the chaincode I wrote to each of the two nodes now in the network:
```bash
./network.sh deployCC -ccn basic-ballot -ccp ../chaincode-java-gradle/ -ccl java
```

## Add a Peer
### To add a peer belonging to a new organization (Org3)
1. Navigate to the root directory of this repository.
2. Navigate to the `addOrg3` directory:
```bash
cd test-network/addOrg3/
```
3. Add Org3 and its peer to the channel `test-ballot-channel`:
```bash
./addOrg3.sh up -c test-ballot-channel
```
4. Navigate to the root directory of this repository.
5. Masquerade as the Org3 admin:
```bash
export PATH=${PWD}/../bin:$PATH
export FABRIC_CFG_PATH=$PWD/../config/
export CORE_PEER_TLS_ENABLED=true
export CORE_PEER_LOCALMSPID="Org3MSP"
export CORE_PEER_TLS_ROOTCERT_FILE=${PWD}/organizations/peerOrganizations/org3.example.com/peers/peer0.org3.example.com/tls/ca.crt
export CORE_PEER_MSPCONFIGPATH=${PWD}/organizations/peerOrganizations/org3.example.com/users/Admin@org3.example.com/msp
export CORE_PEER_ADDRESS=localhost:11051
```
6. Package the chaincode I wrote:
```
peer lifecycle chaincode package basic-ballot.tar.gz --path <FULL_PATH_TO_THIS_REPO>/chaincode-java-gradle//build/install/basic-ballot --lang java --label basic-ballot_1.0
```
For example, my `<FULL_PATH_TO_THIS_REPO> = /Users/alandow/csc724-adv-distributed-systems`
7. Install the packaged chaincode on the Org3 peer:
```
peer lifecycle chaincode install basic-ballot.tar.gz
```
8. Copy the chaincode package id from the output. It will start with `basic-ballot_1.0`. If you cannot find it, print it by:
```bash
peer lifecycle chaincode queryinstalled
```
9. Save the chaincode package id to the `CC_PACKAGE_ID` environment variable:
```bash
export CC_PACKAGE_ID=<chaincode package id>
```
10. Approve the chaincode on the Org3 peer:
```bash
peer lifecycle chaincode approveformyorg -o localhost:7050 --ordererTLSHostnameOverride orderer.example.com --tls --cafile <FULL_PATH_TO_THIS_REPO>/test-network/organizations/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem --channelID test-ballot-channel --name basic-ballot --version 1.0 --package-id $CC_PACKAGE_ID --sequence 1
```

For example, my `<FULL_PATH_TO_THIS_REPO> = /Users/alandow/csc724-adv-distributed-systems`

## Run the App
1. Navigate to the root directory of this repository.
2. Navigate to the `voting-app` directory:
```bash
cd voting-app/
```
3. Build with maven:
```
mvn clean install
```
4. Run the app:
```
java -cp target/voting-app-1.0-SNAPSHOT.jar com.aleclandow.vote.VotingApp
```
IMPORTANT: Ensure the `/wallet` directory is deleted before running the app. Otherwise meaningless credentials will be used and the app will not work.

The app can also be run from `src/main/java/com/aleclandow/vote/VotingApp.java` in IntelliJ.
