package it.skarafaz.mercury.infrastructure.event;

import it.skarafaz.mercury.infrastructure.ssh.SshCommandDrop;

public class SshCommandPassword {
    private String message;
    private SshCommandDrop<String> drop;

    public SshCommandPassword(String message, SshCommandDrop<String> drop) {
        this.message = message;
        this.drop = drop;
    }

    public SshCommandDrop<String> getDrop() {
        return drop;
    }

    public String getMessage() {
        return message;
    }
}
