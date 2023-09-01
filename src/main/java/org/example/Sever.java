package org.example;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.*;
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

        System.out.println("The message sent is: " + massage);
        System.out.println();
    }


    String getRandomID(String head) {
        Random random = new Random();
        return head + ThreadLocalRandom.current().nextLong(100000, 1000000);
    }

    public String GettingMessageSQS() {
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
            String groupID = messageBody.substring(messageBody.indexOf('?'));

            System.out.println("The Message Got Now: " + messageBody);


            if (messageBody.startsWith("NRN")) {
                String roomID = getRandomID("NRN|0|");

//                String roomID = "NRN|0|123456";
                rooms.put(roomID.substring(6), new String[]{"Waiting", null, null, null});

                String megSent = roomID + groupID;

                messageSent(megSent, "NRN", "NRN");

            } else if (messageBody.startsWith("ER")) {
                String roomID = messageBody.substring(5, messageBody.indexOf('?'));

                if (rooms.get(roomID) != null) {
                    String[] room = rooms.get(roomID);
                    if (room[1] == null) {
                        room[1] = "Waiting";
                        String segSent = "ER|1|" + roomID + "|Y" + groupID;
                        messageSent(segSent, "ER", "ER");
                        System.out.println(segSent);
                        System.out.println();
                    } else {
                        String segSent = "ER|1|" + roomID + "|N" + groupID;
                        messageSent(segSent, "ER", "ER");

                    }
                } else {
                    String segSent = "ER|1|" + roomID + "|N" + groupID;
                    messageSent(segSent, "ER", "ER");
                    System.out.println(segSent);
                    System.out.println();
                }
            } else if (messageBody.startsWith("RD")) {
                String[] room = rooms.get(messageBody.substring(5, messageBody.indexOf('?')));
                room[0] = "Ready";
                room[1] = "Ready";
                String megSent = messageBody.substring(0, 3) + "0" + messageBody.substring(4);
                messageSent(megSent, "RD", "RD");

            } else if (messageBody.startsWith("SG")) {
                String[] room = rooms.get(messageBody.substring(5, messageBody.indexOf('?')));
                room[0] = "Playing";
                room[1] = "Playing";
                String cards;
                if (room[4] == null) {
                    cards = getCardsMessage();
                    room[4] = cards;
                } else {
                    cards = room[4];
                }
                messageSent("SG|0|" + messageBody.substring(5, messageBody.indexOf('?')) + "|" + cards, "SG", "SG");

                messageSent("SG|0|" + messageBody.substring(5, messageBody.indexOf('?')) + "|", "SG", "SG");

            } else if (messageBody.startsWith("IG")) {
                String roomID = messageBody.substring(5, 11);
                String[] room = rooms.get(roomID);
                if (messageBody.charAt(3) == '0') {
                    room[0] = messageBody.substring(12, messageBody.indexOf('?'));
                    if (!room[1].equals("Playing")) {
                        messageSent("IG|0|" + roomID + "|" + room[0] + "|" + room[1], "IG", "IG");
                        messageSent("IG|1|" + roomID + "|" + room[0] + "|" + room[1], "IG", "IG");
                        room[4] = null;
                        if (getWinner(Integer.parseInt(room[0]), Integer.parseInt(room[1])) == 0) {
                            room[2] += "*";
                        } else {
                            room[3] += "*";
                        }


                    }
                } else {
                    room[1] = messageBody.substring(12, messageBody.indexOf('?'));
                    if (!room[0].equals("Playing")) {
                        messageSent("IG|0|" + roomID + "|" + room[0] + "|" + room[1], "IG", "IG");
                        messageSent("IG|1|" + roomID + "|" + room[0] + "|" + room[1], "IG", "IG");
                        room[4] = null;
                        if (getWinner(Integer.parseInt(room[0]), Integer.parseInt(room[1])) == 0) {
                            room[2] += "*";
                        } else {
                            room[3] += "*";
                        }
                    }

                }

            } else if (messageBody.startsWith("EG")) {
                rooms.clear();
                messageSent(messageBody, "EG", "EG");
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

    String getCardsMessage() {
        StringBuilder message = new StringBuilder();
        Set<Integer> cardNums = new LinkedHashSet<>();
        while (cardNums.size() < 24) {
            cardNums.add((int) (Math.random() * 32) + 1);
        }
        ArrayList<Integer> list = new ArrayList<>(cardNums);
        message.append(list.get(0));
        for (int i = 1; i < 24; i++) {
            message.append(",");
            message.append(list.get(i));
        }
        int answer1 = list.get((int) (Math.random() * 24));
        int answer2 = list.get((int) (Math.random() * 24));
        while (answer1 == answer2) {
            answer2 = list.get((int) (Math.random() * 24));
        }
        return answer1 + "#" + answer2 + "#" + message;
    }

    int getWinner(int score1, int score2) {
        if (score1 > score2) {
            return 1;
        } else if (score1 < score2) {
            return 0;
        } else {
            return 2;
        }
    }

    void deleteAllMessage() {
        SqsClient sqsClient = SqsClient.builder()
                .region(Region.US_WEST_2)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
        ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
                .queueUrl(inputUrl)
                .maxNumberOfMessages(10)
                .waitTimeSeconds(10)
                .build();
        ReceiveMessageResponse receiveResponse = sqsClient.receiveMessage(receiveRequest);
        for (Message message : receiveResponse.messages()) {
            DeleteMessageRequest deleteRequest = DeleteMessageRequest.builder()
                    .queueUrl(inputUrl)
                    .receiptHandle(message.receiptHandle())
                    .build();
            sqsClient.deleteMessage(deleteRequest);
        }

        receiveRequest = ReceiveMessageRequest.builder()
                .queueUrl(outputUrl)
                .maxNumberOfMessages(10)
                .waitTimeSeconds(10)
                .build();
        receiveResponse = sqsClient.receiveMessage(receiveRequest);
        for (Message message : receiveResponse.messages()) {
            DeleteMessageRequest deleteRequest = DeleteMessageRequest.builder()
                    .queueUrl(outputUrl)
                    .receiptHandle(message.receiptHandle())
                    .build();
            sqsClient.deleteMessage(deleteRequest);
        }
    }

}
