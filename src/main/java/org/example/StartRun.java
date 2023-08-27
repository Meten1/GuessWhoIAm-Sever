package org.example;

public class StartRun {

    public static void main(String[] args) throws InterruptedException {
        Sever service = new Sever();
        while (true) {
            service.GettingMessageSQS();
            Thread.sleep(3000);
        }
    }
}
