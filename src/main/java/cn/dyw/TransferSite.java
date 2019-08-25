package cn.dyw;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

/**
 * 换乘站
 */
@Data
public class TransferSite {

    /**
     * 换乘站所属线路
     */
    private Set<MetroLine> metroLines = new HashSet<>();

    /**
     * 站点
     */
    private String siteName;

    /**
     * 相邻换乘站点
     */
    private Set<TransferSite> transferSites = new HashSet<>();

    public void addMetroLine(MetroLine metroLine) {
        metroLines.add(metroLine);
    }


    public void addTransferSites(TransferSite site) {
        transferSites.add(site);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TransferSite)) return false;

        TransferSite that = (TransferSite) o;

        return siteName.equals(that.siteName);
    }

    @Override
    public int hashCode() {
        return siteName.hashCode();
    }

    @Override
    public String toString() {
        String lineName = "";
        for (MetroLine metroLine : metroLines) {
            lineName += "-" + metroLine.getName();
        }

        String joinSite = "";
        for (TransferSite transferSite : transferSites) {
            joinSite += "-" + transferSite.getSiteName();
        }
        return "TransferSite{" +
                "metroLines=" + lineName +
                ", siteName='" + siteName + '\'' +
                ", transferSites=" + joinSite +
                '}';
    }
}
