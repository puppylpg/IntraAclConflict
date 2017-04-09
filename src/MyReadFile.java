import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by bishe2016 on 下午7:56 17-4-9.
 */
public class MyReadFile {
    /**
     * 以行为单位读取文件，常用于读面向行的格式化文件
     */
    public static ArrayList<Policy> createPolicyByLine(String fileName) {
        ArrayList<Policy> policies = new ArrayList<>();

        File file = new File(fileName);
        BufferedReader reader = null;
        try {
            System.out.println("以行为单位读取文件内容，一次读一整行：");
            reader = new BufferedReader(new FileReader(file));
            String policyLine = null;
            int line = 1;
            // 一次读入一行，直到读入null为文件结束
            while ((policyLine = reader.readLine()) != null) {
                // 显示行号
                System.out.println("line " + line + ": " + policyLine);
                line++;

                String [] policy = policyLine.split(" +");
//                for(String part : policy) {
//                    System.out.println(part);
//                }
                Policy p = new Policy(policy[0], Integer.parseInt(policy[1]), policy[2], Integer.parseInt(policy[3]), policy[4]);
                System.out.println(p);
                policies.add(p);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        return policies;
    }
}
