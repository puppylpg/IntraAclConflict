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
     * before add to the list, check conflict first.
     * @param iPsecRule
     * @param rules
     * @return
     */
    static boolean checkToAdd(Policy iPsecRule, ArrayList<Policy> rules) {

        Policy curPolicy = iPsecRule;
        Iterator<Policy> it = rules.iterator();
        while (it.hasNext()){
            Policy prePolicy = it.next();

            // Use modified IP to compare
            String preSrcIP = prePolicy.getSrc();
            String curSrcIP = curPolicy.getSrc();
            String preDesIP = prePolicy.getDes();
            String curDesIP = curPolicy.getDes();
            // -1: discard, -2: forward without process, 0: protect with IPsec
            String preAction = prePolicy.getAction();
            String curAction = curPolicy.getAction();
            String srcIPComp = compareIP(preSrcIP, curSrcIP);
            String desIPComp = compareIP(preDesIP, curDesIP);

            String ipComp = ipComps(srcIPComp, desIPComp);      //result of comparision in srcIP & desIP

            if(ipComp.equals(Constant.COMP_BE)) {               // Shadow
                // TODO: remember this infos, and alert on the web page together later
                System.out.println("SHADOW policy: " + curPolicy.toString() + " by " +
                        prePolicy.toString());
                return true;
            } else if(ipComp.equals(Constant.COMP_LE)) {
                if(preAction.equals(curAction)) {               // Redundant
                    System.out.println("REDUNDANT policy: " + prePolicy.toString() + " with "
                            + curPolicy.toString());
                    System.out.println("Delete former policy: " + prePolicy.toString());
                    it.remove();
                } else {                                        // Special case
                    System.out.println("SPECIAL_CASE policy: " + prePolicy.toString() + " of "
                            + curPolicy.toString());
                }
            }
        }

        // not conflict
        return false;
    }

    public static void main(String [] args) {
        String fileName = "/home/bishe2016/Liu/Graduation/IdeaProjects/IntraAclConflict/policy.txt";

        int flag = 2;

        ArrayList<Policy> policies = MyReadFile.createPolicyByLine(fileName);

        // if check and add in MyReadFile.java(flag = 2), this is useless
        if(flag == 1) {
            checkList(policies);
        }

        System.out.println("===============Final Policies：================");
        for (Policy policy : policies) {
            System.out.println(policy.toString());
        }
    }
}
