/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package elasticcomputing;

import static elasticcomputing.Main.isRunning;
import java.util.Queue;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author tink
 */
public class RequestQueueGenerator implements Runnable {

    public int arrivalRate;
    public int processingTime;
    Queue<Request> requestList;
    Queue<Request> completedReqList;
    Timer timer;
    int i = 0;
    Logger logger;
    int loadBalancingFactor;
    boolean isHalted = false;

    Dispatcher dsp = null;
    int servicenumber = 1;

    public RequestQueueGenerator(int arrivalRate, int processingTime, Queue<Request> requestList, int loadBalancingFactor, Logger logger, Queue<Request> completedReqList) {
        this.arrivalRate = arrivalRate;
        this.processingTime = processingTime;
        this.requestList = requestList;
        timer = new Timer();
        this.loadBalancingFactor = loadBalancingFactor;
        this.logger = logger;
        dsp = Dispatcher.getInstance(requestList, logger, loadBalancingFactor, completedReqList);
        this.completedReqList = completedReqList;
    }

    void startTimer() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runRequests();
            }
        }, 0, Main.arrivalRate);

    }

    public void changePeriod() {
        timer.cancel();
        startTimer();
    }

    @Override
    public void run() {
        System.out.println("inside web server");
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runRequests();
            }
        }, 0, Main.arrivalRate);

    }

    public void runRequests() {

        long startTime = System.nanoTime();
        Request req = new Request("request" + i, processingTime, startTime);
        if (Main.isRunning) {
            synchronized (requestList) {
                if (!isRequestQueueConsumed()) {
                    requestList.add(req);
                    logger.info("request added " + i + "-----------erquest arrival rate------- " + this.arrivalRate);
                    i++;
                } else {
                    if (!addNewService()) {
                        logger.info("Server at its max");
                    }
                }
//                try {
//                    Thread.sleep(50);
//                } catch (InterruptedException ex) {
//                    Logger.getLogger(RequestQueueGenerator.class.getName()).log(Level.SEVERE, null, ex);
//                }
            }
        } else {
            logger.fine("webserver stopped");
        }

    }

    public boolean isRequestQueueConsumed() {
        boolean isConsumed = false;
        if (requestList.size() > loadBalancingFactor) {
            if (!isHalted) {
                try {
                    Thread.sleep(500);//cooling up time to initialize the load balancer;
                } catch (InterruptedException ex) {
                    Logger.getLogger(RequestQueueGenerator.class.getName()).log(Level.SEVERE, null, ex);
                }
                isHalted = true;
            } else {
                isConsumed = true;
                isHalted = false;
            }
        }
        return isConsumed;
    }

    public synchronized boolean addNewService() {
        boolean isServiceadded = false;
        synchronized (dsp.serviceList) {
            if (dsp.serviceList.size() < Main.maxService) {
                //setServicenumber();
                Service st = new Service(10, logger, completedReqList);
                st.setServiceCount(++servicenumber);
                st.setServiceName("service" + st.getServiceCount()); //"service" + ;
                //servicenumber++;
                dsp.serviceList.add(st);
                isServiceadded = true;
            }
        }
        return isServiceadded;
    }

    public int getArrivalRate() {
        return arrivalRate;
    }

    public void setArrivalRate(int arrivalRate) {
        this.arrivalRate = arrivalRate;
    }

    public int getProcessingTime() {
        return processingTime;
    }

    public void setProcessingTime(int processingTime) {
        this.processingTime = processingTime;
    }

}
