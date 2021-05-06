#!/bin/bash

# Add org3 and its peer
cd addOrg3/
./addOrg3.sh up -c test-ballot-channel
cd ../

export PATH=${PWD}/../bin:$PATH
export FABRIC_CFG_PATH=$PWD/../config/
export CORE_PEER_TLS_ENABLED=true
export CORE_PEER_LOCALMSPID="Org3MSP"
export CORE_PEER_TLS_ROOTCERT_FILE=${PWD}/organizations/peerOrganizations/org3.example.com/peers/peer0.org3.example.com/tls/ca.crt
export CORE_PEER_MSPCONFIGPATH=${PWD}/organizations/peerOrganizations/org3.example.com/users/Admin@org3.example.com/msp
export CORE_PEER_ADDRESS=localhost:11051

# Install the chaincode
peer lifecycle chaincode package basic-ballot.tar.gz --path ../chaincode-java-gradle/ --lang java --label basic-ballot_1.0
peer lifecycle chaincode install basic-ballot.tar.gz

ID_INFO=$(peer lifecycle chaincode queryinstalled)

# https://stackoverflow.com/questions/1891797/capturing-groups-from-a-grep-regex
regex="Package ID: (.*),"

if [[ $ID_INFO =~ $regex ]]
then
  ID="${BASH_REMATCH[1]}"
  export CC_PACKAGE_ID=$ID
else
  echo "No Match IN $ID_INFO"
fi

peer lifecycle chaincode approveformyorg -o localhost:7050 --ordererTLSHostnameOverride orderer.example.com --tls --cafile "${PWD}/organizations/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem" --channelID test-ballot-channel --name basic-ballot --version 1.0 --package-id $CC_PACKAGE_ID --sequence 1
