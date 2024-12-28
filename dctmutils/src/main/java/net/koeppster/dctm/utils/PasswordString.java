package net.koeppster.dctm.utils;

import com.documentum.fc.common.DfLogger;

import java.io.Console;
import java.io.IOException;
import net.koeppster.utils.MachineIdUtils;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;

public class PasswordString {
  private String password;
  private boolean acceptSysInput = false;

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
      this.acceptSysInput = true;
    } else {
      this.password = encryptPassword(password);
    }
  }

  @Override
  public String toString() {
    return "****"; // Masks the password when calling toString
  }

  // Custom method to get the unmasked password
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

  public static String valueOf(String password) throws IOException {
    return new PasswordString(password).getPassword();
  }

  public PasswordString() {
    // Empty constructor
  }

  public String getEncryptedString() {
    return "UTILS_ENCRYPTED:".concat(this.password);
  }
}
