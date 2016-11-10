package it.skarafaz.mercury.infrastructure.ssh;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.UserInfo;

import org.greenrobot.eventbus.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

import it.skarafaz.mercury.MercuryApplication;
import it.skarafaz.mercury.R;
import it.skarafaz.mercury.infrastructure.event.SshCommandConfirm;
import it.skarafaz.mercury.infrastructure.event.SshCommandPassword;
import it.skarafaz.mercury.infrastructure.manager.SshManager;
import it.skarafaz.mercury.infrastructure.model.Command;

public class SshCommandRegular extends SshCommand {
    private static final Logger logger = LoggerFactory.getLogger(SshCommandRegular.class);

    public SshCommandRegular(Command command) {
        super();

        this.host = command.getServer().getHost();
        this.port = command.getServer().getPort();
        this.user = command.getServer().getUser();
        this.password = command.getServer().getPassword();
        this.sudoPath = command.getServer().getSudoPath();
        this.nohupPath = command.getServer().getNohupPath();
        this.sudo = command.getSudo();
        this.cmd = command.getCmd();
        this.confirm = command.getConfirm();
    }

    @Override
    protected boolean beforeExecute() {
        if (confirm) {
            SshCommandDrop<Boolean> drop = new SshCommandDrop<>();
            EventBus.getDefault().postSticky(new SshCommandConfirm(cmd, drop));

            if (!drop.take()) {
                return false;
            }
        }

        if (sudo && password == null) {
            SshCommandDrop<String> drop = new SshCommandDrop<>();
            String message = MercuryApplication.getContext().getString(R.string.type_sudo_password, formatServerLabel());
            EventBus.getDefault().postSticky(new SshCommandPassword(message, drop));

            password = drop.take();
            if (password == null) {
                return false;
            }
        }

        return super.beforeExecute();
    }

    @Override
    protected boolean initConnection() {
        boolean success = true;
        try {
            jsch.setKnownHosts(SshManager.getInstance().getKnownHosts().getAbsolutePath());
            jsch.addIdentity(SshManager.getInstance().getPrivateKey().getAbsolutePath());
        } catch (IOException | JSchException e) {
            logger.error(e.getMessage().replace("\n", " "));
            success = false;
        }
        return success;
    }

    @Override
    protected UserInfo getUserInfo() {
        return new SshCommandUserInfo();
    }

    @Override
    protected Properties getSessionConfig() {
        Properties config = super.getSessionConfig();
        config.put("PreferredAuthentications", "publickey,password");
        config.put("MaxAuthTries", "1");
        return config;
    }

    @Override
    protected String formatCmd(String cmd) {
        if (sudo) {
            return String.format("echo %s | %s -S -p '' %s %s > /dev/null 2>&1", password, sudoPath, nohupPath, cmd);
            //return String.format("export HISTIGNORE='*sudo -S*' && echo %s | sudo -S %s", password, cmd);
            //return String.format("echo %s | %s -S -p '' %s %s", password, sudoPath, nohupPath, cmd);
        } else {
            //return String.format("%s %s > /dev/null 2>&1", nohupPath, cmd);
            //return cmd;
            return String.format("%s %s", nohupPath, cmd);
        }
    }

    private String formatServerLabel() {
        StringBuilder sb = new StringBuilder(String.format("%s@%s", user, host));
        if (port != 22) {
            sb.append(String.format(":%d", port));
        }
        return sb.toString();
    }
}
