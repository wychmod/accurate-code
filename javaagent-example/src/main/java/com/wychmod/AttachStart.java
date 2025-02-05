package com.wychmod;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

/**
 * @description: javaagent attach示例
 * @author: wychmod
 * @date: 2025-02-05
 */
public class AttachStart {

    /**
     * 主函数，用于列出JVM进程并选择一个进程附加agent
     * @param args 命令行参数
     * @throws Exception 当进程附加或agent加载过程中出现异常时抛出
     */
    public static void main(String[] args) throws Exception {
        // 获取jvm进程列表
        List<VirtualMachineDescriptor> list = VirtualMachine.list();
        for (int i = 0; i < list.size(); i++) {
            // 显示进程编号和名称，便于用户选择
            System.out.println(String.format("[%s] %s", i, list.get(i).displayName()));
        }
        System.out.println("输入数字指定要attach的进程");

        // 选择jvm进程
        BufferedReader read = new BufferedReader(new InputStreamReader(System.in));
        String line = read.readLine();
        int i = Integer.parseInt(line);
        // 附着agent
        VirtualMachine virtualMachine = VirtualMachine.attach(list.get(i));
        // 加载agent jar，此处应替换为实际的agent jar路径
        virtualMachine.loadAgent("D:\\idea\\accurate-code\\javaagent-example\\target\\javaagent-example-1.0-SNAPSHOT.jar","111");
        // 从目标进程分离
        virtualMachine.detach();
        System.out.println("加载成功");
    }
}
