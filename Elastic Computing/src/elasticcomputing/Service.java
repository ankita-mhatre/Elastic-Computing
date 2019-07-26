
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package elasticcomputing;

import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author tink
 */
public class Service implements Runnable {

    public Queue<Request> serReqQueue;
    public Queue<Request> completedReqList;
      int ServiceCount;
   String serviceName;
    double totalProcessingTime;
    int requestCount;
    float avgRate;
    int capacity;
    boolean isRunning;
    Logger logger;

    public int getServiceCount() {
        return ServiceCount;
    }

    public void setServiceCount(int ServiceCount) {
        this.ServiceCount = ServiceCount;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Service( int capacity, Logger logger, Queue<Request> completedReqList) {
      //  this.serviceName = serviceName;
        this.completedReqList = completedReqList;
        this.serReqQueue = new LinkedList<>();
        this.capacity = capacity;
        isRunning = false;
        totalProcessingTime = 0;
        requestCount = 0;
        avgRate = 0;
        this.logger = logger;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException ex) {
                Logger.getLogger(Service.class.getName()).log(Level.SEVERE, null, ex);
            }
            synchronized (this.serReqQueue) {

                while (!serReqQueue.isEmpty()) {
                    // logger.info("serReqQueue.size() " + serReqQueue.size());
                    Request req = serReqQueue.peek();
                    try {
                        Thread.sleep(req.processingTime);
                        long endTime = System.nanoTime();
                        double timeElapsed = endTime - (req.getStartTime());
                        timeElapsed = timeElapsed / 1000000000; // in secs
                        System.out.println(timeElapsed + " [[[[[[[[[[[[[[[[[[ Elaspsed time in secs ]]]]]]]]]]]]]]]]]]" + serviceName);

                        totalProcessingTime += timeElapsed;
                        requestCount++;
                        avgRate = (float) (totalProcessingTime / requestCount);
                        req.serviceName = this.serviceName;
//                        System.out.println(timeElapsed + " [[[[[[[[[[[[[[[[[[----- Avg speed in speed/secs-----]]]]]]]]]]]]]]]]]]" + serviceName);
                        //  serReqQueue.notifyAll();     

//                        logger.info(this.serviceName + "  completed execution of " + req.request_Name + " completed in: " + ((endTime - req.startTime) / 1000000000));
                        //logger.info(this.serviceName + "  completed execution of " + req.request_Name + " completed in: " + ((completionTime - req.startTime) / 1000000000));
                        synchronized (completedReqList) {
                            completedReqList.add(req);
                        }
                        serReqQueue.remove();

                    } catch (InterruptedException ex) {
                        Logger.getLogger(Service.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }

}
