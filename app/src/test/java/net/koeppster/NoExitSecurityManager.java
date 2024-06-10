package net.koeppster;

public class NoExitSecurityManager extends SecurityManager {
    public static final class ExitException extends SecurityException {
        public final int status;
        public ExitException(int status) {
            this.status = status;
        }
    }

    @Override
    public void checkPermission(java.security.Permission perm) {
        // Allow other activities by default
    }

    @Override
    public void checkPermission(java.security.Permission perm, Object context) {
        // Allow other activities by default
    }

    @Override
    public void checkExit(int status) {
        super.checkExit(status);
        throw new ExitException(status);
    }
}
