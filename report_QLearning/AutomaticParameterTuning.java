import java.io.*;
import java.util.Arrays;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AutomaticParameterTuning {
    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);

            // パラメータの初期値と変更幅を入力
            System.out.println("=== パラメータの設定 ===");
            System.out.print("εの初期値,終了値,増加幅を入力（例: 0.1,0.9,0.2）: ");
            String[] epsilonParams = scanner.nextLine().split(",");
            double epsilonStart = Double.parseDouble(epsilonParams[0]);
            double epsilonEnd = Double.parseDouble(epsilonParams[1]);
            double epsilonStep = Double.parseDouble(epsilonParams[2]);

            System.out.print("αの初期値,終了値,増加幅を入力（例: 0.1,0.9,0.2）: ");
            String[] alphaParams = scanner.nextLine().split(",");
            double alphaStart = Double.parseDouble(alphaParams[0]);
            double alphaEnd = Double.parseDouble(alphaParams[1]);
            double alphaStep = Double.parseDouble(alphaParams[2]);

            System.out.print("γの初期値,終了値,増加幅を入力（例: 0.1,0.9,0.2）: ");
            String[] gammaParams = scanner.nextLine().split(",");
            double gammaStart = Double.parseDouble(gammaParams[0]);
            double gammaEnd = Double.parseDouble(gammaParams[1]);
            double gammaStep = Double.parseDouble(gammaParams[2]);

            // スレッドプールの作成
            ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

            // パラメータの組み合わせごとに実験を実行
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
                                    "QLearningAsToMaze", "maze_original.dat", "1,1,8,8,22",
                                    String.format("%.2f,%.2f,%.2f", e, a, g)
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

                                try (PrintWriter writer = new PrintWriter(
                                        new OutputStreamWriter(
                                            new FileOutputStream("output.csv", true),
                                            StandardCharsets.UTF_8
                                        ))) {

                                    if (!new File("output.csv").exists()) {
                                        writer.write('\ufeff');
                                    }

                                    writer.printf("ε:%.2f α:%.2f γ:%.2f 学習回数:%d ステップ数:%d%n",
                                        e, a, g, learningCount, steps);
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
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}