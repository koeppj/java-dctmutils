package net.koeppster.dctm.utils;

import net.sourceforge.argparse4j.inf.Namespace;

public interface UtilsFunction {
    public void execute(Namespace ns) throws UtilsException;
}
