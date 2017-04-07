package cn;

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
        boolean branchExist = false;

        System.out.println("Field: " + curField);

        if(curField == Constant.FIELD_LEAF) {
            System.out.println("-----End of constTree!-----");
            return;
        }

        Iterator it = policyTreeNode.hashMap.entrySet().iterator();
        while (it.hasNext()) {                                          //can't be FIELD_ACTION || FIELD_LEAF
            Map.Entry<String, PolicyTreeNode> entry = (Map.Entry<String, PolicyTreeNode>) it.next();
            String key = entry.getKey();
            PolicyTreeNode value = entry.getValue();

            String ipComp = compareIP(context, key);
            if(ipComp.equals(Constant.COMP_EQUAL) || ipComp.equals(Constant.COMP_BE)) { //if A >= B
                branchExist = true;
                System.out.println("context found: " + context);
                System.out.println(policyTreeNode.hashMap.size() + " key-value in total!");
                constTree(policy, value);
            }
        }

        if(!branchExist) {
            PolicyTreeNode newNode = new PolicyTreeNode(nextField(curField), new HashMap<String, PolicyTreeNode>());
            System.out.println("New Node ===> context: " + context);
            policyTreeNode.hashMap.put(context, newNode);
            System.out.println("Add to HashMap.");
            System.out.println(policyTreeNode.hashMap.size() + " key-value in total!");
            constTree(policy, newNode);
        }
    }

    String compareIP(String ipA, String ipB) {
        String [] ipsA = ipA.split(".");
        String [] ipsB = ipB.split(".");
        String [] flags = {Constant.COMP_EQUAL, Constant.COMP_EQUAL, Constant.COMP_EQUAL, Constant.COMP_EQUAL};
        for(int i = 0; i < 4; ++i) {
            if(ipsA[i].equals("*")) {
                flags[i] = Constant.COMP_BE;
            } else if(ipsB[i].equals("*")) {
                flags[i] = Constant.COMP_LESS;
            } else if(ipsA[i].equals(ipsB[i])) {
                flags[i] = Constant.COMP_EQUAL;
            }
        }

        return judgeResult(flags, 0, Constant.COMP_EQUAL);
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
            judgeResult(flags, i+1, nextState);
        }
        return state;
    }

    public static void main(String [] args) {
        Policy policy1 = new Policy("hello", "world", Constant.ACTION_PROTECTED);
        Policy policy2 = new Policy("hello1", "world1", Constant.ACTION_PROTECTED);
        Policy policy3 = new Policy("hello", "world2", Constant.ACTION_PROTECTED);
        Policy policy4 = new Policy("hello", "world2", Constant.ACTION_DENY);

        PolicyTreeNode root = new PolicyTreeNode(Constant.FIELD_SOURCE, new HashMap<String, PolicyTreeNode>());
        root.constTree(policy1, root);
        root.constTree(policy2, root);
        root.constTree(policy3, root);
        root.constTree(policy4, root);
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
