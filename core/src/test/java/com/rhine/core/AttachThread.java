package com.rhine.core;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class AttachThread extends Thread {

    private final List<VirtualMachineDescriptor> listBefore;

    private final String agentJarPath;

    private Class testClass;

    AttachThread(List<VirtualMachineDescriptor> vms, Class testClass, String agentJarPath) {
        // 记录程序启动时的 VM 集合
        listBefore = vms;
        this.testClass = testClass;
        this.agentJarPath = agentJarPath;
    }

    private static final String AGENT_JAR_PATH = "/Users/zhiyinglish/code/DEV/rhine/heck/target/heck-1.0-SNAPSHOT.jar";
    private static final String TERMINAL_JAR_PATH = "/Users/zhiyinglish/code/DEV/rhine/terminal/target/terminal-jar-with-dependencies.jar";

    public static void main(String[] args) {
        log.info(System.getProperty("user.dir"));
        List<VirtualMachineDescriptor> vmList = VirtualMachine.list();
        log.info("=========当前已经启动的JVM=========");
        for (VirtualMachineDescriptor virtualMachineDescriptor : vmList) {
            log.info("VM id = {}, name = {}", virtualMachineDescriptor.id(), virtualMachineDescriptor.displayName());
        }
        new AttachThread(vmList, TestMainJar.class, AGENT_JAR_PATH).start();
    }

    @Override
    public void run() {
        VirtualMachine vm = null;
        List<VirtualMachineDescriptor> listAfter;
        try {
            int count = 0;
            while (true) {
                listAfter = VirtualMachine.list();
                log.info("寻找新启动的JVM......");
                for (VirtualMachineDescriptor vmd : listAfter) {

                    if (!listBefore.contains(vmd)) {
                        // 如果 VM 有增加，我们就认为是被监控的 VM 启动了
                        // 这时，我们开始监控这个 VM
                        log.info("找到新启动的虚拟机 id = {}, name = {}, 附加到该虚拟机", vmd.id(), vmd.displayName());
                        if (vmd.displayName().contains(testClass.getSimpleName())) {
                            vm = VirtualMachine.attach(vmd);

                            vm.loadAgent(agentJarPath, TERMINAL_JAR_PATH);
                            vm.detach();
                            log.info("从该虚拟机中分离");
                            break;
                        }
                    }
                }
                Thread.sleep(500);
                count++;
                if (null != vm || count >= 100) {
                    break;
                }
            }
            log.info("退出");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
