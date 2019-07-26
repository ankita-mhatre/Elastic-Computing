/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package elasticcomputing;

import java.util.AbstractList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author tink
 */
public class Dispatcher implements Runnable {

    Queue<Request> requestList;
    Queue<Request> completedReqList;
    Queue<Thread> serThread;
    private static int[] serverSizes = null;
    List<Service> serviceList;
    //static int servicecount = 1;
    static int loadBalancingFactor;
    Logger logger;
    boolean isSerEmpty;
    boolean isDis;
    private Timer dispatchTimer = new Timer();
    private int dispatchPeriod = 2; //despatch period is 2ms
    private int inProcCount = 0;
    private int nodeQueueMaxSize = 10;
    private static Dispatcher instance = null;

    public Dispatcher(Queue<Request> requestList, Logger logger, int loadBalancingFactor, Queue<Request> completedReqList) {
        this.loadBalancingFactor = loadBalancingFactor;
        this.requestList = requestList;
        this.completedReqList = completedReqList;
        serThread = new LinkedList<>();
        serviceList = new LinkedList<>();
        this.logger = logger;
        isSerEmpty = false;
        //this.isDis = isDis;
        Service ser = new Service(10, logger, completedReqList);
        ser.serviceName = "service1";
        serverSizes = new int[Main.maxService];
        // Main.servStaticQueue.add(ser);
        serviceList.add(ser);
    }

    public static Dispatcher getInstance(Queue<Request> requestList, Logger logger, int loadBalancingFactor, Queue<Request> completedReqList) {
        if (instance == null) {
            instance = new Dispatcher(requestList, logger, loadBalancingFactor, completedReqList);
        }
        return instance;
    }

//    public int getServicecount() {
//        return servicecount;
//    }
    public List<Service> getServiceList() {
        return serviceList;
    }

    public int getInProcCount() {
        inProcCount = 0;
        for (int i = 0; i < serviceList.size(); i++) {
            inProcCount += serviceList.get(i).serReqQueue.size();
        }

        return inProcCount;
    }

    public int[] getServerSizes() {
        for (int i = 0; i < serviceList.size(); i++) {
            serverSizes[i] = serviceList.get(i).serReqQueue.size();
        }
        return serverSizes;
    }

    @Override
    public void run() {

        dispatchTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                //synchronized (requestList) {
                while (!requestList.isEmpty()) {
                    // check if any request in requestlist is empty
                    //boolean isadded = false;
                    int index = 0;
                    int lowestval = Integer.MAX_VALUE;
                    synchronized (serviceList) {
                        for (int i = 0; i < serviceList.size(); i++) {

                            if (serviceList.get(i).serReqQueue.size() < lowestval) {
                                lowestval = serviceList.get(i).serReqQueue.size();
                                index = i;
                            }
                        }

                        if (serviceList.get(index).serReqQueue.size() < nodeQueueMaxSize) {
                            System.out.println("request list size in dispatcher" + requestList.size());
                            Request req = requestList.poll();
                            //System.out.println("------request procxessing rate ------------" + req.processingTime);
                            serviceList.get(index).serReqQueue.add(req);
                            // isadded = true;
                        }

//                    if (!isadded) {
//                        Service st = new Service("service" + (++servicecount), 10, logger, completedReqList);
//
//                        serviceList.add(st);
//                        synchronized (st.serReqQueue) {
//                            st.serReqQueue.add(requestList.poll());
//                        }
//                    }
                        for (int i = 0; i < serviceList.size(); i++) {
                            synchronized (serviceList) {
                                if (serviceList.get(i).serReqQueue.isEmpty()) {
                                    System.out.println("_______________ removing request from service queue ___________");
                                    serviceList.remove(i);
                                }
                            }
                        }

                        startService();
                    }
                }
            }
         ;
        }, dispatchPeriod, dispatchPeriod);
    }

    void startService() {
        try {
            //logger.info("inside start service " + serviceList.size());
            for (int i = 0; i < serviceList.size(); i++) {

                Service s = serviceList.get(i);
                if (!s.isRunning) {
                    Thread th = new Thread(s);
                    System.out.println("new " + i + " service started");
                    logger.info("new " + i + " service started");

                    logger.info("service list from start service " + s.serReqQueue.size());
                    serThread.add(th);
                    th.start();
                    //th.join();
                    s.isRunning = true;
                }

            }
        } catch (Exception ex) {
            System.out.println("assignment2.Dispatcher.methodName()");
        }
    }

    void stopService() {
        while (true) {

            //logger.info("inside Dispatcher stop service");
            if (!Main.isRunning && isSerEmpty) {
                for (Thread tser : serThread) {
                    tser.suspend();
                    //logger.info(tser.getName() + "\t syspended");
                }
            }
            if (!Main.isRunning) {
                // logger.warning("inside to check if service is empty");
                isServiceEmpty();
                break;
            }
        }
    }

    void isServiceEmpty() {
        //  logger.info("I am checking");
        // for (Service s : Main.servStaticQueue) {
//        for (int i = 0; i < serviceList.size(); i++) {
//            if (serviceList.get(i).serReqQueue.isEmpty()) {
//                // logger.info("service with empty request queue");
//                serviceList.remove(i);
//                isSerEmpty = true;
//            } else {
//                isSerEmpty = false;
//            }
//        }
        for (Service s : serviceList) {
            // logger.info("size of  request queue " + requestList.size());
            //logger.info("size of service queue " + s.serReqQueue.size());
            if (s.serReqQueue.isEmpty()) {
                // logger.info("service with empty request queue");
                isSerEmpty = true;
            } else {
                isSerEmpty = false;
            }
        }
//        return isSerEmpty;
    }

    public void stopDispatchingRequests() {
        serviceList.clear();
        dispatchTimer.cancel();
        dispatchTimer = null;
        dispatchTimer = new Timer();
    }
}
