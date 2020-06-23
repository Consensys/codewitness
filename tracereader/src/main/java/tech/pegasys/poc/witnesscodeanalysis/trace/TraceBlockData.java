package tech.pegasys.poc.witnesscodeanalysis.trace;

public class TraceBlockData {

  //{"jsonrpc":"2.0",
  //"result":[
  //  {"output":"0x","stateDiff":null,"trace":[
  //    {"action":
  //       {"callType":"call","from":"0xc6a8e24a8e30fd4bafc61f4a98238cedddf99a1f","gas":"0x9858","input":"0x","to":"0x3b921f0f543cfdb3fe9797911267e14382d8db7b","value":"0x1f0ef9247f530000"},
  //    "result":{"gasUsed":"0x0","output":"0x"},"subtraces":0,"traceAddress":[],"type":"call"}
  //   ],
  //  "transactionHash":"0x0cd608fee14619f52b6f27e7b89034b94d0449dd303bfb82584098f8cd2c4840","vmTrace":null
  // },{"output":"0x","stateDiff":null,"trace":[
  //  {"action":{"callType":"call","from":"0x902898971a1439b39d498cf71266c29bf7a79c43","gas":"0x5208","input":"0x","to":"0x1994283c93a99ee469b1b3001ac438debba81b55","value":"0xd061f262e570400"},
  //   "result":{"gasUsed":"0x0","output":"0x"},
  //  "subtraces":0,
  // "traceAddress":[],
  //"type":"call"}],
  //"transactionHash":"0x7990db661aaf964590f9237009b9b8a876f8456cb8088ca69e4c2f6d2ee14413","vmTrace":null},
  //
  //{"output":"0x","stateDiff":null,"trace":[
  //   {"action":{"callType":"call","from":"0xe7ddb3b3c960074ca4b14b4619dc8b7806d17594","gas":"0x9858","input":"0x","to":"0xd33d208ad9dc41608048cd3c572adaf2213967b9","value":"0x1049cf4fe77fc00"},
  //  "result":{"gasUsed":"0x0","output":"0x"},"subtraces":0,"traceAddress":[],"type":"call"}],
  // "transactionHash":"0xf1b8e38de4aeab3ba742080a55662f45ea9ffb0f5fee582ef401b1540d199b6d","vmTrace":null},
  //{"output":"0x0000000000000000000000000000000000000000000000000000000000000001","stateDiff":null,"trace":[
  //  {"action":{"callType":"call","from":"0xd45727e3d7405c6ab3b2b3a57474012e1f998483","gas":"0x738c8","input":"0x4ab0d1909b88af5cfde6d46a0420e5c5fd70a8490f4e3505a463088a6da7fab82a35462700000000000000000000000000000000000000000000000004a03ce68d21555500000000000000000000000079febf6b9f76853edbcbc913e6aae8232cfb9de96a9705b400000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000005d364818000000000000000000000000000000000000000000000000000000050e5d1f20","to":"0x4565300c576431e5228e8aa32642d5739cf9247d","value":"0x0"},"result":{"gasUsed":"0x1ece8","output":"0x0000000000000000000000000000000000000000000000000000000000000001"},"subtraces":1,"traceAddress":[],"type":"call"},{"action":{"callType":"call","from":"0x4565300c576431e5228e8aa32642d5739cf9247d","gas":"0x6e5b8","input":"0x6a9705b49b88af5cfde6d46a0420e5c5fd70a8490f4e3505a463088a6da7fab82a354627000000000000000000000000000000000000000000000000000000050e5d1f20","to":"0x79febf6b9f76853edbcbc913e6aae8232cfb9de9","value":"0x0"},"result":{"gasUsed":"0x1b57e","output":"0x"},"subtraces":0,"traceAddress":[0],"type":"call"}],"transactionHash":"0x757fe2bbdc29af8b32fed162ea82174ebd725bbee2fbee74655a07d329629d24","vmTrace":null},{"output":"0x","stateDiff":null,"trace":[{"action":{"callType":"call","from":"0x52bc44d5378309ee2abf1539bf71de1b7d7be3b5","gas":"0x7148","input":"0x","to":"0x3e1e48db08848cd3eedb4cf5b66955ba77ca37c4","value":"0x2cc7ee46a78de00"},"result":{"gasUsed":"0x0","output":"0x"},"subtraces":0,"traceAddress":[],"type":"call"}],"transactionHash":"0x30cfa021e4a31ad2e9c94192553d5e392406b7b37d5e4187db5dee8e3655e88e","vmTrace":null},{"output":"0x","stateDiff":null,"trace":[{"action":{"callType":"call","from":"0x3c0f52e7a3576eb376a3b935e7fb90db3bcc1e40","gas":"0x0","input":"0x","to":"0x23619d1f8ce00adba2f2c24f190aa9eb3bb24156","value":"0xdf29bd843379800"},"result":{"gasUsed":"0x0","output":"0x"},"subtraces":0,"traceAddress":[],"type":"call"}],"transactionHash":"0xa09d551568e144c8629ba2c01c2189bc033d00cf05a2d76796add2917271d72f","vmTrace":null},{"output":"0x","stateDiff":null,"trace":[{"action":{"callType":"call","from":"0x59a5208b32e627891c389ebafc644145224006e8","gas":"0xa410","input":"0x","to":"0xbce2062daec73ae6e3ac2ebf8b250ea5794a4eb0","value":"0x54392ef4616ec00"},"result":{"gasUsed":"0x0","output":"0x"},"subtraces":0,"traceAddress":[],"type":"call"}],"transactionHash":"0xc5a7720139c9567c1ca2f0c63bc16d1413178fa928592f85a183a4154a182660","vmTrace":null},{"output":"0x","stateDiff":null,"trace":[{"action":{"callType":"call","from":"0xe35f12181a2748285358b63cff25887410d0804b","gas":"0xc7d8","input":"0x86d1a69f","to":"0x72dfd50910aee68a69f0326819fcf906e484221d","value":"0x0"},"result":{"gasUsed":"0xa0c8","output":"0x"},"subtraces":2,"traceAddress":[],"type":"call"},{"action":{"callType":"call","from":"0x72dfd50910aee68a69f0326819fcf906e484221d","gas":"0xb8fd","input":"0x70a0823100000000000000000000000072dfd50910aee68a69f0326819fcf906e484221d","to":"0xaa1ae5e57dc05981d83ec7fca0b3c7ee2565b7d6","value":"0x0"},"result":{"gasUsed":"0x2c1","output":"0x000000000000000000000000000000000000000000000000000000e8d4a51000"},"subtraces":0,"traceAddress":[0],"type":"call"},{"action":{"callType":"call","from":"0x72dfd50910aee68a69f0326819fcf906e484221d","gas":"0xae11","input":"0xa9059cbb000000000000000000000000f2a35b44363228a3265b0041d21e0f32d93475e4000000000000000000000000000000000000000000000000000000e8d4a51000","to":"0xaa1ae5e57dc05981d83ec7fca0b3c7ee2565b7d6","value":"0x0"},"result":{"gasUsed":"0x6ec0","output":"0x0000000000000000000000000000000000000000000000000000000000000001"},"subtraces":0,"traceAddress":[1],"type":"call"}],"transactionHash":"0x33c975242c335058ad66e4b239bfe879e673e230d541910c4b0f9713e38c7e8e","vmTrace":null},{"output":"0x","stateDiff":null,"trace":[{"action":{"callType":"call","from":"0xa2635b3d63b4e31976419865e1a81553bb347be3","gas":"0xc7d8","input":"0x86d1a69f","to":"0x3ba8a69a4dac31c2c633c5ece2f60260086a82db","value":"0x0"},"result":{"gasUsed":"0xa0c8","output":"0x"},"subtraces":2,"traceAddress":[],"type":"call"},{"action":{"callType":"call","from":"0x3ba8a69a4dac31c2c633c5ece2f60260086a82db","gas":"0xb8fd","input":"0x70a082310000000000000000000000003ba8a69a4dac31c2c633c5ece2f60260086a82db","to":"0xaa1ae5e57dc05981d83ec7fca0b3c7ee2565b7d6","value":"0x0"},"result":{"gasUsed":"0x2c1","output":"0x000000000000000000000000000000000000000000000000000000174876e800"},"subtraces":0,"traceAddress":[0],"type":"call"},{"action":{"callType":"call","from":"0x3ba8a69a4dac31c2c633c5ece2f60260086a82db","gas":"0xae11","input":"0xa9059cbb00000000000000000000000005ebd0c649e756a94e50186fc860fa571d53b49a000000000000000000000000000000000000000000000000000000174876e800","to":"0xaa1ae5e57dc05981d83ec7fca0b3c7ee2565b7d6","value":"0x0"},"result":{"gasUsed":"0x6ec0","output":"0x0000000000000000000000000000000000000000000000000000000000000001"},"subtraces":0,"traceAddress":[1],"type":"call"}],
  // "transactionHash":"0xe063bdc16aea473efeabda9bc467122256e1d487f9ddb7b0f1e5d6076f086016","vmTrace":null}],
  //"id":1}

  double jsonrpc;
  TraceTransactionData[] result;
  int id;

  public TraceTransactionData[] getBlock() {
    return result;
  }
}
