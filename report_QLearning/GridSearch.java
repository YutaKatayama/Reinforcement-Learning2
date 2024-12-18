// 値を指定してグリッドサーチを行うプログラム
import java.io.*;
import java.util.Arrays;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GridSearch {
    public static void main(String[] args) {
        // パラメータの範囲を設定
        double[] epsilonValues = {0.1, 0.3, 0.5, 0.7, 0.9};
        double[] alphaValues = {0.1, 0.3, 0.5, 0.7, 0.9};
        double[] gammaValues = {0.1, 0.3, 0.5, 0.7, 0.9};

        // 最大ステップ数を固定値として設定
        int maxSteps = 14;

        // 結果を保存するCSVファイルの準備
        try (PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(
                    new FileOutputStream("grid_search_results.csv", true),
                    StandardCharsets.UTF_8
                ))) {
            // CSVファイルのヘッダー
            writer.write('\ufeff'); // BOMの追加
            writer.println("ε,α,γ,学習回数,ステップ数");

            // グリッドサーチの実行
            for (double epsilon : epsilonValues) {
                for (double alpha : alphaValues) {
                    for (double gamma : gammaValues) {
                        System.out.printf("=== 実行: ε=%.2f, α=%.2f, γ=%.2f ===%n", epsilon, alpha, gamma);

                        // QLearningAsToMazeを実行
                        ProcessBuilder pb = new ProcessBuilder(Arrays.asList(
                            "java", "-Dfile.encoding=UTF-8",
                            "-cp", ".;commons-lang3-3.6.jar",
                            "QLearningAsToMaze", "maze_original.dat", "1,1,8,8," + maxSteps,
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

                        // 結果をCSVに出力
                        writer.printf("%.2f,%.2f,%.2f,%d,%d%n", epsilon, alpha, gamma, learningCount, steps);

                        process.waitFor();
                        System.out.println("実行完了");
                    }
                }
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}