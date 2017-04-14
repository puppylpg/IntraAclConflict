import java.util.ArrayList;

/**
 * Created by bishe2016 on 下午2:18 17-4-14.
 */
public class PolicyCheck {

    static String compareIP(String ipA, String ipB) {
        if(ipA.equals(ipB)) {
            return Constant.COMP_EQUAL;
        }
        String [] ipsA = ipA.split("\\.");      //  IMPORTANT: REGEX, not normal string
        String [] ipsB = ipB.split("\\.");
        for(int i = 0; i < 4; ++i) {
            if(ipsA[i].equals("*")) {
                return Constant.COMP_BIGGER;
            } else if(ipsB[i].equals("*")) {
                return Constant.COMP_LESS;
            } else if(ipsA[i].equals(ipsB[i])) {
                continue;
            } else {
                return Constant.COMP_NOTEQUAL;
            }
        }
        return Constant.COMP_EQUAL;                 // useless
    }

    static String ipComps(String srcIPComp, String desIPComp) {
        if((srcIPComp.equals(Constant.COMP_BIGGER) && desIPComp.equals(Constant.COMP_BIGGER))
                || (srcIPComp.equals(Constant.COMP_EQUAL) && desIPComp.equals(Constant.COMP_EQUAL))
                || (srcIPComp.equals(Constant.COMP_BIGGER) && desIPComp.equals(Constant.COMP_EQUAL))
                || (srcIPComp.equals(Constant.COMP_EQUAL) && desIPComp.equals(Constant.COMP_BIGGER))
                ) {
            return Constant.COMP_BE;
        }
        if(((srcIPComp.equals(Constant.COMP_LESS) && desIPComp.equals(Constant.COMP_LESS)))
                || (srcIPComp.equals(Constant.COMP_EQUAL) && desIPComp.equals(Constant.COMP_LESS))
                || (srcIPComp.equals(Constant.COMP_LESS) && desIPComp.equals(Constant.COMP_EQUAL))
                ) {
            return Constant.COMP_LE;
        }
        return Constant.COMP_NOTEQUAL;
    }

    static void checkList(ArrayList<Policy> policies) {
//        Iterator<Policy> it = policies.iterator();
//        while (it.hasNext()) {
        for (int i = 0; i < policies.size(); ++i) {
            Policy curPolicy = policies.get(i);
//            Policy curPolicy = it.next();
//            Iterator<Policy> iterator = policies.iterator();
//            Policy prePolicy = iterator.next();
//            while (!curPolicy.toString().equals(prePolicy.toString())) {
            for(int j = 0; j < i; ++j) {
                Policy prePolicy = policies.get(j);
                String preSrcIP = prePolicy.getContextByField(Constant.FIELD_SOURCE);
                String curSreIP = curPolicy.getContextByField(Constant.FIELD_SOURCE);
                String preDesIP = prePolicy.getContextByField(Constant.FIELD_DESTINATION);
                String curDesIP = curPolicy.getContextByField(Constant.FIELD_DESTINATION);
                String preAction = prePolicy.getContextByField(Constant.FIELD_ACTION);
                String curAction = curPolicy.getContextByField(Constant.FIELD_ACTION);
                String srcIPComp = compareIP(preSrcIP, curSreIP);
                String desIPComp = compareIP(preDesIP, curDesIP);

                String ipComp = ipComps(srcIPComp, desIPComp);
                //Shadow
                if(ipComp.equals(Constant.COMP_BE)) {
                    System.out.println("SHADOW policy: " + curPolicy.toString() + " by " +
                                        prePolicy.toString());
                    System.out.println("Delete latter policy: " + curPolicy.toString());
                    policies.remove(i);
                    checkList(policies);
                    return;
//                    it.remove();
//                    break;
                } else if(ipComp.equals(Constant.COMP_LE)) {
                    //Redundant
                    if(preAction.equals(curAction)) {
                        System.out.println("REDUNDANT policy: " + prePolicy.toString() + " with "
                                            + curPolicy.toString());
                        System.out.println("Delete former policy: " + prePolicy.toString());
                        policies.remove(j);
                        checkList(policies);
                        return;
//                        iterator.remove();
                    } else {
                        System.out.println("SPECIAL_CASE policy: " + prePolicy.toString() + " of "
                                            + curPolicy.toString());
                    }
                }
//                prePolicy = iterator.next();
            }
        }

    }

    public static void main(String [] args) {
        String fileName = "/home/bishe2016/Liu/Graduation/IdeaProjects/IntraAclConflict/policy.txt";

        ArrayList<Policy> policies = MyReadFile.createPolicyByLine(fileName);

        checkList(policies);

        for (Policy policy : policies) {
            System.out.println(policy.toString());
        }
    }
}
