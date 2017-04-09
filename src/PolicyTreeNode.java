import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by bishe2016 on 下午8:58 17-4-6.
 */
public class PolicyTreeNode {
    String field;
    HashMap<String, PolicyTreeNode> hashMap;

    PolicyTreeNode(String field, HashMap<String, PolicyTreeNode> hashMap) {
        this.field = field;
        this.hashMap = hashMap;
    }

    String nextField(String field) {
        if(field == Constant.FIELD_SOURCE) {
            return Constant.FIELD_DESTINATION;
        }
        if(field == Constant.FIELD_DESTINATION) {
            return Constant.FIELD_ACTION;
        }
        if(field == Constant.FIELD_ACTION) {
            return Constant.FIELD_LEAF;
        }
        return null;
    }

    void constTree(Policy policy, PolicyTreeNode policyTreeNode) {
        String curField = policyTreeNode.getField();
        String context = policy.getContextByField(curField);

        System.out.println("Field: " + curField);

        if(curField == Constant.FIELD_LEAF) {
            System.out.println("-----End of constTree!-----");
            return;
        }

        String searchResult = searchPolicy(policy, policyTreeNode);

        if(searchResult.equals(Constant.REDUNDANT)) {                       // redundant found
            System.out.println("REDUNDANT policy: " + policy.toString());
        } else if(searchResult.equals(Constant.SHADOW)) {                   // shadow found
            System.out.println("SHADOW polocy: " + policy.toString());
        } else {                                                            // add to policy Tree
            PolicyTreeNode newNode = new PolicyTreeNode(nextField(curField), new HashMap<String, PolicyTreeNode>());
            System.out.println("New Node ===> context: " + context);
            policyTreeNode.hashMap.put(context, newNode);
            System.out.println("Add to HashMap.");
            System.out.println(policyTreeNode.hashMap.size() + " key-value in total!");
            constTree(policy, newNode);
        }
    }

    String searchPolicy(Policy policy, PolicyTreeNode policyTreeNode) {
        String curField = policyTreeNode.getField();
        String context = policy.getContextByField(curField);

        if(curField.equals(Constant.FIELD_ACTION)) {        // Field Action: end Recursion
            Iterator it = policyTreeNode.hashMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, PolicyTreeNode> entry = (Map.Entry<String, PolicyTreeNode>) it.next();
                String key = entry.getKey();

                if(context.equals(key)) {
                    return Constant.COMP_EQUAL;             // same Action
                } else {
                    return Constant.COMP_NOTEQUAL;          // different Action
                }
            }
        }

        Iterator it = policyTreeNode.hashMap.entrySet().iterator();
        while (it.hasNext()) {                              // can't be FIELD_ACTION || FIELD_LEAF
            Map.Entry<String, PolicyTreeNode> entry = (Map.Entry<String, PolicyTreeNode>) it.next();
            String key = entry.getKey();
            PolicyTreeNode value = entry.getValue();

            String ipComp = compareIP(key, context);               // srcIP/DesIP comparision

            if(curField.equals(Constant.FIELD_SOURCE)) {    // Field Source
                String destIPComp = searchPolicy(policy, value);
                if(ipComp.equals(Constant.COMP_BE)) {           // SrcIP: A >= B
                    if(destIPComp.equals(Constant.COMP_BE)) {       // desIP: A >= B : SHADOW
                        return Constant.SHADOW;
                    } else {                                        // desIP: A !>= B : continue
                        continue;
                    }
                }
                if(ipComp.equals(Constant.COMP_LESS)) {       // SrcIP: A < B
                    if (destIPComp.equals(Constant.COMP_LESS)) {    // desIP: A < B and Action same : REDUNDANT
                        return Constant.REDUNDANT;
                    } else {                                        // desIP: A !< B : continue
                        continue;
                    }
                }
            }

            if(curField.equals(Constant.FIELD_DESTINATION)) {//Field Destination
                if(ipComp.equals(Constant.COMP_BE)) {           // desIP: A >= B
                    return Constant.COMP_BE;
                }
                if(ipComp.equals(Constant.COMP_LESS)) {         // desIP: A < B
                    String actionComp = searchPolicy(policy, value);
                    if(actionComp.equals(Constant.COMP_EQUAL)) {    // desIP: A < B & Action: equal: LESS
                        return Constant.COMP_LESS;
                    }
                }
            }
        }

        return Constant.COMP_NOTEQUAL;      // if all the value from hashMap doesn't match
    }

    String compareIP(String ipA, String ipB) {
        String [] ipsA = ipA.split("\\.");
        String [] ipsB = ipB.split("\\.");
//        String [] flags = {Constant.COMP_EQUAL, Constant.COMP_EQUAL, Constant.COMP_EQUAL, Constant.COMP_EQUAL};
        for(int i = 0; i < 4; ++i) {
            if(ipsA[i].equals("*")) {
//                flags[i] = Constant.COMP_BE;
                return Constant.COMP_BE;
            } else if(ipsB[i].equals("*")) {
//                flags[i] = Constant.COMP_LESS;
                return Constant.COMP_LESS;
            } else if(ipsA[i].equals(ipsB[i])) {
//                flags[i] = Constant.COMP_EQUAL;
                continue;
            } else {
                return Constant.COMP_NOTEQUAL;
            }
        }

//        return judgeResult(flags, 0, Constant.COMP_EQUAL);
        return Constant.COMP_BE;                    /// Equal belongs to BE
    }

    String judgeResult(String [] flags, int i, String state) {
        if(i < flags.length) {
            String nextState = Constant.COMP_EQUAL;
            if(state.equals(Constant.COMP_EQUAL)) {                 //=, continue to compare
                nextState = flags[i];
            } else if(state.equals(Constant.COMP_BE)) {             //>, the nexts must be >
                nextState = flags[i].equals(Constant.COMP_BE) ? Constant.COMP_BE : Constant.COMP_NOTEQUAL;
            } else if(state.equals(Constant.COMP_LESS)) {           //<, the nexts must be <
                nextState = flags[i].equals(Constant.COMP_LESS) ? Constant.COMP_LESS : Constant.COMP_NOTEQUAL;
            } else if(state.equals(Constant.COMP_NOTEQUAL)) {       //!=, continue to exit
                nextState = Constant.COMP_NOTEQUAL;
            }
            return judgeResult(flags, i+1, nextState);              /// IMPORTANT: Last judgement must be returned!!!
        }
        return state;
    }

    public static void main(String [] args) {
        String fileName = "/home/bishe2016/Liu/Graduation/IdeaProjects/test/policy.txt";

        ArrayList<Policy> policies = MyReadFile.createPolicyByLine(fileName);

        PolicyTreeNode root = new PolicyTreeNode(Constant.FIELD_SOURCE, new HashMap<String, PolicyTreeNode>());

        for(Policy p : policies) {
            System.out.println("Constructing policy ===> " + p.toString());
            root.constTree(p, root);
        }

    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public HashMap<String, PolicyTreeNode> getHashMap() {
        return hashMap;
    }

    public void setHashMap(HashMap<String, PolicyTreeNode> hashMap) {
        this.hashMap = hashMap;
    }
}
