package net.koeppster.utils;


import oshi.SystemInfo;
import oshi.hardware.ComputerSystem;

import java.io.IOException;

public class MachineIdUtils {

    // Method to retrieve machine ID based on platform
    public static String getMachineId() throws IOException {
        SystemInfo si = new SystemInfo();
        ComputerSystem cs = si.getHardware().getComputerSystem();
        return cs.getHardwareUUID();
    }
}
