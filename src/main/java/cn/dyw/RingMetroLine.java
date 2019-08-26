package cn.dyw;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 环形线
 *
 * @author ldt
 */
public class RingMetroLine extends MetroLine {


    @Override
    public List<String> nextTransferSite(String siteName) {
        Set<String> tranSites = getTransferSites().keySet();
        List<String> siteList = getSites()
                .stream()
                .filter(item -> tranSites.contains(item.getName()) || item.getName().equals(siteName))
                .sorted(Comparator.comparingInt(Site::getIndex))
                .map(Site::getName)
                .collect(Collectors.toList());
        List<String> rt = new ArrayList<>();
        for (int i = 0; i < siteList.size(); i++) {
            String site = siteList.get(i);
            // 在这条线上找到了当前换乘点
            if (site.equals(siteName)) {
                if (siteList.size() - 1 > i) {
                    // 添加下一个点
                    rt.add(siteList.get(i + 1));
                } else if (i > 0) {
                    rt.add(siteList.get(0));
                }
                if (i > 0) {
                    // 添加上一个点
                    rt.add(siteList.get(i - 1));
                } else if (siteList.size() != 1) {
                    rt.add(siteList.get(siteList.size() - 1));
                }
            }
        }
        return rt;
    }

    @Override
    public List<Site> range(int start, int end) {
        // 环形线
        List<Site> sites = getSites()
                .stream()
                .sorted(Comparator.comparingInt(Site::getIndex))
                .collect(Collectors.toList());

        int s1 = end - start;
        int s2 = sites.size() - end + start;
        if (s1 < s2) {
            // 环形线不经过起点和终点时比较近
            return sites.subList(start, end + 1);
        } else {
            List<Site> rt = new ArrayList<>();
            rt.addAll(sites.subList(end, sites.size()));
            rt.addAll(sites.subList(0, start + 1));
            return rt;
        }
    }
}
