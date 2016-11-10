package it.skarafaz.mercury.infrastructure.ssh;

import android.util.Log;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

import org.greenrobot.eventbus.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import it.skarafaz.mercury.infrastructure.event.SshCommandEnd;
import it.skarafaz.mercury.infrastructure.event.SshCommandStart;

public abstract class SshCommand extends Thread {
    protected static final int TIMEOUT = 10000;
    private static final Logger logger = LoggerFactory.getLogger(SshCommand.class);
    protected JSch jsch;
    protected Session session;
    protected String host;
    protected Integer port;
    protected String user;
    protected String password;
    protected String sudoPath;
    protected String nohupPath;
    protected Boolean sudo;
    protected String cmd;
    protected Boolean confirm;
    protected String result;

    public SshCommand() {
        this.jsch = new JSch();
    }

    @Override
    public void run() {
        if(beforeExecute()) {
            SshCommandStatus status = execute();
            afterExecute(status);
        }
    }

    protected boolean beforeExecute() {
        EventBus.getDefault().postSticky(new SshCommandStart());
        return true;
    }

    private SshCommandStatus execute() {
        SshCommandStatus status = SshCommandStatus.COMMAND_SENT;

        if (initConnection()) {
            if (connect()) {
                if (!send(formatCmd(cmd))) {
                    status = SshCommandStatus.EXECUTION_FAILED;
                }
                disconnect();
            } else {
                status = SshCommandStatus.CONNECTION_FAILED;
            }
        } else {
            status = SshCommandStatus.CONNECTION_INIT_ERROR;
        }

        return status;
    }

    protected void afterExecute(SshCommandStatus status) {
        EventBus.getDefault().postSticky(new SshCommandEnd(status, result));
    }

    protected boolean initConnection() {
        return true;
    }

    protected boolean connect() {
        boolean success = true;
        try {
            session = jsch.getSession(user, host, port);

            session.setUserInfo(getUserInfo());
            session.setConfig(getSessionConfig());
            session.setPassword(password);

            session.connect(TIMEOUT);
        } catch (JSchException e) {
            logger.error(e.getMessage().replace("\n", " "));
            success = false;
        }
        return success;
    }

    protected boolean send(String cmd) {
        logger.debug("sending command: {}", cmd);

        ChannelExec channel = null;

        boolean success = true;
        try {
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(cmd);
            channel.setInputStream(null);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            channel.setOutputStream(baos, true);
            //channel.setOutputStream(baos);

            InputStream stdout = channel.getInputStream();
            InputStream stderr = channel.getErrStream();

            channel.connect(TIMEOUT);
            success = waitForChannelClosed(channel, stdout, stderr);
        } catch (IOException | JSchException e) {
            logger.error(e.getMessage().replace("\n", " "));
            success = false;
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
        }
        return success;
    }

    protected boolean waitForChannelClosed(ChannelExec channel, InputStream stdout, InputStream stderr) {
        BufferedReader r = new BufferedReader(new InputStreamReader(stdout));
        try {
            StringBuilder total = new StringBuilder(stdout.available());
            String line;
            while ((line = r.readLine()) != null) {
                total.append(line).append('\n');
            }
            result = total.toString();
            Log.e("toto", "" + result);
        } catch (IOException e) {
            e.printStackTrace();
        }

/*        try {
            byte[] data = new byte[stdout.available()];
            stdout.read(data);
            result = new String(data);
            Log.e("toto",result);
        }catch (IOException e){
            e.printStackTrace();
        }*/
        return true;
    }

    protected void disconnect() {
        session.disconnect();
    }

    protected UserInfo getUserInfo() {
        return null;
    }

    protected Properties getSessionConfig() {
        return new Properties();
    }

    protected String formatCmd(String cmd) {
        return cmd;
    }
}
