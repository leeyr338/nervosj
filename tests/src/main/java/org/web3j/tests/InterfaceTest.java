package org.web3j.tests;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Uint;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Call;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthGetCode;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthMetaData;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.EthTransaction;
import org.web3j.protocol.core.methods.response.NetPeerCount;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;



public class InterfaceTest {

    private static String testNetAddr;
    private static int version;
    private static int chainId;
    private static Web3j service;
    private static String value;
    private static String privateKey;
    private static Properties props;
    private static long quota;
    private static final String configPath = "tests/src/main/resources/config.properties";
    private static String validTransactionHash;

    static {
        try {
            props = Config.load(configPath);
        } catch (Exception e) {
            System.out.println("Failed to get props from config file");
            System.exit(1);
        }
        testNetAddr = props.getProperty(Config.TEST_NET_ADDR);
        HttpService.setDebug(false);
        service = Web3j.build(new HttpService(testNetAddr));
        privateKey = props.getProperty(Config.SENDER_PRIVATE_KEY);
        quota = Long.parseLong(props.getProperty(Config.DEFAULT_QUOTA));
        version = Integer.parseInt(props.getProperty(Config.VERSION));
        chainId = Integer.parseInt(props.getProperty(Config.CHAIN_ID));
        value = "0";
    }

    public static void main(String[] args) throws Exception {


        System.out.println("======================================");
        System.out.println("***  0.  getbalance             ***");
        testGetBalance();

        System.out.println("======================================");
        System.out.println("***  1.  getMetaData            ***");
        testMetaData();

        System.out.println("======================================");
        System.out.println("***  2.  net_peerCount          ***");
        testNetPeerCount();

        System.out.println("======================================");
        System.out.println("***  3.  blockNumber          ***");
        BigInteger validBlockNumber = testBlockNumber();

        System.out.println(validBlockNumber);
        System.out.println(validBlockNumber.toString(16));
        System.out.println(DefaultBlockParameter.valueOf(validBlockNumber).getValue());

        System.out.println("======================================");
        System.out.println("***  4.  getBlockByNumber     ***");
        boolean returnFullTransactions = true;
        String validBlockHash = testEthGetBlockByNumber(
                validBlockNumber, returnFullTransactions).get();
        validBlockHash = "0xda9e8497221e9d18131292f8b459d62e03c882be4666d084c67b8dcebcce91d1";

        System.out.println("======================================");
        System.out.println("***  5.  getBlockByHash       ***");
        testEthGetBlockByHash(validBlockHash, returnFullTransactions);

        //because unsigned transaction is not supported in cita, there is no method sendTransaction.
        System.out.println("======================================");
        System.out.println("***  6.  sendRawTransaction      ***");
        String code = "6060604052341561000f57600080fd5b600160a060020"
                + "a033316600090815260208190526040902061271090556101"
                + "df8061003b6000396000f3006060604052600436106100565"
                + "763ffffffff7c010000000000000000000000000000000000"
                + "000000000000000000000060003504166327e235e38114610"
                + "05b578063a9059cbb1461008c578063f8b2cb4f146100c257"
                + "5b600080fd5b341561006657600080fd5b61007a600160a06"
                + "0020a03600435166100e1565b604051908152602001604051"
                + "80910390f35b341561009757600080fd5b6100ae600160a06"
                + "0020a03600435166024356100f3565b604051901515815260"
                + "200160405180910390f35b34156100cd57600080fd5b61007"
                + "a600160a060020a0360043516610198565b60006020819052"
                + "908152604090205481565b600160a060020a0333166000908"
                + "1526020819052604081205482901080159061011c57506000"
                + "82115b1561018e57600160a060020a0333811660008181526"
                + "0208190526040808220805487900390559286168082529083"
                + "9020805486019055917fddf252ad1be2c89b69c2b068fc378"
                + "daa952ba7f163c4a11628f55a4df523b3ef90859051908152"
                + "60200160405180910390a3506001610192565b5060005b929"
                + "15050565b600160a060020a03166000908152602081905260"
                + "40902054905600a165627a7a72305820f59b7130870eee8f0"
                + "44b129f4a20345ffaff662707fc0758133cd16684bc3b160029";
        BigInteger nonce = TestUtil.getNonce();
        BigInteger validUtil = TestUtil.getValidUtilBlock(service);

        Transaction rtx = Transaction.createContractTransaction(
                nonce, quota, validUtil.longValue(),
                version, chainId, value, code);
        String signedTx = rtx.sign(privateKey, false, false);

        validTransactionHash = testEthSendRawTransaction(signedTx).get();
        System.out.println("waiting for tx into chain ...");
        Thread.sleep(8000);

        System.out.println("======================================");
        System.out.println("***  7.  getTransactionByHash  ***");
        testEthGetTransactionByHash(validTransactionHash);

        System.out.println("======================================");
        System.out.println("***  8.  getTransactionCount   ***");
        Credentials credentials = Credentials.create(privateKey);
        String validAccount = credentials.getAddress();
        DefaultBlockParameter param = DefaultBlockParameter.valueOf("latest");
        testEthGetTransactionCount(validAccount, param);

        System.out.println("======================================");
        System.out.println("***  9.  getTransactionReceipt ***");
        String validContractAddress = testEthGetTransactionReceipt(validTransactionHash).get();

        System.out.println("======================================");
        System.out.println("***  10.  getCode               ***");
        DefaultBlockParameter parameter = DefaultBlockParameter.valueOf("latest");
        testEthGetCode(validContractAddress, parameter);

        System.out.println("======================================");
        System.out.println("***  11. eth_call                  ***");
        String fromAddr = Credentials.create(privateKey).getAddress();
        Function getBalanceFunc = new Function(
                "getBalance",
                Arrays.asList(new Address(fromAddr)),
                Arrays.asList(new TypeReference<Uint>() {
                })
        );
        String funcCallData = FunctionEncoder.encode(getBalanceFunc);

        testEthCall(fromAddr, validContractAddress, funcCallData, DefaultBlockParameterName.LATEST);
    }


    //0.  getBalance
    static void testGetBalance() throws Exception {
        Credentials c = Credentials.create(privateKey);
        String addr = c.getAddress();
        EthGetBalance ethGetbalance = service.ethGetBalance(
                addr, DefaultBlockParameterName.LATEST).send();
        if (ethGetbalance == null) {
            System.out.println("the result is null");
        } else {
            BigInteger balance = ethGetbalance.getBalance();
            System.out.println("Balance for addr " + addr + "is " + balance);
        }
    }

    //1.  getMetaData
    static void testMetaData() throws Exception {
        EthMetaData ethMetaData = service.ethMetaData(DefaultBlockParameterName.LATEST).send();
        if (ethMetaData == null) {
            System.out.println("the result is null");
        } else {
            System.out.println("BasicToken: "
                    + ethMetaData.getEthMetaDataResult().basicToken);
            System.out.println("ChainName: "
                    + ethMetaData.getEthMetaDataResult().chainName);
            System.out.println("Genesis TS: "
                    + ethMetaData.getEthMetaDataResult().genesisTimestamp);
            System.out.println("Operator: "
                    + ethMetaData.getEthMetaDataResult().operator);
            System.out.println("Website: "
                    + ethMetaData.getEthMetaDataResult().website);
            System.out.println("Block Interval: "
                    + ethMetaData.getEthMetaDataResult().blockInterval);
            System.out.println("Chain Id: "
                    + ethMetaData.getEthMetaDataResult().chainId);
            System.out.println("Validators: ");
            Arrays.asList(ethMetaData.getEthMetaDataResult().validators)
                    .stream()
                    .forEach(x -> System.out.println("Address: " + x.toString()));
        }
    }

    //1.  net_peerCount
    static void testNetPeerCount() throws Exception {
        NetPeerCount netPeerCount = service.netPeerCount().send();
        System.out.println("net_peerCount:" + netPeerCount.getQuantity());
    }

    //2.  blockNumber
    static BigInteger testBlockNumber() throws Exception {

        EthBlockNumber ethBlockNumber = service.ethBlockNumber().send();

        BigInteger validBlockNumber = BigInteger.valueOf(Long.MAX_VALUE);

        if (ethBlockNumber.isEmpty()) {
            System.out.println("the result is null");
        } else {
            validBlockNumber = ethBlockNumber.getBlockNumber();
            System.out.println("blockNumber:" + validBlockNumber);
        }
        return validBlockNumber;
    }

    //3.  getBlockByNumber
    public static Optional<String> testEthGetBlockByNumber(
            BigInteger validBlockNumber, boolean isfullTranobj)
            throws Exception {
        EthBlock ethBlock = service.ethGetBlockByNumber(
                DefaultBlockParameter.valueOf(validBlockNumber), isfullTranobj).send();

        if (ethBlock.isEmpty()) {
            System.out.println("the result is null");
            return Optional.empty();
        } else {
            EthBlock.Block block = ethBlock.getBlock();
            printBlock(block);
            return Optional.of(block.getHash());
        }
    }

    //4.  cita_getBlockByHash
    public static Optional<String> testEthGetBlockByHash(
            String validBlockHash, boolean isfullTran)
            throws Exception {
        EthBlock ethBlock = service
                .ethGetBlockByHash(validBlockHash, isfullTran).send();

        if (ethBlock.isEmpty()) {
            System.out.println("the result is null");
            return Optional.empty();
        } else {
            EthBlock.Block block = ethBlock.getBlock();
            printBlock(block);
            return Optional.of(block.getHash());
        }
    }


    //5.  sendRawTransaction
    public static Optional<String> testEthSendRawTransaction(
            String rawData) throws Exception {
        EthSendTransaction ethSendTx = service
                .ethSendRawTransaction(rawData).send();

        if (ethSendTx.isEmpty()) {
            System.out.println("the result is null");
            return Optional.empty();
        } else {
            String hash = ethSendTx.getSendTransactionResult().getHash();
            System.out.println("hash(Transaction):" + hash);
            System.out.println("status:"
                    + ethSendTx.getSendTransactionResult().getStatus());
            return Optional.of(hash);
        }
    }


    //6.  getTransactionByHash
    public static void testEthGetTransactionByHash(
            String validTransactionHash) throws Exception {
        EthTransaction ethTransaction = service.ethGetTransactionByHash(
                validTransactionHash).send();

        if (!ethTransaction.getTransaction().isPresent()) {
            System.out.println("the result is null");
        } else {
            org.web3j.protocol.core.methods.response.Transaction transaction
                    = ethTransaction.getTransaction().get();
            System.out.println("hash(Transaction):" + transaction.getHash());
            System.out.println("content:" + transaction.getContent());
            System.out.println("blockNumber(dec):" + transaction.getBlockNumber());
            System.out.println("blockHash:" + transaction.getBlockHash());
            System.out.println("index:" + transaction.getIndex());
        }
    }


    //7.  getTransactionCount
    public static void testEthGetTransactionCount(
            String validAccount, DefaultBlockParameter param) throws Exception {
        EthGetTransactionCount ethGetTransactionCount = service.ethGetTransactionCount(
                validAccount, param).send();

        if (ethGetTransactionCount.isEmpty()) {
            System.out.println("the result is null");
        } else {
            System.out.println("TransactionCount:"
                    + ethGetTransactionCount.getTransactionCount());
        }
    }

    //8.  getTransactionReceipt
    public static Optional<String> testEthGetTransactionReceipt(
            String validTransactionHash) throws Exception {
        EthGetTransactionReceipt ethGetTransactionReceipt = service.ethGetTransactionReceipt(
                validTransactionHash).send();

        if (!ethGetTransactionReceipt.getTransactionReceipt().isPresent()) {
            System.out.println("the result is null");
            return Optional.empty();
        } else {
            //is option_value is null return NoSuchElementException, else return option_value
            TransactionReceipt transactionReceipt =
                    ethGetTransactionReceipt.getTransactionReceipt().get();
            printTransactionReceiptInfo(transactionReceipt);
            return Optional.of(transactionReceipt.getContractAddress());
        }

    }

    public static void printTransactionReceiptInfo(
            TransactionReceipt transactionReceipt) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String result = mapper.writeValueAsString(transactionReceipt);
            System.out.println(result);
        } catch (JsonProcessingException e) {
            System.out.println("Failed to process object to json. Exception: " + e);
            e.printStackTrace();
            System.exit(1);
        }
    }

    //9.  eth_getCode
    public static void testEthGetCode(
            String validContractAddress, DefaultBlockParameter param)
            throws Exception {
        EthGetCode ethGetCode = service
                .ethGetCode(validContractAddress, param).send();

        if (ethGetCode.isEmpty()) {
            System.out.println("the result is null");
        } else {
            System.out.println("contractcode:" + ethGetCode.getCode());
        }
    }


    public Function totalSupply() {
        return new Function(
                "get",
                Collections.emptyList(),
                Collections.singletonList(new TypeReference<Uint256>() {
                }));
    }

    //10.  eth_call
    public static void testEthCall(
            String fromaddress, String contractAddress, String encodedFunction,
            DefaultBlockParameter param) throws Exception {
        EthCall ethCall = service.ethCall(
                new Call(fromaddress, contractAddress, encodedFunction),
                param).send();

        System.out.println("call result:" + ethCall.getValue());
    }

    private static void printBlock(EthBlock.Block block) {
        System.out.println("hash(blockhash):"
                + block.getHash());
        System.out.println("version:"
                + block.getVersion());
        System.out.println("header.timestamp:"
                + block.getHeader().getTimestamp());
        System.out.println("header.prevHash:"
                + block.getHeader().getPrevHash());
        System.out.println("header.number(hex):"
                + block.getHeader().getNumber());
        System.out.println("header.number(dec):"
                + block.getHeader().getNumberDec());
        System.out.println("header.stateRoot:"
                + block.getHeader().getStateRoot());
        System.out.println("header.transactionsRoot:"
                + block.getHeader().getTransactionsRoot());
        System.out.println("header.receiptsRoot:"
                + block.getHeader().getReceiptsRoot());
        System.out.println("header.gasUsed:"
                + block.getHeader().getGasUsed());
        System.out.println("header.proposer:"
                + block.getHeader().getProposer());
        System.out.println("header.proof.proposal:"
                + block.getHeader().getProof().getTendermint().getProposal());
        System.out.println("header.proof.height:"
                + block.getHeader().getProof().getTendermint().getHeight());
        System.out.println("header.proof.round:"
                + block.getHeader().getProof().getTendermint().getRound());

        if (!block.getBody().getTransactions().isEmpty()) {
            System.out.println("number of transaction:"
                    + block.getBody().getTransactions().size());

            for (int i = 0; i < block.getBody().getTransactions().size(); i++) {
                org.web3j.protocol.core.methods.response.Transaction tx =
                        (org.web3j.protocol.core.methods.response.Transaction)
                                block.getBody().getTransactions().get(i).get();
                System.out.println("body.transactions.tranhash:" + tx.getHash());
                System.out.println("body.transactions.content:" + tx.getContent());
            }
        } else {
            System.out.println("the block transactions is null");
        }
    }
}