package net.koeppster.dctm.utils;

import com.documentum.fc.common.DfLogger;

import net.sourceforge.argparse4j.inf.Argument;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.ArgumentType;

public class DocbrokerSpecType implements ArgumentType<DocbrokerSpec> {

  @Override
  public DocbrokerSpec convert(ArgumentParser parser, Argument arg, String value)
      throws ArgumentParserException {
    DfLogger.debug(this, "Converting DocbrokerSpecType with value " + value, null, null);
    try {
      return new DocbrokerSpec(value);
    } catch (IllegalArgumentException e) {
      throw new ArgumentParserException("Error parsing Docbroker Connection Spec.", e, parser, arg);
    }
  }

  public DocbrokerSpecType() {
  }
}

