package cn;

/**
 * Created by bishe2016 on 下午9:08 17-4-6.
 */
public class Policy {
    String src;
    int srcPrefixLen;
    String des;
    int desPrefixLen;
    String action;

    Policy(String src, int srcPrefixLen, String des, int desPrefixLen, String action) {
        this.src = src;
        this.des = des;
        this.action = action;
        this.srcPrefixLen = srcPrefixLen;
        this.desPrefixLen = desPrefixLen;
        modifyIP(this.src, this.srcPrefixLen);
        modifyIP(this.des, this.desPrefixLen);
    }

    String getContextByField(String field) {
        if(field == Constant.FIELD_SOURCE) {
            return src;
        } else if(field == Constant.FIELD_DESTINATION) {
            return des;
        } else if(field == Constant.FIELD_ACTION) {
            return action;
        }

        return null;
    }

    void modifyIP(String ip, int prefixLen) {
        String [] ips = ip.split(".");
        switch (prefixLen) {
            case 0 :
                ip = "*" + "." + "*" + "." + "*" + "." + "*";
                break;
            case 8 :
                ip = ips[0] + "." + "*" + ".";
                break;
            case 16 :
                ip = ips[0] + "." + ips[1] + "." + "*" + "." + "*";
                break;
            case 24 :
                ip = ips[0] + "." + ips[1] + "." + ips[2] + "." + "*";
                break;
            case 32 :
                ip = ips[0] + "." + ips[1] + "." + ips[2] + "." + ips[3];
                break;
            default :
                //TODO:
        }
    }

    @Override
    public String toString() {
        return "Policy{" +
                "src='" + src + '\'' +
                ", des='" + des + '\'' +
                ", action='" + action + '\'' +
                '}';
    }
}
