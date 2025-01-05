package net.koeppster.dctm.types;

import com.documentum.fc.common.DfLogger;
import java.io.Console;
import java.io.IOException;
import net.koeppster.utils.MachineIdUtils;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;

/**
 * The PasswordString class is responsible for handling passwords securely. It provides
 * functionality to encrypt, decrypt, and mask passwords.
 *
 * <p>Features:
 *
 * <ul>
 *   <li>Encrypts passwords using a specified encryption library (e.g., Jasypt).
 *   <li>Decrypts encrypted passwords.
 *   <li>Masks passwords when calling toString to prevent exposure.
 *   <li>Accepts passwords from system input if specified.
 * </ul>
 *
 * <p>Usage:
 *
 * <pre>{@code
 * PasswordString passwordString = new PasswordString("myPassword");
 * String encryptedPassword = passwordString.getEncryptedString();
 * String decryptedPassword = passwordString.getPassword();
 * }</pre>
 *
 * <p>Constructor:
 *
 * <ul>
 *   <li>{@link #PasswordString(String)}: Initializes the PasswordString with the given password.
 * </ul>
 *
 * <p>Methods:
 *
 * <ul>
 *   <li>{@link #toString()}: Returns a masked string representation of the password.
 *   <li>{@link #getPassword()}: Returns the unmasked password, either from console input or
 *       decrypted.
 *   <li>{@link #getEncryptedString()}: Returns the encrypted password with a specific prefix.
 *   <li>{@link #valueOf(String)}: Static method to get the unmasked password from a given string.
 * </ul>
 *
 * <p>Exceptions:
 *
 * <ul>
 *   <li>{@link IllegalArgumentException}: Thrown if the password is null or empty.
 *   <li>{@link IllegalStateException}: Thrown if the console is not available for system input.
 *   <li>{@link IOException}: Thrown if there is an error during encryption or decryption.
 * </ul>
 */
public class PasswordString {
  private String password;
  private boolean acceptSysInput = false;

  /**
   * Constructs a PasswordString object with the given password.
   *
   * @param password the password to be used for the PasswordString object.
   * @throws IOException if an I/O error occurs.
   * @throws IllegalArgumentException if the password is null or empty.
   *     <p>The password can be in one of the following formats: - A plain text password, which will
   *     be encrypted. - A password prefixed with "UTIILS_ENCRYPTED:", which will be encrypted after
   *     removing the prefix. - A password starting with "-", which indicates that system input is
   *     accepted.
   */
  public PasswordString(String password) throws IOException {
    DfLogger.debug(this, "Creating PasswordString with password ****", null, null);
    if (password == null) {
      throw new IllegalArgumentException("Password cannot be null");
    }
    if (password.isEmpty()) {
      throw new IllegalArgumentException("Password cannot be empty");
    }
    if (password.startsWith("UTIILS_ENCRYPTED:")) {
      this.password = encryptPassword(password.substring(17));
    } // Skip the prefix and the colon
    else if (password.startsWith("-")) {
      Console console = System.console();
      if (console == null) {
        throw new IllegalStateException("Console not available");
      }
      console.printf("Enter Password: ");
      char[] passwordChars = console.readPassword();
      this.password = encryptPassword(new String(passwordChars));
    } else {
      this.password = encryptPassword(password);
    }
  }

  /**
   * Returns a masked string representation of the password. This method overrides the default
   * toString implementation to ensure that the actual password is not exposed when the object is
   * printed.
   *
   * @return a string containing "****" to mask the password
   */
  @Override
  public String toString() {
    return "****"; // Masks the password when calling toString
  }

  /**
   * Retrieves the password based on the system input acceptance flag. If system input is accepted,
   * it prompts the user to enter the password via the console. Otherwise, it decrypts and returns
   * the stored password.
   *
   * @return the password as a String
   * @throws IOException if an I/O error occurs while reading from the console
   */
  public String getPassword() throws IOException {
    if (this.acceptSysInput) {
      return getPasswordFromConsole();
    } else {
      return decryptPassword(this.password);
    }
  }

  private String getPasswordFromConsole() {
    // Use JLine or other library to read password from console
    Console console = System.console();
    if (console == null) {
      throw new IllegalStateException("Console not available");
    }
    console.printf("Enter password:");
    char[] passwordChars = console.readPassword();
    return new String(passwordChars);
  }

  // Method to encrypt the password
  private String encryptPassword(String arg0) throws IOException {
    // Use Jasypt or other encryption library here
    // Example with Jasypt (requires integration):
    StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
    encryptor.setPassword(MachineIdUtils.getMachineId());
    return encryptor.encrypt(arg0);
  }

  private String decryptPassword(String encryptedPassword) throws IOException {
    // Use Jasypt or other decryption library here
    // Example with Jasypt (requires integration):
    StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
    encryptor.setPassword(MachineIdUtils.getMachineId());
    return encryptor.decrypt(encryptedPassword);
  }

  /**
   * Converts the given password string into a PasswordString object and returns its password.
   *
   * @param password the password string to be converted
   * @return the password from the PasswordString object
   * @throws IOException if an I/O error occurs
   */
  public static String valueOf(String password) throws IOException {
    return new PasswordString(password).getPassword();
  }

  public PasswordString() {
    // Empty constructor
  }

  /**
   * Returns the encrypted version of the password. The encrypted string is prefixed with
   * "UTILS_ENCRYPTED:".
   *
   * @return the encrypted password string
   */
  public String getEncryptedString() {
    return "UTILS_ENCRYPTED:".concat(this.password);
  }
}
