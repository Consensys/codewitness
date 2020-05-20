/*
 * Copyright ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package tech.pegasys.poc.witnesscodeanalysis.vm;

import tech.pegasys.poc.witnesscodeanalysis.vm.operations.AddModOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.AddOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.AddressOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.AndOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.BalanceOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.BeginSubOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.BlockHashOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.ByteOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.CallCodeOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.CallDataCopyOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.CallDataLoadOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.CallDataSizeOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.CallOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.CallValueOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.CallerOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.ChainIdOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.CodeCopyOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.CodeSizeOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.CoinbaseOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.Create2Operation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.CreateOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.DelegateCallOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.DifficultyOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.DivOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.DupOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.EqOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.ExpOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.ExtCodeCopyOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.ExtCodeHashOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.ExtCodeSizeOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.GasLimitOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.GasOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.GasPriceOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.GtOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.InvalidOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.IsZeroOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.JumpDestOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.JumpOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.JumpSubOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.JumpiOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.LogOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.LtOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.MLoadOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.MSizeOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.MStore8Operation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.MStoreOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.ModOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.MulModOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.MulOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.NotOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.NumberOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.OrOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.OriginOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.PCOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.PopOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.PushOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.ReturnDataCopyOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.ReturnDataSizeOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.ReturnOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.ReturnSubOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.RevertOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.SDivOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.SGtOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.SLoadOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.SLtOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.SModOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.SStoreOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.SarOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.SelfBalanceOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.SelfDestructOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.Sha3Operation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.ShlOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.ShrOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.SignExtendOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.StaticCallOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.StopOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.SubOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.SwapOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.TimestampOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.XorOperation;

import java.math.BigInteger;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;

/** Provides EVMs supporting the appropriate operations for mainnet hard forks. */
public abstract class MainnetEvmRegistries {

//  static EVM frontier() {
//    final OperationRegistry registry = new OperationRegistry();
//
//    registerFrontierOpcodes(registry, 0);
//
//    return new EVM(registry);
//  }
//
//  static EVM homestead() {
//    final OperationRegistry registry = new OperationRegistry();
//
//    registerHomesteadOpcodes(registry, 0);
//
//    return new EVM(registry);
//  }
//
//  static EVM byzantium() {
//    final OperationRegistry registry = new OperationRegistry();
//
//    registerByzantiumOpcodes(registry, 0);
//
//    return new EVM(registry);
//  }
//
//  static EVM constantinople() {
//    final OperationRegistry registry = new OperationRegistry();
//
//    registerConstantinopleOpcodes(registry, 0);
//
//    return new EVM(registry);
//  }
//
//  static EVM istanbul(final BigInteger chainId) {
//    final OperationRegistry registry = new OperationRegistry();
//
//    registerIstanbulOpcodes(registry, 0, chainId);
//
//    return new EVM(registry);
//  }

  public static OperationRegistry berlin(final BigInteger chainId) {
    final OperationRegistry registry = new OperationRegistry();
    registerBerlinOpcodes(registry, 0, chainId);
    return registry;
  }

  private static void registerFrontierOpcodes(
      final OperationRegistry registry,
      final int accountVersion) {
    registry.put(new AddOperation(), accountVersion);
    registry.put(new AddOperation(), accountVersion);
    registry.put(new MulOperation(), accountVersion);
    registry.put(new SubOperation(), accountVersion);
    registry.put(new DivOperation(), accountVersion);
    registry.put(new SDivOperation(), accountVersion);
    registry.put(new ModOperation(), accountVersion);
    registry.put(new SModOperation(), accountVersion);
    registry.put(new ExpOperation(), accountVersion);
    registry.put(new AddModOperation(), accountVersion);
    registry.put(new MulModOperation(), accountVersion);
    registry.put(new SignExtendOperation(), accountVersion);
    registry.put(new LtOperation(), accountVersion);
    registry.put(new GtOperation(), accountVersion);
    registry.put(new SLtOperation(), accountVersion);
    registry.put(new SGtOperation(), accountVersion);
    registry.put(new EqOperation(), accountVersion);
    registry.put(new IsZeroOperation(), accountVersion);
    registry.put(new AndOperation(), accountVersion);
    registry.put(new OrOperation(), accountVersion);
    registry.put(new XorOperation(), accountVersion);
    registry.put(new NotOperation(), accountVersion);
    registry.put(new ByteOperation(), accountVersion);
    registry.put(new Sha3Operation(), accountVersion);
    registry.put(new AddressOperation(), accountVersion);
    registry.put(new BalanceOperation(), accountVersion);
    registry.put(new OriginOperation(), accountVersion);
    registry.put(new CallerOperation(), accountVersion);
    registry.put(new CallValueOperation(), accountVersion);
    registry.put(new CallDataLoadOperation(), accountVersion);
    registry.put(new CallDataSizeOperation(), accountVersion);
    registry.put(new CallDataCopyOperation(), accountVersion);
    registry.put(new CodeSizeOperation(), accountVersion);
    registry.put(new CodeCopyOperation(), accountVersion);
    registry.put(new GasPriceOperation(), accountVersion);
    registry.put(new ExtCodeCopyOperation(), accountVersion);
    registry.put(new ExtCodeSizeOperation(), accountVersion);
    registry.put(new BlockHashOperation(), accountVersion);
    registry.put(new CoinbaseOperation(), accountVersion);
    registry.put(new TimestampOperation(), accountVersion);
    registry.put(new NumberOperation(), accountVersion);
    registry.put(new DifficultyOperation(), accountVersion);
    registry.put(new GasLimitOperation(), accountVersion);
    registry.put(new PopOperation(), accountVersion);
    registry.put(new MLoadOperation(), accountVersion);
    registry.put(new MStoreOperation(), accountVersion);
    registry.put(new MStore8Operation(), accountVersion);
    registry.put(new SLoadOperation(), accountVersion);
    registry.put(
        new SStoreOperation(), accountVersion);
    registry.put(new JumpOperation(), accountVersion);
    registry.put(new JumpiOperation(), accountVersion);
    registry.put(new PCOperation(), accountVersion);
    registry.put(new MSizeOperation(), accountVersion);
    registry.put(new GasOperation(), accountVersion);
    registry.put(new JumpDestOperation(), accountVersion);
    registry.put(new ReturnOperation(), accountVersion);
    registry.put(new InvalidOperation(), accountVersion);
    registry.put(new StopOperation(), accountVersion);
    registry.put(new SelfDestructOperation(), accountVersion);
    registry.put(new CreateOperation(), accountVersion);
    registry.put(new CallOperation(), accountVersion);
    registry.put(new CallCodeOperation(), accountVersion);

    // Register the PUSH1, PUSH2, ..., PUSH32 operations.
    for (int i = 1; i <= 32; ++i) {
      registry.put(new PushOperation(i), accountVersion);
    }

    // Register the DUP1, DUP2, ..., DUP16 operations.
    for (int i = 1; i <= 16; ++i) {
      registry.put(new DupOperation(i), accountVersion);
    }

    // Register the SWAP1, SWAP2, ..., SWAP16 operations.
    for (int i = 1; i <= 16; ++i) {
      registry.put(new SwapOperation(i), accountVersion);
    }

    // Register the LOG0, LOG1, ..., LOG4 operations.
    for (int i = 0; i < 5; ++i) {
      registry.put(new LogOperation(i), accountVersion);
    }
  }

  private static void registerHomesteadOpcodes(
      final OperationRegistry registry,
      final int accountVersion) {
    registerFrontierOpcodes(registry, accountVersion);
    registry.put(new DelegateCallOperation(), accountVersion);
  }


  private static void registerByzantiumOpcodes(
      final OperationRegistry registry,
      final int accountVersion) {
    registerHomesteadOpcodes(registry, accountVersion);
    registry.put(new ReturnDataCopyOperation(), accountVersion);
    registry.put(new ReturnDataSizeOperation(), accountVersion);
    registry.put(new RevertOperation(), accountVersion);
    registry.put(new StaticCallOperation(), accountVersion);
  }

  private static void registerConstantinopleOpcodes(
      final OperationRegistry registry,
      final int accountVersion) {
    registerByzantiumOpcodes(registry, accountVersion);
    registry.put(new Create2Operation(), accountVersion);
    registry.put(new SarOperation(), accountVersion);
    registry.put(new ShlOperation(), accountVersion);
    registry.put(new ShrOperation(), accountVersion);
    registry.put(new ExtCodeHashOperation(), accountVersion);
  }

  private static void registerIstanbulOpcodes(
      final OperationRegistry registry,
      final int accountVersion,
      final BigInteger chainId) {
    registerConstantinopleOpcodes(registry, accountVersion);
    registry.put(
        new ChainIdOperation(Bytes32.leftPad(Bytes.of(chainId.toByteArray()))), 0);

    registry.put(new SelfBalanceOperation(), 0);
    registry.put(
        new SStoreOperation(), 0);
  }

  private static void registerBerlinOpcodes(
      final OperationRegistry registry,
      final int accountVersion,
      final BigInteger chainId) {
    registerIstanbulOpcodes(registry, accountVersion, chainId);
    registry.put(new BeginSubOperation(), accountVersion);
    registry.put(new JumpSubOperation(), accountVersion);
    registry.put(new ReturnSubOperation(), accountVersion);
  }
}
