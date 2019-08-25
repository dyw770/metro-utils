package cn.dyw;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

/**
 * 行程信息
 */
@Data
public class Trip {

    /**
     * 起点
     */
    private String setOut;

    /**
     * 终点
     */
    private String arrive;

    /**
     * 起点相邻的换乘站
     */
    private Set<TransferSite> setOutJoin = new HashSet<>();

    /**
     * 起点线
     */
    private MetroLine setOutLine;

    /**
     * 终点相邻的换乘站
     */
    private Set<TransferSite> arriveJoin = new HashSet<>();

    /**
     * 终点线
     */
    private MetroLine arriveLine;
}
