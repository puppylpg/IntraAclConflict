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

        // 1: add all policies to ArrayList, then check
        // 2: check the policy, then add to ArrayList
        int flag = 2;

        File file = new File(fileName);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String policyLine = null;
            int line = 1;
            while ((policyLine = reader.readLine()) != null) {
                System.out.println("line " + line + ": " + policyLine);
                line++;

                String [] policy = policyLine.split(" +");          // split according to ' '(or more)
//                for(String part : policy) {
//                    System.out.println(part);
//                }
                Policy p = new Policy(policy[0], Byte.parseByte(policy[1]), policy[2], Byte.parseByte(policy[3]), policy[4]);
                System.out.println(p);

                if(flag == 1) {
                    policies.add(p);
                } else {
                    if (!PolicyCheck.checkToAdd(p, policies)) {
                        policies.add(p);
                        System.out.println("Add policy " + p.toString());
                    } else {
                        System.out.println("Conflicts, don't Add!");
                    }
                }
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
