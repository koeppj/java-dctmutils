package net.koeppster.dctm.utils;

import net.sourceforge.argparse4j.inf.Namespace;

public class PasswordEncryptCmd implements UtilsFunction {

    @Override
    public void execute(Namespace ns) throws UtilsException {
        PasswordString password = ns.get(UtilsArgsParserFactory.ARG_PASS);
        System.out.printf("Encrypted password string: %s%n",password.getEncryptedString());
    }

}
