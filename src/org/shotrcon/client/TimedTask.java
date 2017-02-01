/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shotrcon.client;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author shotbygun
 */
public class TimedTask {
    
    private ScheduledExecutorService executorService;
    private Runnable runnable;
    private ScheduledFuture task;
    
    public TimedTask(Runnable runnable) {
        this.runnable = runnable;
        executorService = Executors.newSingleThreadScheduledExecutor();
        
    }
    
    public void start() {
        task = executorService.schedule(runnable, 60, TimeUnit.SECONDS);
    }
    
    public void stop() {
        if(task != null)
            task.cancel(false);
    }
    
    public void resetTimer() {
        stop();
        start();
    }
}
