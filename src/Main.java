import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Main {
    //public static final int QUEUE_SIZE = 100;
    public static final int QUEUE_SIZE = 10; // DEBUG

    //public static final int TEXT_SIZE = 100_000;
    public static final int TEXT_SIZE = 50; // DEBUG

    //public static final int TEXT_QUANTITY = 10_000;
    public static final int TEXT_QUANTITY = 20; // DEBUG

    public static final BlockingQueue<String> queue1 = new ArrayBlockingQueue<>(QUEUE_SIZE);
    public static final BlockingQueue<String> queue2 = new ArrayBlockingQueue<>(QUEUE_SIZE);
    public static final BlockingQueue<String> queue3 = new ArrayBlockingQueue<>(QUEUE_SIZE);
    public static final List<BlockingQueue<String>> queues = Arrays.asList(
            queue1,
            queue2,
            queue3
    );

    public static void main(String[] args) throws InterruptedException {
        Thread producer = new Thread(() -> {
            for (int i = 0; i < TEXT_QUANTITY; i++) {
                String s = generateText("abc", TEXT_SIZE);
                try {
                    for (BlockingQueue<String> queue : queues) {
                        queue.put(s);
                    }
                    System.out.printf("=====> [%s]\n", s);
                } catch (InterruptedException e) {
                    return;
                }
            }
        });
        producer.start();

        List<Thread> consumers = new ArrayList<>();
        for (int i = 0; i < queues.size(); i++) {
            int idx = i;
            char c = (char)('a' + i);
            Thread consumer = new Thread(() -> {
                String maxString = "";
                int maxCount = 0;
                for (int j = 0; j < TEXT_QUANTITY; j++) {
                    try {
                        String s = queues.get(idx).take();
                        int count = countChar(s, c);
                        if (count > maxCount) {
                            maxCount = count;
                            maxString = s;
                        }
                        System.out.printf("[%c] <= [%s] - %d\n", c, s, count);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
                System.out.printf("REPORT: max count of letter %c - %d - found in the following string: %s\n", c, maxCount, maxString);
            });
            consumer.start();
            consumers.add(consumer);
        }

        for (Thread consumer : consumers) {
            consumer.join();
        }

        //producer.interrupt();
    }

    public static String generateText(String letters, int length) {
        Random random = new Random();
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < length; i++) {
            text.append(letters.charAt(random.nextInt(letters.length())));
        }
        return text.toString();
    }

    private static int countChar(String s, char c) {
        return s.length() - s.replace(String.valueOf(c), "").length();
    }
}