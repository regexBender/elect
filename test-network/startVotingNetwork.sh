#!/bin/bash

./network.sh up createChannel -c test-ballot-channel -ca
./network.sh deployCC -ccn basic-ballot -ccp ../chaincode-java-gradle/ -ccl java