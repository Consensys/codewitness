#!/usr/bin/env bash
rm -rf build

BUILDDIR=./build
BUILDDIRO=./build/optimize
BUILDDIRN=./build/normal
CONTRACTSDIR=contracts
PACKAGE=tech.pegasys.poc.witness1.soliditywrappers

# compiling one file also compiles its dependendencies. We use overwrite to avoid the related warnings.
solc $CONTRACTSDIR/Simple1.sol --allow-paths . --asm --asm-json --hashes --userdoc  --devdoc  --metadata --opcodes --bin --abi --optimize -o $BUILDDIRO --overwrite
solc $CONTRACTSDIR/Simple2.sol --allow-paths . --asm --asm-json --hashes --userdoc  --devdoc  --metadata --opcodes --bin --abi --optimize -o $BUILDDIRO --overwrite
solc $CONTRACTSDIR/Simple3.sol --allow-paths . --asm --asm-json --hashes --userdoc  --devdoc  --metadata --opcodes --bin --abi --optimize -o $BUILDDIRO --overwrite
solc $CONTRACTSDIR/Simple4.sol --allow-paths . --asm --asm-json --hashes --userdoc  --devdoc  --metadata --opcodes --bin --abi --optimize -o $BUILDDIRO --overwrite
solc $CONTRACTSDIR/Simple5.sol --allow-paths . --asm --asm-json --hashes --userdoc  --devdoc  --metadata --opcodes --bin --abi --optimize -o $BUILDDIRO --overwrite
solc $CONTRACTSDIR/ERC20.sol --allow-paths . --asm --asm-json --hashes --userdoc  --devdoc  --metadata --opcodes --bin --abi --optimize -o $BUILDDIRO --overwrite
solc $CONTRACTSDIR/Simple1.sol --allow-paths . --asm --asm-json --hashes --userdoc  --devdoc  --metadata --opcodes --bin --abi -o $BUILDDIRN --overwrite
solc $CONTRACTSDIR/Simple2.sol --allow-paths . --asm --asm-json --hashes --userdoc  --devdoc  --metadata --opcodes --bin --abi -o $BUILDDIRN --overwrite
solc $CONTRACTSDIR/Simple3.sol --allow-paths . --asm --asm-json --hashes --userdoc  --devdoc  --metadata --opcodes --bin --abi -o $BUILDDIRN --overwrite
solc $CONTRACTSDIR/Simple4.sol --allow-paths . --asm --asm-json --hashes --userdoc  --devdoc  --metadata --opcodes --bin --abi -o $BUILDDIRN --overwrite
solc $CONTRACTSDIR/Simple5.sol --allow-paths . --asm --asm-json --hashes --userdoc  --devdoc  --metadata --opcodes --bin --abi -o $BUILDDIRN --overwrite
solc $CONTRACTSDIR/ERC20.sol --allow-paths . --asm --asm-json --hashes --userdoc  --devdoc  --metadata --opcodes --bin --abi -o $BUILDDIRN --overwrite

# TODO try out:
# solc --ir <file> and solc --ir-optimized <file> to get the yul output. That then needs to be manually assembled via solc --strict-assembly <file>. 


ls -al $BUILDDIR/normal
ls -al $BUILDDIR/optimize

