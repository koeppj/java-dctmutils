package net.koeppster.dctm.utils;

public class ArgType {
    private String[] argNames;
    private String helpText;
    private String description;
    private Object defaultValue;
    @SuppressWarnings("rawtypes")
    private Class argType;

    // Parametrized constructor
    /**
     * @param <T>
     * @param argNames The short and long arg names
     * @param helpText Brief description of the parameter
     * @param description Longer descriptive text
     * @param defaultValue The default value
     * @param argType The primative type
     */
    public <T> ArgType(String[] argNames, String helpText, String description, Object defaultValue, Class<T> argType) {
        this.argNames = argNames;
        this.helpText = helpText;
        this.description = description;
        this.defaultValue = defaultValue;
        this.argType = argType;
    }

    /**
     * @param argNames The short and long arg names
     * @param helpText Brief description of the parameter
     * @param description Longer descriptive text
     * @param defaultValue The default value
     */
    public ArgType(String[] argNames, String helpText, String description, Object defaultValue) {
        this.argNames = argNames;
        this.helpText = helpText;
        this.description = description;
        this.defaultValue = defaultValue;
        this.argType = String.class;
    }
    // Getter and Setter for argNames
    public String[] getargNames() {
        return argNames;
    }

    public void setargNames(String[] argNames) {
        this.argNames = argNames;
    }

    // Getter and Setter for helpText
    public String getHelpText() {
        return helpText;
    }

    public void setHelpText(String helpText) {
        this.helpText = helpText;
    }

    // Getter and Setter for description
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Getter and Setter for defaultValue
    public Object getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    @SuppressWarnings("rawtypes")
    public Class getArgType() {
        return argType;
    }

    @SuppressWarnings("rawtypes")
    public void setArgType(Class argType) {
        this.argType = argType;
    }

    // Override toString for better readability
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ArgType [ ");
        sb.append("argNames=").append(String.join(", ", argNames));
        sb.append(", helpText='").append(helpText).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", defaultValue=").append(defaultValue);
        sb.append(" ]");
        return sb.toString();
    }
}
