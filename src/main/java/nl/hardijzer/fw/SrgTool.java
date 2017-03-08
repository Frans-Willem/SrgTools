
package nl.hardijzer.fw;

import java.io.IOException;
import java.util.Arrays;

import nl.hardijzer.fw.applysrg.*;
import nl.hardijzer.fw.chainsrg.*;
import nl.hardijzer.fw.checkabstract.*;
import nl.hardijzer.fw.integratemcpsrg.*;
import nl.hardijzer.fw.jarcompare.*;
import nl.hardijzer.fw.reversesrg.*;
import nl.hardijzer.fw.srgcollisions.*;

public class SrgTool {
    public static void main(String[] commandArgs) throws IOException {
        if (commandArgs.length < 1) {
            usage();
        }

        String command = commandArgs[0];
        String[] args = Arrays.copyOfRange(commandArgs, 1, commandArgs.length);

        if (command.equalsIgnoreCase("apply")) {
            ApplySrg.main(args);
        } else if (command.equalsIgnoreCase("chain")) {
            ChainSrg.main(args);
        } else if (command.equalsIgnoreCase("checkabstract") || command.equalsIgnoreCase("check")) {
            CheckAbstract.main(args);
        } else if (command.equalsIgnoreCase("integratemcp")) {
            IntegrateMcpSrg.main(args);
        } else if (command.equalsIgnoreCase("jarcompare")) {
            JarCompare.main(args);
        } else if (command.equalsIgnoreCase("reverse")) {
            ReverseSrg.main(args);
        } else if (command.equalsIgnoreCase("collisions")) {
            SrgCollisions.main(args);
        } else {
            usage();
        }
    }

    private static void usage() {
        System.out.println("Usage: java -jar srgtool.jar command args");
        System.out.println("where command is one of:");
        System.out.println("apply");
        System.out.println("chain");
        System.out.println("checkabstract");
        System.out.println("integratemcp");
        System.out.println("jarcompare");
        System.out.println("reverse");
        System.out.println("collisions");

        System.exit(-1);
    }
}
