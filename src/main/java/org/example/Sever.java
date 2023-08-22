package org.example;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Sever {
    HashMap<String, String[]> rooms = new HashMap<>();

    // Due to AWS not allowing urls to be placed in public areas, the urls in the GitHub library have been processed but the urls in the files running locally and on the server are correct.

    String inputUrl = "https://truly URL has be hided";
    String outputUrl = "https://truly URL has be hided";

    // Due to AWS not allowing keys to be placed in public areas, the keys in the GitHub library have been processed but the keys in the files running locally and on the server are correct.
    Sever() {
        System.setProperty("aws.accessKeyId", "truly key has be hided");
        System.setProperty("aws.secretAccessKey", "truly key has be hided");
    }

    void messageSent(String massage, String groupHead, String dPHead) {
        SqsClient sqsClient = SqsClient.builder()
                .region(Region.US_WEST_2)
                .build();
//        "Deduplication"
        SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                .queueUrl(outputUrl)
                .messageGroupId(getRandomID(groupHead))
                .messageBody(massage)
                .messageDeduplicationId(dPHead)
                .build();

        sqsClient.sendMessage(sendMessageRequest);

        System.out.println("Message sent successfully.");
    }


    String getRandomID(String head) {
        Random random = new Random();
        return head + ThreadLocalRandom.current().nextLong(100000, 1000000);
    }

    public String GettingMessageSQS(Sever os) {
        SqsClient sqsClient = SqsClient.builder()
                .region(Region.US_WEST_2)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();

        ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
                .queueUrl(inputUrl)
                .maxNumberOfMessages(10) // set poll max messages number
                .waitTimeSeconds(20) // set poll long waiting time
                .build();

        ReceiveMessageResponse receiveResponse = sqsClient.receiveMessage(receiveRequest);
        String returnInfor = "";
        for (Message message : receiveResponse.messages()) {
            String messageBody = message.body();


            // New room
            if (messageBody.startsWith("NRN")) {
                String roomID = getRandomID("NRN|0|");
                // test room id
//                String roomID = "NRN|0|123456";
                rooms.put(roomID.substring(6), new String[]{"Waiting", null, null, null});
                os.messageSent(roomID, "NRN", "NRN");
                System.out.println(roomID);
                System.out.println();
            }

            returnInfor = messageBody;
            System.out.println("Received message: " + messageBody);

            // delete this message
            DeleteMessageRequest deleteRequest = DeleteMessageRequest.builder()
                    .queueUrl(inputUrl)
                    .receiptHandle(message.receiptHandle())
                    .build();
            sqsClient.deleteMessage(deleteRequest);
        }
        return returnInfor;
    }


}
