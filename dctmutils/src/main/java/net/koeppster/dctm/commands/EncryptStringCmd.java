package net.koeppster.dctm.commands;

import net.koeppster.dctm.types.PasswordString;
import net.koeppster.dctm.types.PasswordType;
import net.koeppster.dctm.utils.UtilsArgsParserFactory;
import net.koeppster.dctm.utils.UtilsException;
import net.koeppster.dctm.utils.UtilsFunction;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

public class EncryptStringCmd extends AbstractCmd implements UtilsFunction {

    public static final String[] ARG_STRING_NAME = {"-s","--string"};
    public static final String ARG_STRING = "string";
    public static final String ARG_STRING_HELP = "Value of string to encrypt";
    public static final String CMD_ENCRYPT  = "encrypt";


    public static void addCommandToArgParser(UtilsArgsParserFactory argParser) throws ArgumentParserException {
      Subparser cmd = argParser.addSubparser(CMD_ENCRYPT, "Encrypt a String and output to console", new EncryptStringCmd());
      argParser.addArgument(cmd,ARG_STRING_NAME,ARG_STRING,ARG_STRING_HELP,true,new PasswordType());
    }

    public void execute(Namespace ns) throws UtilsException {
        PasswordString password = ns.get(ARG_STRING);
        System.out.printf("Encrypted Value:%s%n",password.getEncryptedString());
    }

}
