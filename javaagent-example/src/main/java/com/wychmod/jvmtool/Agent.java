package com.wychmod.jvmtool;

import java.lang.instrument.Instrumentation;
import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

/**
 * @description:
 * @author: wychmod
 * @date: 2025-02-08
 */
public class Agent {

    public static Instrumentation instrumentation;

    public static void agentmain(String args, Instrumentation inst) {
        instrumentation = inst;
        try {
            startRmiService(Integer.parseInt(args));
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void startRmiService(int port) throws RemoteException, MalformedURLException, AlreadyBoundException {
        ToolServiceImpl userService =new ToolServiceImpl();
        LocateRegistry.createRegistry(port);
        Naming.bind("rmi://localhost:"+port+"/ToolService", userService);
        System.out.println("rmi 已启动："+port);
    }
}
