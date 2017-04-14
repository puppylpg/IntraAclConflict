import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by bishe2016 on 下午2:18 17-4-14.
 */
public class PolicyCheck {

    /**
     * compare IP
     * @param ipA
     * @param ipB
     * @return = or > or < or !=
     */
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

    /**
     * the result of comparision in srcIP & desIP
     * @param srcIPComp
     * @param desIPComp
     * @return >= or <= or !=
     */
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

    /**
     * check list. if policies need to be deleted, mark in valid[] and deal it at last.
     * @param policies
     */
    static void checkList(ArrayList<Policy> policies) {
        // validity to mark
        int len = policies.size();
        boolean [] valid = new boolean[len];
        for(int i=0; i < len; i++) {
            valid[i] = true;
        }

        for (int i = 0; i < policies.size(); ++i) {
            if(!valid[i]) {                                         // check validity
                continue;
            }
            Policy curPolicy = policies.get(i);
            for(int j = 0; j < i; ++j) {
                if(!valid[j]) {                                     // check validity
                    continue;
                }
                Policy prePolicy = policies.get(j);
                String preSrcIP = prePolicy.getSrc();
                String curSrcIP = curPolicy.getSrc();
                String preDesIP = prePolicy.getDes();
                String curDesIP = curPolicy.getDes();
                String preAction = prePolicy.getAction();
                String curAction = curPolicy.getAction();
                String srcIPComp = compareIP(preSrcIP, curSrcIP);
                String desIPComp = compareIP(preDesIP, curDesIP);

                String ipComp = ipComps(srcIPComp, desIPComp);      //result of comparision in srcIP & desIP

                if(ipComp.equals(Constant.COMP_BE)) {               // Shadow
                    System.out.println("SHADOW policy: " + curPolicy.toString() + " by " +
                            prePolicy.toString());
                    System.out.println("Delete latter policy: " + curPolicy.toString());
                    valid[i] = false;
                    break;
                } else if(ipComp.equals(Constant.COMP_LE)) {
                    if(preAction.equals(curAction)) {               // Redundant
                        System.out.println("REDUNDANT policy: " + prePolicy.toString() + " with "
                                + curPolicy.toString());
                        System.out.println("Delete former policy: " + prePolicy.toString());
                        valid[j] = false;
                    } else {                                        // Special case
                        System.out.println("SPECIAL_CASE policy: " + prePolicy.toString() + " of "
                                + curPolicy.toString());
                    }
                }
            }
        }

        // delete at last according to the validity
        Iterator<Policy> it = policies.iterator();
        int i = 0;
        while (it.hasNext()) {
            it.next();
            if(!valid[i++]) {
                it.remove();
            }
        }
    }

    /**
     * check list. if policies need to be deleted, recheck.
     * @param policies
     */
    static void checkListCycle(ArrayList<Policy> policies) {
        for (int i = 0; i < policies.size(); ++i) {
            Policy curPolicy = policies.get(i);
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

                if(ipComp.equals(Constant.COMP_BE)) {               // Shadow
                    System.out.println("SHADOW policy: " + curPolicy.toString() + " by " +
                                        prePolicy.toString());
                    System.out.println("Delete latter policy: " + curPolicy.toString());
                    policies.remove(i);
                    checkListCycle(policies);
                    return;
                } else if(ipComp.equals(Constant.COMP_LE)) {
                    if(preAction.equals(curAction)) {               // Redundant
                        System.out.println("REDUNDANT policy: " + prePolicy.toString() + " with "
                                            + curPolicy.toString());
                        System.out.println("Delete former policy: " + prePolicy.toString());
                        policies.remove(j);
                        checkListCycle(policies);
                        return;
                    } else {                                        // Special case
                        System.out.println("SPECIAL_CASE policy: " + prePolicy.toString() + " of "
                                            + curPolicy.toString());
                    }
                }
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
