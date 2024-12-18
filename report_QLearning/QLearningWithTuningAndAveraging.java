import java.io.*;
import java.util.Arrays;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;

public class QLearningWithTuningAndAveraging {
    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);

            // パラメータの設定
            System.out.println("=== パラメータの設定 ===");
            System.out.print("εの値を入力（例: 0.5）: ");
            double epsilon = Double.parseDouble(scanner.nextLine());

            System.out.print("αの値を入力（例: 0.1）: ");
            double alpha = Double.parseDouble(scanner.nextLine());

            System.out.print("γの値を入力（例: 0.8）: ");
            double gamma = Double.parseDouble(scanner.nextLine());

            System.out.print("実行回数を入力してください: ");
            int executionCount = scanner.nextInt();

            // 最大ステップ数を入力
            System.out.print("最大ステップ数を入力してください（例: 22）: ");
            int maxSteps = scanner.nextInt();

            // 学習回数とステップ数を保存するリスト
            ArrayList<Integer> learningCounts = new ArrayList<>();
            ArrayList<Integer> stepCounts = new ArrayList<>();

            // スレッドプールの作成
            ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

            // 指定回数分実行
            ArrayList<Future<Void>> futures = new ArrayList<>();
            for (int i = 0; i < executionCount; i++) {
                final int runIndex = i; // ラムダ式で使用するための最終変数
                futures.add(executor.submit(() -> {
                    try {
                        ProcessBuilder pb = new ProcessBuilder(Arrays.asList(
                            "java", "-Dfile.encoding=UTF-8",
                            "-cp", ".;commons-lang3-3.6.jar",
                            "QLearningAsToMaze", "maze_original.dat", "1,1,8,8," + maxSteps,
                            String.format("%.2f,%.2f,%.2f", epsilon, alpha, gamma)
                        ));
                        pb.directory(new File("."));
                        Process process = pb.start();

                        BufferedReader reader = new BufferedReader(
                            new InputStreamReader(process.getInputStream(), "MS932")
                        );

                        int learningCount = 0;
                        int steps = 0;

                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (line.contains("学習回数")) {
                                Pattern pattern = Pattern.compile("学習回数：(\\d+)\\s+ゴールまでのステップ数:(\\d+)");
                                Matcher matcher = pattern.matcher(line);
                                if (matcher.find()) {
                                    learningCount = Integer.parseInt(matcher.group(1));
                                    steps = Integer.parseInt(matcher.group(2));
                                    break;
                                }
                            }
                        }

                        synchronized (learningCounts) {
                            learningCounts.add(learningCount);
                        }
                        synchronized (stepCounts) {
                            stepCounts.add(steps);
                        }

                        process.waitFor();
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                    return null;
                }));
            }

            // 全タスクの完了を待つ
            for (Future<Void> future : futures) {
                future.get();
            }
            executor.shutdown();

            // 平均値と分散の計算
            double learningAverage = calculateAverage(learningCounts);
            double learningVariance = calculateVariance(learningCounts, learningAverage);
            double stepsAverage = calculateAverage(stepCounts);
            double stepsVariance = calculateVariance(stepCounts, stepsAverage);

            // 結果の表示
            System.out.printf("%n=== 結果 ===%n");
            System.out.printf("学習回数の平均値: %.2f%n", learningAverage);
            System.out.printf("学習回数の分散: %.2f%n", learningVariance);
            System.out.printf("ステップ数の平均値: %.2f%n", stepsAverage);
            System.out.printf("ステップ数の分散: %.2f%n", stepsVariance);

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    // 平均値を計算するメソッド
    private static double calculateAverage(ArrayList<Integer> numbers) {
        if (numbers.isEmpty()) return 0.0;
        return numbers.stream().mapToDouble(d -> d).average().orElse(0.0);
    }

    // 分散を計算するメソッド
    private static double calculateVariance(ArrayList<Integer> numbers, double mean) {
        if (numbers.isEmpty()) return 0.0;
        return numbers.stream()
                .mapToDouble(d -> Math.pow(d - mean, 2))
                .average()
                .orElse(0.0);
    }
}
