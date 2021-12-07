package mx.unam.iimas.mcic;

import mx.unam.iimas.mcic.utils.JsonMapper;
import mx.unam.iimas.mcic.voter.VoterName;

public class Sample {
    public static void main(String[] args) {
        System.out.println("Hello, world!");
        VoterName name = new VoterName("Victor", "Cruz");
        System.out.println(JsonMapper.toJSONString(name));
    }
}
