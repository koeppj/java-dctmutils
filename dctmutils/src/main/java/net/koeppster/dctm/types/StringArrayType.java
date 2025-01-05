package net.koeppster.dctm.types;

import net.sourceforge.argparse4j.inf.Argument;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.ArgumentType;

public class StringArrayType implements ArgumentType<StringArray> {

  @Override
  public StringArray convert(ArgumentParser parser, Argument arg, String value) throws ArgumentParserException {
    try {
      return new StringArray(value);
    } catch (Exception e) {
      throw new ArgumentParserException("Error creating StringArray: " + e.getMessage(), e, parser, arg);
    }
  }

  public StringArrayType()  {
  }

}
