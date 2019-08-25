package cn.dyw;

import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 地铁路线
 *
 * @author ldt
 */
@Data
public abstract class MetroLine implements Line {

    /**
     * 地铁站名
     */
    private String name;

    /**
     * 地铁站点
     */
    private Set<Site> sites;

    private Map<String, TransferSite> transferSites = new HashMap<>();

    public void addTransferSite(TransferSite transferSite) {
        transferSites.put(transferSite.getSiteName(), transferSite);
    }

    public void addAllTransferSite(Set<TransferSite> transferSites) {
        Map<String, TransferSite> collect = transferSites.stream().collect(Collectors.toMap(TransferSite::getSiteName, transferSite -> transferSite));
        this.transferSites.putAll(collect);
    }

    /**
     * 下一换乘点
     * @param site
     * @return
     */
    public abstract List<String> nextTransferSite(String site);


    @Override
    public List<Site> range(String s1, String s2) throws Exception {
        List<Site> sites = getSites()
                .stream()
                .sorted(Comparator.comparingInt(Site::getIndex))
                .collect(Collectors.toList());
        Set<String> siteNames = sites.stream().map(Site::getName)
                .collect(Collectors.toSet());
        if (!siteNames.contains(s1))
            throw new Exception("该线不包含[" + s1 + "]");
        if (!siteNames.contains(s2))
            throw new Exception("该线不包含[" + s2 + "]");
        int start = 0, end = 0, j = 0;

        for (; j < sites.size(); j++) {
            if (sites.get(j).getName().equals(s1)) {
                start = j;
            } else if (sites.get(j).getName().equals(s2)) {
                end = j;
            }
        }
        // 反向
        boolean tag = false;
        if (start > end) {
            tag = true;
            start = start + end;
            end = start - end;
            start = start - end;
        }

        List<Site> range = range(start, end);
        if (tag) {
            Collections.reverse(range);
        }
        return range;
    }

    /**
     * 计算两个站点之间的路线
     * @param start
     * @param end
     * @return
     */
    public abstract List<Site> range(int start, int end);

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MetroLine) {
            MetroLine line= (MetroLine) obj;
            return name.equals(line.getName());
        }
        return false;
    }

    @Override
    public String toString() {
        return "MetroLine{" +
                "name='" + name + '\'' +
                ", sites=" + sites +
                ", transferSites=" + transferSites +
                '}';
    }
}
