package it.skarafaz.mercury.infrastructure.event;

import it.skarafaz.mercury.infrastructure.ssh.SshCommandDrop;

public class SshCommandPubKeyInput {
    SshCommandDrop<String> drop;

    public SshCommandPubKeyInput(SshCommandDrop<String> drop) {
        this.drop = drop;
    }

    public SshCommandDrop<String> getDrop() {
        return drop;
    }
}
