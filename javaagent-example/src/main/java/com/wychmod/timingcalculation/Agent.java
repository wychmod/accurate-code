package com.wychmod.timingcalculation;

import java.lang.instrument.Instrumentation;

/**
 * @description:
 * @author: wychmod
 * @date: 2025-02-11
 */
public class Agent {

    public static void premain(String args, Instrumentation instrumentation) {
        instrumentation.addTransformer(new ServiceAgentCopy());
    }
}
