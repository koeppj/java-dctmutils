/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package net.koeppster.dctm.utils;

import org.junit.jupiter.api.Test;

import net.koeppster.NoExitSecurityManager;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.SecurityManager;

class AppTest {

    @Test 
    void checkPingBroker() {
        assertTrue(Utils.pingDocbroker("ubuntu-mini", "30189"),"Operating Docbroker Reported Up");
        assertFalse(Utils.pingDocbroker("ubuntu-mini", "30178"),"Check via invalid port reported down");
    }

    @Test
    void checkPingDocbase() {
        assertTrue(Utils.pingDocbase("ubuntu-mini", "30189", "sandbox"),"Operating docbase reported up");
        assertFalse(Utils.pingDocbase("ubuntu-mini", "30189", "badbox"),"Invalid docbase reported unavailable");
    }

    @Test
    void checkCheckLogin() {
        assertTrue(Utils.checkLogin("ubuntu-mini", "30189", "sandbox", "sandbox", "changeme"),"Login Attempt reported success");
        assertFalse(Utils.checkLogin("ubuntu-mini", "30189", "badbox", "sandbox", "badpass"),"Login Attempt Reported failed");
    }

    @Test
    void checkMainNoCmd() {
        SecurityManager origManager = System.getSecurityManager();
        NoExitSecurityManager newManager = new NoExitSecurityManager();
        System.setSecurityManager(newManager);
        try {
            assertThrows(NoExitSecurityManager.ExitException.class, () -> {
                Utils.main(new String[] {});
            },"Must return 1 when called with no args provided");
        }
        finally {
            System.setSecurityManager(origManager);
        }
    } 
    
    @Test
    void checkPingBrokerCmd() {
        SecurityManager origManager = System.getSecurityManager();
        NoExitSecurityManager newManager = new NoExitSecurityManager();
        System.setSecurityManager(newManager);
        try {
            NoExitSecurityManager.ExitException exitException = 
                assertThrows(NoExitSecurityManager.ExitException.class, () -> {
                    Utils.main(new String[] {"pingbroker"});
                }, "Must call System.exit(int)");
            assertEquals(1, exitException.status, "Must return 1 when called with no args");
            exitException = 
                assertThrows(NoExitSecurityManager.ExitException.class, () -> {
                    Utils.main(new String[] {"pingbroker", "ubuntu-mini", "1689"});
                }, "Must call System.exit(int)");
            assertEquals(1, exitException.status, "Must return 1 when called with proper number of but invalid args");
            exitException = 
                assertThrows(NoExitSecurityManager.ExitException.class, () -> {
                    Utils.main(new String[] {"pingbroker", "ubuntu-mini", "30189"});
                }, "Must call System.exit(int)");
            assertEquals(0, exitException.status, "Must return 0 when called with proper number of valid args");
        }
        finally {
            System.setSecurityManager(origManager);
        }
    } 

    @Test
    void checkPingDocbaseCmd() {
        SecurityManager origManager = System.getSecurityManager();
        NoExitSecurityManager newManager = new NoExitSecurityManager();
        System.setSecurityManager(newManager);
        try {
            NoExitSecurityManager.ExitException exitException = 
                assertThrows(NoExitSecurityManager.ExitException.class, () -> {
                    Utils.main(new String[] {"pingdocbase"});
                }, "Must call System.exit(int)");
            assertEquals(1, exitException.status, "Must return 1 when called with no args");
            exitException = 
                assertThrows(NoExitSecurityManager.ExitException.class, () -> {
                    Utils.main(new String[] {"pingdocbase", "ubuntu-mini", "30189", "badbox"});
                }, "Must call System.exit(int)");
            assertEquals(1, exitException.status, "Must return 1 when called with proper number of but invalid args");
            exitException = 
                assertThrows(NoExitSecurityManager.ExitException.class, () -> {
                    Utils.main(new String[] {"pingdocbase", "ubuntu-mini", "30189", "sandbox"});
                }, "Must call System.exit(int)");
            assertEquals(0, exitException.status, "Must return 0 when called with proper number of valid args");
        }
        finally {
            System.setSecurityManager(origManager);
        }
    } 
}
