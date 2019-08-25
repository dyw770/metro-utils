package cn.dyw;

import java.util.List;

public interface Line {

    /**
     * 计算两个站之间的距离
     * @param s1
     * @param s2
     * @return
     * @throws Exception
     */
    List<Site> range(String s1, String s2) throws Exception;
}
