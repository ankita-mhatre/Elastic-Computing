/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package elasticcomputing;

/**
 *
 * @author tink
 */
public class Request {
    public String request_Name;
    public int processingTime;
    public long startTime;
    public double completionTime;    
    public boolean isDone;
    public String serviceName;
    //public int comingTime;

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public double getCompletionTime() {
        return completionTime;
    }

    public Request(String request_Name, int processingTime, long startTime) {
        this.request_Name = request_Name;
        this.processingTime = processingTime;
        this.startTime = startTime;
        isDone = false;
    }

    public String getRequest_Name() {
        return request_Name;
    }

    public void setRequest_Name(String request_Name) {
        this.request_Name = request_Name;
    }

    public int getProcessingTime() {
        return processingTime;
    }

    public void setProcessingTime(int processingTime) {
        this.processingTime = processingTime;
    }

    
}
