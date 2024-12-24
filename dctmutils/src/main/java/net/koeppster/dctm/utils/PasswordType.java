package net.koeppster.dctm.utils;

import com.documentum.fc.common.DfLogger;

import net.sourceforge.argparse4j.inf.Argument;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.ArgumentType;

public class PasswordType implements ArgumentType<PasswordString> {

  @Override
  public PasswordString convert(ArgumentParser parser, Argument arg, String value) throws ArgumentParserException {
    DfLogger.debug(this, "Converting PasswordType with value ****", null, null);
    try {
      return new PasswordString(value);
    } catch (Exception e) {
      throw new ArgumentParserException("Error creating PasswordString: " + e.getMessage(), e, parser, arg);
    }
  }

  public PasswordType()  {
  }
}
