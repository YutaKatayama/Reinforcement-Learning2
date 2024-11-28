import java.io.*;
import java.util.Arrays;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class H_T_A {
    public static void main(String[] args) {
        // パラメータの値を設定（0.0から1.0まで0.5刻み）
        double[] values = {0.0, 0.5, 1.0};
        int totalCombinations = values.length * values.length * values.length;
        int currentCombination = 0;

        try {
            // 出力ファイルの準備
            File outputFile = new File("parameter_search_results.csv");
            PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(
                    new FileOutputStream(outputFile),
                    StandardCharsets.UTF_8
                )
            );
            
            // BOMを追加
            writer.write('\ufeff');
            writer.println("=== パラメータ探索の結果 ===");
            writer.println("全組み合わせ数: " + totalCombinations);
            writer.println();
            writer.flush();

            // すべての組み合わせを試行
            for (double epsilon : values) {
                for (double alpha : values) {
                    for (double gamma : values) {
                        currentCombination++;
                        
                        // 進捗状況を表示
                        System.out.printf("[%d/%d] 実行中: ε=%.1f, α=%.1f, γ=%.1f%n",
                            currentCombination, totalCombinations, epsilon, alpha, gamma);

                        String paramString = String.format("%.1f,%.1f,%.1f", epsilon, alpha, gamma);
                        
                        // Q学習を実行
                        ProcessBuilder pb = new ProcessBuilder(
                            "java",
                            "-cp", ".;commons-lang3-3.6.jar",
                            "QLearningAsToMaze",
                            "maze_original.dat",
                            "1,1,8,8,22",
                            paramString
                        );
                        
                        pb.directory(new File("."));
                        Process process = pb.start();

                        // 結果を読み取り
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
                                }
                            }
                        }

                        // 結果を出力
                        String result = String.format("ε:%.1f α:%.1f γ:%.1f 学習回数:%d ステップ数:%d",
                            epsilon, alpha, gamma, learningCount, steps);
                        writer.println(result);
                        writer.flush();

                        // プロセスの終了を待つ
                        process.waitFor();
                        reader.close();
                        
                        // 各実行の間に少し待機
                        Thread.sleep(1000);
                    }
                }
            }

            writer.println("\n=== 探索完了 ===");
            writer.close();
            
            System.out.println("\n全パラメータの組み合わせでの実行が完了しました。");
            System.out.println("結果は parameter_search_results.csv に保存されました。");

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}