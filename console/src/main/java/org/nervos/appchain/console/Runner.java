package org.nervos.appchain.console;

import org.nervos.appchain.codegen.Console;
import org.nervos.appchain.codegen.SolidityFunctionWrapperGenerator;
import org.nervos.appchain.codegen.TruffleJsonFunctionWrapperGenerator;
import org.nervos.appchain.utils.Version;

import static org.nervos.appchain.utils.Collection.tail;

/**
 * Main entry point for running command line utilities.
 */
public class Runner {

    private static String USAGE = "Usage: nervosj version|wallet|solidity ...";

    private static String LOGO = "\n" // generated at http://patorjk.com/software/taag
            + "              _      _____ _     _        \n"
            + "             | |    |____ (_)   (_)       \n"
            + "__      _____| |__      / /_     _   ___  \n"
            + "\\ \\ /\\ / / _ \\ '_ \\     \\ \\ |   | | / _ \\ \n"
            + " \\ V  V /  __/ |_) |.___/ / | _ | || (_) |\n"
            + "  \\_/\\_/ \\___|_.__/ \\____/| |(_)|_| \\___/ \n"
            + "                         _/ |             \n"
            + "                        |__/              \n";

    public static void main(String[] args) throws Exception {
        System.out.println(LOGO);

        if (args.length < 1) {
            Console.exitError(USAGE);
        } else {
            switch (args[0]) {
                case "wallet":
                    WalletRunner.run(tail(args));
                    break;
                case "solidity":
                    SolidityFunctionWrapperGenerator.run(tail(args));
                    break;
                case "truffle":
                    TruffleJsonFunctionWrapperGenerator.run(tail(args));
                    break;
                case "version":
                    Console.exitSuccess("Version: " + Version.getVersion() + "\n"
                            + "Build timestamp: " + Version.getTimestamp());
                    break;
                default:
                    Console.exitError(USAGE);
            }
        }
    }
}
