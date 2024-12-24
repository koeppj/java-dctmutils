package net.koeppster.utils;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MachineIdUtils {

    // Method to retrieve machine ID based on platform
    public static String getMachineId() throws IOException {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            // Windows: Access the registry for Machine GUID
            return getWindowsMachineGuid();
        } else if (os.contains("nix") || os.contains("nux")) {
            // Linux: Access the /etc/machine-id
            return getLinuxMachineId();
        } else {
            return "Unknown OS";
        }
    }

    // Access the Windows Machine GUID from the registry
    private static String getWindowsMachineGuid() {
        // Read the Windows Machine GUID from the registry
        return Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "SOFTWARE\\Microsoft\\Cryptography", "MachineGuid");        
    }

    // Access the Linux machine ID from /etc/machine-id
    private static String getLinuxMachineId() throws IOException {
        return new String(Files.readAllBytes(Paths.get("/etc/machine-id"))).trim();
    }

    public static void main(String[] args) throws IOException {
        String machineId = getMachineId();
        System.out.println("Machine ID: " + machineId);
    }
}
