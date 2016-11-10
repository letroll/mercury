package it.skarafaz.mercury.infrastructure.event;

import it.skarafaz.mercury.infrastructure.ssh.SshCommandDrop;

public class SshCommandConfirm {
    private String cmd;
    private SshCommandDrop<Boolean> drop;

    public SshCommandConfirm(String cmd, SshCommandDrop<Boolean> drop) {
        this.cmd = cmd;
        this.drop = drop;
    }

    public String getCmd() {
        return cmd;
    }

    public SshCommandDrop<Boolean> getDrop() {
        return drop;
    }
}
