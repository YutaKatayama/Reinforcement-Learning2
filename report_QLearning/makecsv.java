import java.io.*;
import java.util.Arrays;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MakeCSV {
    public static void main(String[] args) {
        try {
            String command = "java -cp \".;commons-lang3-3.6.jar\" QLearningAsToMaze maze_original.dat \"1,1,8,8,22\" \"0.5,0.5,0.5\"";
            
            ProcessBuilder pb = new ProcessBuilder(Arrays.asList(
                "java", "-Dfile.encoding=UTF-8",
                "-cp", ".;commons-lang3-3.6.jar",
                "QLearningAsToMaze", "maze_original.dat", "1,1,8,8,22", "0.5,0.5,0.5"
            ));
            
            pb.directory(new File("."));
            pb.environment().put("JAVA_TOOL_OPTIONS", "-Dfile.encoding=UTF-8");
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), "MS932")
            );

            String epsilon = "0.5";
            String alpha = "0.5";
            String gamma = "0.5";
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

            // UTF-8でファイルを出力
            try (PrintWriter writer = new PrintWriter(
                    new OutputStreamWriter(
                        new FileOutputStream("output.csv", true),
                        StandardCharsets.UTF_8
                    ))) {
                
                // BOMを追加
                if (!new File("output.csv").exists()) {
                    writer.write('\ufeff');
                }

                // 結果を出力
                writer.printf("ε:%s α:%s γ:%s 学習回数:%d ステップ数:%d%n",
                    epsilon, alpha, gamma, learningCount, steps);
            }

            process.waitFor();
            System.out.println("実行結果をoutput.csvに追記しました。");

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}