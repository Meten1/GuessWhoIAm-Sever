package org.example;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class Sever {
    HashMap<String, String[]> rooms = new HashMap<>();
    // Due to AWS not allowing urls to be placed in public areas, the urls in the GitHub library have been processed but the urls in the files running locally and on the server are correct.

    String inputUrl = "https://truly URL has be hided";
    String outputUrl = "https://truly URL has be hided";
    Region position;


    /**
     * Initialize the Serve object, import the key and region to create a SQS connection client in the future.
     *
     * @param key1     The AWS key 1
     * @param key2     The AWS key 2
     * @param key3     The AWS key 3
     * @param key4     The AWS key 4
     * @param position The AWS position
     */
    public Sever(String key1, String key2, String key3, String key4, Region position) {
        System.setProperty(key1, key2);
        System.setProperty(key3, key4);
        this.position = position;
    }

    /**
     * The message sending function, used to create a connection client with Output SQS and send messages.
     *
     * @param massage   The message content
     * @param GroupHead The message's group id head
     * @param DPHead    The message's deduplication id head
     */
    void messageSent(String massage, String GroupHead, String DPHead) {
        SqsClient sqsClient = SqsClient.builder()
                .region(position)
                .build();

        SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                .queueUrl(outputUrl)
                .messageGroupId(getRandomID(GroupHead))
                .messageBody(massage)
                .messageDeduplicationId(getRandomID(DPHead))
                .build();
        System.out.println("The message sent is: " + massage + "\n");

        sqsClient.sendMessage(sendMessageRequest);

//        System.out.println("Message sent successfully.");
    }

    /**
     * The random card data generation function, used to generate random and non-repetitive card numbers and randomly select two different answers, and return card data.
     *
     * @return card data in the format of "answer1 # answer2 # cardData"
     */
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

    /**
     * Random ID function, used to obtain random IDs.
     *
     * @param head message's group id head
     * @return head+id(6 digits)
     */
    String getRandomID(String head) {
        return head + ThreadLocalRandom.current().nextLong(100000, 1000000);
    }

    /**
     * Polling message function, used to create a connection client to Input SQS and poll messages from that SQS, processing messages based on headers.
     */
    public void GettingMessageSQS() {
        SqsClient sqsClient = SqsClient.builder()
                .region(position)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();

        ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
                .queueUrl(inputUrl)
                .maxNumberOfMessages(3)
                .waitTimeSeconds(10)
                .build();

        ReceiveMessageResponse receiveResponse = sqsClient.receiveMessage(receiveRequest);

        for (Message message : receiveResponse.messages()) {
            String messageBody = message.body();
            String groupID = messageBody.substring(messageBody.indexOf('?'));

            System.out.println("The message polled is: " + messageBody);


            // NRN - New Room
            if (messageBody.startsWith("NRN")) {
                creatNewRoom(groupID);
            }

            // ER - Entry Room
            else if (messageBody.startsWith("ER")) {
                entryRoom(messageBody, groupID);
            }

            // RD - Ready
            else if (messageBody.startsWith("RD")) {
                String megSent = messageBody.substring(0, 3) + "0" + messageBody.substring(4);
                messageSent(megSent, "RD", "RD");

            }

            // SG - Start Game
            else if (messageBody.startsWith("SG")) {
                startGame(messageBody);
            }

            // IG - In Game
            else if (messageBody.startsWith("IG")) {
                inGame(messageBody);
            }

            // EG - Exit Game
            else if (messageBody.startsWith("EG")) {
                exitGame(messageBody);
            }


            // Delete processed messages
            DeleteMessageRequest deleteRequest = DeleteMessageRequest.builder()
                    .queueUrl(inputUrl)
                    .receiptHandle(message.receiptHandle())
                    .build();
            sqsClient.deleteMessage(deleteRequest);
        }

    }

    /**
     * Exit the game function, used to delete the specified room.
     *
     * @param messageBody The message's content
     */
    private void exitGame(String messageBody) {
        rooms.clear();
        messageSent(messageBody, "EG", "EG");
    }

    /**
     * In game function, used to record the number of steps taken by the user during a turn and end the turn when both users have completed the game.
     *
     * @param messageBody The input message's content
     */
    private void inGame(String messageBody) {
        String roomID = messageBody.substring(5, 11);
        String[] room = rooms.get(roomID);
        if (messageBody.charAt(3) == '0') {
            room[0] = messageBody.substring(12, messageBody.indexOf('?'));
            if (!room[1].equals("Playing")) {
                roundOver(roomID, room);
            }
        } else {
            room[1] = messageBody.substring(12, messageBody.indexOf('?'));
            if (!room[0].equals("Playing")) {
                roundOver(roomID, room);
            }
        }
    }

    /**
     * The end of turn function, used to compare the number of steps after both sides have achieved results, and the winner will be scored 1 point.
     *
     * @param roomID The input room id
     * @param room   The room
     */
    private void roundOver(String roomID, String[] room) {
        deleteAllMessage();
        messageSent("IG|0|" + roomID + "|" + room[0] + "|" + room[1], "IG", "IG");
        messageSent("IG|1|" + roomID + "|" + room[0] + "|" + room[1], "IG", "IG");
        room[4] = null;
        if (getWinner(Integer.parseInt(room[0]), Integer.parseInt(room[1])) == 0) {
            room[2] += "*";
        } else if (getWinner(Integer.parseInt(room[0]), Integer.parseInt(room[1])) == 1) {
            room[3] += "*";
        } else {
            room[2] += "*";
            room[3] += "*";
        }
    }

    /**
     * Start game function, used to change the player status of both parties in the specified room to Playing and return the card data of that room. If there is no card data yet, the getCardsMessage () function is called to generate card data and store and return it.
     *
     * @param messageBody The input message's content
     */
    private void startGame(String messageBody) {
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
        messageSent("SG|1|" + messageBody.substring(5, messageBody.indexOf('?')) + "|" + cards, "SG", "SG");
        messageSent("SG|0|" + messageBody.substring(5, messageBody.indexOf('?')) + "|" + cards, "SG", "SG");
    }

    /**
     * Entry room function, used to change player2 to waiting in the specified room in rooms (player1 has already entered the room by default when creating the room).
     *
     * @param messageBody The input message's content
     * @param oneStepID   The message One-Step-ID, used for client check the message return to whom.
     */
    private void entryRoom(String messageBody, String oneStepID) {
        String roomID = messageBody.substring(5, messageBody.indexOf('?'));
        try {
            String[] room = rooms.get(roomID);
            if (room[1] == null) {
                room[1] = "Waiting";
                String segSent = "ER|1|" + roomID + "|Y" + oneStepID;
                messageSent(segSent, "ER", "ER");
            } else {
                String segSent = "ER|1|" + roomID + "|N" + oneStepID;
                messageSent(segSent, "ER", "ER");
            }
        } catch (NullPointerException e) {
            String segSent = "ER|1|" + roomID + "|N" + oneStepID;
            System.out.println(e.getMessage());
            messageSent(segSent, "ER", "ER");
        }

    }

    /**
     * Used to create a new room in rooms and send the room number message.
     *
     * @param oneStepID The message One-Step-ID, used for client check the message return to whom.
     */
    private void creatNewRoom(String oneStepID) {
        String roomID = getRandomID("NRN|0|");
        rooms.put(roomID.substring(6), new String[]{"Waiting", null, "", "", null});
        String megSent = roomID + oneStepID;
        messageSent(megSent, "NRN", "NRN");
    }

    /**
     * Return the winner by comparing the scores of player1 and player2.
     *
     * @param score1 The player1's score
     * @param score2 The player2's score
     * @return The winner: 0 - Player1; 1 - Player2; 2 - Same score
     */
    int getWinner(int score1, int score2) {
        if (score1 > score2) {
            return 0;
        } else if (score1 < score2) {
            return 1;
        } else {
            return 2;
        }
    }

    /**
     * Delete all message functions, used to clear all residual messages before starting a new game.
     */
    void deleteAllMessage() {
        SqsClient sqsClient = SqsClient.builder()
                .region(position)
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