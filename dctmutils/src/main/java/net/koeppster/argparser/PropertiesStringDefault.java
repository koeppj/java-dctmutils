package net.koeppster.argparser;

import java.util.Properties;
import net.sourceforge.argparse4j.inf.Argument;


public class PropertiesStringDefault {

    private Argument parentArg = null;
    private Properties defaultProps = null;

    /**
     * Constructs a new instance of PropertiesStringDefault with the specified argument and properties.
     *
     * @param arg0 The Argument object cpntaining the name of the key in the defaults property bag.
     * @param arg1 The Properties object containing default values.
     * @throws IllegalArgumentException if either {@code arg0} or {@code arg1} is {@code null}.
     */
    public PropertiesStringDefault(Argument arg0, Properties arg1) {
        if (arg0 == null) {
            throw new IllegalArgumentException("Argument 'arg0' must not be null.");
        }
        if (arg1 == null) {
            throw new IllegalArgumentException("Argument 'arg1' must not be null.");
        }
        parentArg = arg0;
        defaultProps = arg1;
    }
    

    /**
     * Return the default value by looking the properties bad for a key 
     * that matches the argument name
     */
    @Override
    public String toString() {
        return defaultProps.getProperty(parentArg.getDest());
    }

}
