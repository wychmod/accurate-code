package com.wychmod.jvmtool;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;

/**
 * @description: jvm监控启动类
 * @author: wychmod
 * @date: 2025-02-09
 */
public class Bootstrap {

    static int port = 7766;
    private final ToolService toolService;

    public Bootstrap() throws RemoteException, NotBoundException, MalformedURLException {
        // 加载远程服务
        toolService = (ToolService) Naming.lookup("rmi://localhost:" + port + "/ToolService");
    }

    public static void main(String[] args) throws Exception {
        // 获取jvm进程列表
        List<VirtualMachineDescriptor> list = VirtualMachine.list();
        for (int i = 0; i < list.size(); i++) {
            System.out.println(String.format("[%s] %s", i, list.get(i).displayName()));
        }
        // 选择jvm进程
        BufferedReader read = new BufferedReader(new InputStreamReader(System.in));
        String line = read.readLine();
        int i = Integer.parseInt(line);
        // 附着agent
        VirtualMachine virtualMachine = VirtualMachine.attach(list.get(i));
        virtualMachine.loadAgent("D:\\idea\\accurate-code\\javaagent-example\\target\\javaagent-example-1.0-SNAPSHOT.jar", String.valueOf(port));
        virtualMachine.detach();
        System.out.println("加载成功");

        Bootstrap bootstrap = new Bootstrap();
        while (true) {
            line = read.readLine().trim();
            if (line.equals("exit")) {
                break;
            }
            try {
                bootstrap.runCommand(line);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void runCommand(String cmdAndParams) throws RemoteException {
        String[] s = cmdAndParams.split(" ");
        String cmd = s[0];
        String result;
        if (cmd.equals("sc")) {
            result = toolService.findClassName(s[1]);
        } else if (cmd.equals("jad")) {
            result = toolService.jadClass(s[1]);
        } else {
            System.err.println("不支持的命令:" + cmdAndParams);
            return;
        }
        System.out.println(result);
    }

}
