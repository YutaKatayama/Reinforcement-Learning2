import java.io.*;
import java.util.Arrays;

public class Nomal {
    public static void main(String[] args) {
        try {
            // 引数の確認
            if (args.length < 4) {
                System.out.println("使用方法: java Nomal <maze_file> <start_x,start_y> <goal_x,goal_y> <max_steps> <epsilon,alpha,gamma>");
                return;
            }

            // 引数の取得
            String mazeFile = args[0];
            String start = args[1];
            String goal = args[2];
            int maxSteps = Integer.parseInt(args[3]);
            String parameters = args[4];

            // QLearningAsToMazeを実行
            ProcessBuilder pb = new ProcessBuilder(Arrays.asList(
                "java", "-Dfile.encoding=UTF-8",
                "-cp", ".;commons-lang3-3.6.jar",
                "QLearningAsToMaze", mazeFile, start + "," + goal + "," + maxSteps, parameters
            ));

            pb.directory(new File("."));
            pb.environment().put("JAVA_TOOL_OPTIONS", "-Dfile.encoding=UTF-8");
            Process process = pb.start();

            // 結果を読み取る
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "MS932"));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}