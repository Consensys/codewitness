# Code Analysis
This repo started out as a set of analyses for various approaches to code
merklization. This repo is being morphed into an EVM byte code analysis tool set.

Directories:

* bytecodedump: Used for printing out the bytecode of a contract.
* combinedanalysis: Uses the output of the initial chunking analysis to determine witness sizes for blocks.
* common: Simple and AuxData anlysis plus some common files.
* data: Contains test contracts and associated data and scripts to generate test data. This is not used directly by any of the other modules.
* deploymentAddress: Create a file that maps deployment address to contract id (where contract id is the offset into the contract data file).
* evm: Opcode processing.
* functionid: Split the code based on the function selector in a transaction.
* gradle: build system related.
* main: Runner for strict, fixed, jumpdest, and function id analyses.
* tracereader: Reads blocks of transactions from trace files.
* traces: Direcrtory containing trace files.
* visualisation: Creates a visual representation of the where functions are in contracts.


# How to create witness data
* Get the contract data set and name it contract_data.json and put it in this directory.
* Run WitnessCodeAnalysis.java (in the main module), ensuring the code has all of the types of analyses on (booleans set to true) and the analyseAll function is uncommented in the code. This will take some hours to run.
* Run DeploymentAddress.java in the deploymentAddress module.
* Copy traces you want to analyse to the ./traces directory.
* Run CombinedAnalysis.java in the combinedanalysis module.