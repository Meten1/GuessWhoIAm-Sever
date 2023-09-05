package org.example;

import software.amazon.awssdk.regions.Region;

public class StartRun {

    public static void main(String[] args) throws InterruptedException {
        // Due to AWS not allowing keys to be placed in public areas, the keys in the GitHub library have been processed but the keys in the files running locally and on the server are correct.
        Sever service = new Sever("truly keys has be hided", "truly keys has be hided", "truly keys has be hided", "truly keys has be hided", Region.US_WEST_2);
        while (true) {
            service.GettingMessageSQS();
            Thread.sleep(3000);
        }
    }
}
