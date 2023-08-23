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
            // Join room
            else if (messageBody.startsWith("ER")) {
                String roomID = messageBody.substring(5);

                if (rooms.get(roomID) != null) {
                    String[] room = rooms.get(roomID);
                    if (room[1] == null) {
                        room[1] = "Waiting";
                        os.messageSent("ER|1|" + roomID + "|Y", "ER", "ER");
                        System.out.println("ER|1|" + roomID + "|Y");
                        System.out.println();
                    } else {
                        os.messageSent("ER|1|" + roomID + "|N", "ER", "ER");
                        System.out.println("ER|1|" + roomID + "|N");
                        System.out.println();
                    }
                } else {
                    os.messageSent("ER|1|" + roomID + "|N", "ER", "ER");
                    System.out.println("ER|1|" + roomID + "|N");
                    System.out.println();
                }
            }
            // Ready
            else if (messageBody.startsWith("RD")) {
                String[] room = rooms.get(messageBody.substring(5));
                if (room[0].equals("Ready")) {
                    room[1] = "Ready";
                    os.messageSent(messageBody, "RD", "RD");
                    System.out.println(messageBody);
                    System.out.println();
                } else {
                    room[0] = "Ready";
                    os.messageSent(messageBody, "RD", "RD");
                    System.out.println(messageBody);
                    System.out.println();
                }
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
