import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Main {

    public static final String STOP_WORLD = "DONE";
    public static final int COUNT_TEXT = 10_000;
    public static final int LENGTH_TEXT = 100_000;
    // Создайте в статических полях три потокобезопасные блокирующие очереди
    public static BlockingQueue<String> queueForLetterA = new ArrayBlockingQueue<>(100);
    public static BlockingQueue<String> queueForLetterB = new ArrayBlockingQueue<>(100);
    public static BlockingQueue<String> queueForLetterC = new ArrayBlockingQueue<>(100);

    public static void main(String[] args) throws InterruptedException {
        List<Thread> threads = new ArrayList<>();

        //Создайте поток, который наполнял бы эти очереди текстами.
        Thread generateTextThread = new Thread(() -> {
            for (int i = 0; i < COUNT_TEXT; i++) {
                String text = generateText("abc", LENGTH_TEXT);
                try {
                    queueForLetterA.put(text);
                    queueForLetterB.put(text);
                    queueForLetterC.put(text);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            try {
                queueForLetterA.put(STOP_WORLD);
                queueForLetterB.put(STOP_WORLD);
                queueForLetterC.put(STOP_WORLD);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        generateTextThread.start();
        threads.add(generateTextThread);

        // Создайте по потоку для каждого из трёх символов 'a', 'b' и 'c', которые разбирали бы свою очередь и выполняли подсчёты

        Thread findMaxCountA = new Thread(() -> {
            finderMaxCountLetter(queueForLetterA, 'a');
        });
        findMaxCountA.start();
        threads.add(findMaxCountA);

        Thread findMaxCountB = new Thread(() -> {
            finderMaxCountLetter(queueForLetterB, 'b');
        });
        findMaxCountB.start();
        threads.add(findMaxCountB);

        Thread findMaxCountC = new Thread(() -> {
            finderMaxCountLetter(queueForLetterC, 'c');
        });
        findMaxCountC.start();
        threads.add(findMaxCountC);


        for (Thread thread : threads) {
            thread.join();
        }

    }

    // Поиск максимума
    public static void finderMaxCountLetter(BlockingQueue<String> queue, char letterForFind) {
        String textMax = "";
        int maxCount = 0;
        try {
            String text = null;
            while (!((text = queue.take()).equals(STOP_WORLD))) {
                int maxLocal = 0;
                for (int i = 0; i < text.length(); i++) {
                    if (text.charAt(i) == letterForFind) {
                        maxLocal++;
                    }
                }
                if (maxLocal > maxCount) {
                    maxCount = maxLocal;
                    textMax = text;
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Максимальное значение буквы \"" + letterForFind + "\" - " + maxCount +
                "\nСтрока с максимальным значением буквы \"" + letterForFind + "\": " + textMax.substring(0, 100) + "...");
    }

    // Генератор строк из условия задачи
    public static String generateText(String letters, int length) {
        Random random = new Random();
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < length; i++) {
            text.append(letters.charAt(random.nextInt(letters.length())));
        }
        return text.toString();
    }
}
