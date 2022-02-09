package com.github.kischang.raspberry.utils.cmd;

import com.github.kischang.raspberry.utils.RaspCmdUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 守护运行的命令行指令
 *
 * @author KisChang
 */
public class CommandDaemon implements Runnable {

    private String[] cmd;
    private String cmdName;
    private Thread runThread;
    private Process runProcess;

    //当前状态
    private StringBuilder outStr = new StringBuilder();
    private StringBuilder errOutStr = new StringBuilder();
    private boolean hasFail = false;

    public CommandDaemon(String cmd, String name) {
        this(cmd);
        this.cmdName = name;
    }

    public CommandDaemon(String cmd) {
        if (cmd.contains(" ")){
            this.cmd = cmd.split(" ");
        }else {
            this.cmd = new String[]{cmd};
        }
        this.cmdName = this.cmd[0];
    }

    public CommandDaemon(String[] cmd) {
        this.cmd = cmd;
        this.cmdName = this.cmd[0];
    }

    public void startDaemon() {
        if (this.runThread == null) {
            this.runThread = new Thread(this);
            this.runThread.start();
        }
    }

    @Override
    public void run() {
        try {
            if (this.cmd.length == 1){
                this.runProcess = Runtime.getRuntime().exec(this.cmd[0]);
            }else {
                this.runProcess = Runtime.getRuntime().exec(this.cmd);
            }
        } catch (IOException e) {
            e.printStackTrace();
            this.appendErr("started_fail: " + e.getMessage());
            return;
        }
        new Thread(()->{
            try (BufferedReader in = new BufferedReader(new InputStreamReader(this.runProcess.getInputStream()))) {
                String line = null;
                while ((line = in.readLine()) != null) {
                    this.appendOut("\n").appendOut(line);
                }
            } catch (IOException e) {
                this.appendErr("\nprocess_input_fail").appendErr(e.getMessage());
            }
        }).start();
        new Thread(()->{
            try (BufferedReader in = new BufferedReader(new InputStreamReader(this.runProcess.getErrorStream()))) {
                String line = null;
                while ((line = in.readLine()) != null) {
                    this.appendErr("\n").appendErr(line);
                }
            } catch (IOException e) {
                this.appendErr("\nerror_input_fail").appendErr(e.getMessage());
            }
        }).start();
        try {
            this.runProcess.waitFor();
        } catch (InterruptedException e) {
            this.appendErr("\nwait_for_fail").appendErr(e.getMessage());
        }
    }

    private CommandDaemon appendOut(String out) {
        if (RaspCmdUtils.DAEMON_OUT_PRINT){
            System.out.printf("CMD:[%s] OUT: %s%s", cmdName, out, out.endsWith("\n") ? "" : "\n");
        }
        this.outStr.append(out);
        return this;
    }
    private CommandDaemon appendErr(String err) {
        System.err.printf("CMD:[%s] FAIL: %s%s", cmdName, err, err.endsWith("\n") ? "" : "\n");
        this.hasFail = true;
        this.errOutStr.append(err);
        return this;
    }

    public void shutdown(){
        if (this.runProcess != null){
            this.runProcess.destroy();
        }
    }

    public String getOutStr() {
        return outStr.toString();
    }

    public String getErrOutStr() {
        return errOutStr.toString();
    }

    public boolean isHasFail() {
        return hasFail;
    }
}
