package it.skarafaz.mercury.infrastructure.event;

import it.skarafaz.mercury.infrastructure.ssh.SshCommandStatus;

public class SshCommandEnd {
    private SshCommandStatus status;
    private String result;

    public SshCommandEnd(SshCommandStatus status, String result) {
        this.status = status;
        this.result = result;
    }

    public SshCommandStatus getStatus() {
        return status;
    }

    public String getResult() {
        return result;
    }
}
