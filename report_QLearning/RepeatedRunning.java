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

public class RepeatedRunning {
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
                    System.out.printf("%n=== 実行 %d 回目: ε=%.2f, α=%.2f, γ=%.2f ===%n", 
                        runIndex + 1, epsilon, alpha, gamma);

                    ProcessBuilder pb = new ProcessBuilder(Arrays.asList(
                        "java", "-Dfile.encoding=UTF-8",
                        "-cp", ".;commons-lang3-3.6.jar",
                        "QLearningAsToMaze", "maze_original.dat", "1,1,8,8,22",
                        String.format("%.2f,%.2f,%.2f", epsilon, alpha, gamma)
                    ));
                    
                    pb.directory(new File("."));
                    pb.environment().put("JAVA_TOOL_OPTIONS", "-Dfile.encoding=UTF-8");
                    Process process = pb.start();

                    BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream(), "MS932")
                    );

                    int learningCount = 0;
                    int steps = 0;

                    String line;
                    boolean foundOptimal = false;
                    
                    while ((line = reader.readLine()) != null) {
                        if (line.contains("最適ルートを獲得")) {
                            foundOptimal = true;
                            continue;
                        }
                        
                        if (foundOptimal && line.contains("学習回数")) {
                            Pattern pattern = Pattern.compile("学習回数：(\\d+)\\s+ゴールまでのステップ数:(\\d+)");
                            Matcher matcher = pattern.matcher(line);
                            if (matcher.find()) {
                                learningCount = Integer.parseInt(matcher.group(1));
                                steps = Integer.parseInt(matcher.group(2));
                                break;
                            }
                        }
                    }

                    // 結果をリストに追加
                    synchronized (learningCounts) {
                        learningCounts.add(learningCount);
                    }
                    synchronized (stepCounts) {
                        stepCounts.add(steps);
                    }

                    // 結果をCSVに出力
                    try (PrintWriter writer = new PrintWriter(
                            new OutputStreamWriter(
                                new FileOutputStream("output.csv", true),
                                StandardCharsets.UTF_8
                            ))) {
                        
                        if (!new File("output.csv").exists()) {
                            writer.write('\ufeff');
                        }

                        writer.printf("実行%d回目 ε:%.2f α:%.2f γ:%.2f 学習回数:%d ステップ数:%d%n",
                            runIndex + 1, epsilon, alpha, gamma, learningCount, steps);
                    }

                    process.waitFor();
                    System.out.println("実行完了");
                    return null;
                }));
            }

            // 全てのタスクが完了するのを待つ
            for (Future<Void> future : futures) {
                future.get();
            }

            executor.shutdown();

            // 分散の計算と表示
            double learningVariance = calculateVariance(learningCounts);
            double stepsVariance = calculateVariance(stepCounts);

            System.out.printf("%n=== 分散の結果 ===%n");
            System.out.printf("学習回数の分散: %.2f%n", learningVariance);
            System.out.printf("ステップ数の分散: %.2f%n", stepsVariance);

        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    // 分散を計算するメソッド
    private static double calculateVariance(ArrayList<Integer> numbers) {
        double mean = numbers.stream().mapToDouble(d -> d).average().orElse(0.0);
        double variance = numbers.stream()
            .mapToDouble(d -> Math.pow(d - mean, 2))
            .average()
            .orElse(0.0);
        return variance;
    }
}