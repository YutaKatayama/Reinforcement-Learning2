import java.io.*;
import java.util.Arrays;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ForLerningStepsPlot {
    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);

            // パラメータの範囲を入力
            System.out.println("=== パラメータの設定 ===");
            System.out.print("εの値の範囲（例: 0.1,0.9,0.1）: ");
            String[] epsilonParams = scanner.nextLine().split(",");
            double epsilonStart = Double.parseDouble(epsilonParams[0]);
            double epsilonEnd = Double.parseDouble(epsilonParams[1]);
            double epsilonStep = Double.parseDouble(epsilonParams[2]);

            System.out.print("αの値の範囲（例: 0.1,0.9,0.1）: ");
            String[] alphaParams = scanner.nextLine().split(",");
            double alphaStart = Double.parseDouble(alphaParams[0]);
            double alphaEnd = Double.parseDouble(alphaParams[1]);
            double alphaStep = Double.parseDouble(alphaParams[2]);

            System.out.print("γの値の範囲（例: 0.1,0.9,0.1）: ");
            String[] gammaParams = scanner.nextLine().split(",");
            double gammaStart = Double.parseDouble(gammaParams[0]);
            double gammaEnd = Double.parseDouble(gammaParams[1]);
            double gammaStep = Double.parseDouble(gammaParams[2]);

            // 最大ステップ数を入力
            System.out.print("最大ステップ数を入力してください（例: 1000）: ");
            int maxSteps = Integer.parseInt(scanner.nextLine());

            // スレッドプールの作成
            ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

            // グリッドサーチの実行
            for (double epsilon = epsilonStart; epsilon <= epsilonEnd; epsilon += epsilonStep) {
                for (double alpha = alphaStart; alpha <= alphaEnd; alpha += alphaStep) {
                    for (double gamma = gammaStart; gamma <= gammaEnd; gamma += gammaStep) {
                        final double e = epsilon;
                        final double a = alpha;
                        final double g = gamma;

                        executor.submit(() -> {
                            System.out.printf("%n=== パラメータ: ε=%.2f, α=%.2f, γ=%.2f ===%n", e, a, g);

                            try {
                                ProcessBuilder pb = new ProcessBuilder(Arrays.asList(
                                    "java", "-Dfile.encoding=UTF-8",
                                    "-cp", ".;commons-lang3-3.6.jar",
                                    "QLearningAsToMaze", "maze_original.dat", "1,1,8,8," + maxSteps,
                                    String.format("%.2f,%.2f,%.2f", e, a, g)
                                ));

                                pb.directory(new File("."));
                                pb.environment().put("JAVA_TOOL_OPTIONS", "-Dfile.encoding=UTF-8");
                                Process process = pb.start();

                                BufferedReader reader = new BufferedReader(
                                    new InputStreamReader(process.getInputStream(), "MS932")
                                );

                                // 結果をForLerningStepsPlot.csvに出力
                                try (PrintWriter writer = new PrintWriter(
                                        new OutputStreamWriter(
                                            new FileOutputStream("ForLerningStepsPlot.csv", true),
                                            StandardCharsets.UTF_8
                                        ))) {

                                    if (!new File("ForLerningStepsPlot.csv").exists()) {
                                        writer.write('\ufeff'); // BOMを追加
                                    }

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
                                                int learningCount = Integer.parseInt(matcher.group(1));
                                                int steps = Integer.parseInt(matcher.group(2));
                                                // 各学習回数とステップ数を記録
                                                writer.printf("パラメータ: ε=%.2f, α=%.2f, γ=%.2f, 学習回数=%d, ステップ数=%d%n",
                                                    e, a, g, learningCount, steps);
                                            }
                                        } else if (foundOptimal) {
                                            // 学習回数とステップ数の過程を記録
                                            Pattern pattern = Pattern.compile("学習回数：(\\d+) ゴールまでのステップ数:(\\d+)");
                                            Matcher matcher = pattern.matcher(line);
                                            if (matcher.find()) {
                                                int learningCount = Integer.parseInt(matcher.group(1));
                                                int steps = Integer.parseInt(matcher.group(2));
                                                writer.printf("パラメータ: ε=%.2f, α=%.2f, γ=%.2f, 学習回数=%d, ステップ数=%d%n",
                                                    e, a, g, learningCount, steps);
                                            }
                                        }
                                    }
                                }

                                process.waitFor();
                                System.out.println("実行完了");
                            } catch (IOException | InterruptedException e1) {
                                e1.printStackTrace();
                            }
                        });
                    }
                }
            }

            executor.shutdown();
            while (!executor.isTerminated()) {
                // Wait for all tasks to finish
            }kura

        } catch (Exception e) { // IOExceptionを含むすべての例外をキャッチ
            e.printStackTrace();
        }
    }
}