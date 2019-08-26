package cn.dyw;

import lombok.Data;
import lombok.extern.log4j.Log4j;

import java.io.FileInputStream;
import java.util.*;

/**
 * 地铁地图
 *
 * @author ldt
 */
@Log4j
@Data
public class MetroMap {

    /**
     * 地铁线路
     */
    private List<MetroLine> metroLines;

    /**
     * 换乘不超10次
     */
    private static int MAX_DEPTH = 10;

    /**
     * 换乘站
     */
    private Map<String, TransferSite> transferSiteMap;

    public MetroMap() {
        metroLines = new ArrayList<>();
        transferSiteMap = new HashMap<>();
    }

    public void init(String filePath) throws Exception {
        buildMetroLine(filePath);
        buildTransferSite();
        buildAdjoinSite();
    }

    /**
     * 从文件中构建地图路线
     *
     * @param filePath 地图文件路径
     * @throws Exception 解析文件错误
     */
    public void buildMetroLine(String filePath) throws Exception {
        FileInputStream fis = new FileInputStream(filePath);
        Scanner scan = new Scanner(fis);
        while (scan.hasNextLine()) {
            String s = scan.nextLine();
            String[] split = s.split("-");
            if (split.length < 2) continue;
            MetroLine metroLine = null;
            String lineName = split[0];
            if (lineName.endsWith("|")) {
                metroLine = new RingMetroLine();
                lineName = lineName.substring(0, lineName.length() - 1).trim();
            } else {
                metroLine = new StraightMetroLine();
            }
            metroLine.setName(lineName);
            String sites = split[1];
            String[] siteArr = sites.split(" ");
            Set<Site> siteSet = new HashSet<>();
            for (int i = 0; i < siteArr.length; i++) {
                Site site = new Site();
                site.setName(siteArr[i].trim());
                site.setIndex(i);
                siteSet.add(site);
            }
            metroLine.setSites(siteSet);
            metroLines.add(metroLine);
        }
        scan.close();
        log.debug("解析地图:" + metroLines);
    }


    /**
     * 构建一个行程
     *
     * @param startSite 开始站点
     * @param endSite 结束站点
     * @return 行程
     * @throws Exception 找不到站点
     */
    public Trip buildTrip(String startSite, String endSite) throws Exception {
        Trip trip = new Trip();
        trip.setSetOut(startSite);
        trip.setArrive(endSite);

        TransferSite transferSite1 = transferSiteMap.get(trip.getSetOut());
        if (transferSite1 != null) {
            trip.setSetOutJoin(transferSite1.getTransferSites());
        } else {
            trip.getSetOutJoin().addAll(buildJoinSite(buildAllSiteOnLine(trip.getSetOut())));
        }
        TransferSite transferSite2 = transferSiteMap.get(trip.getArrive());
        if (transferSite2 != null) {
            trip.setArriveJoin(transferSite2.getTransferSites());
        } else {
            trip.getArriveJoin().addAll(buildJoinSite(buildAllSiteOnLine(trip.getArrive())));
        }
        return trip;
    }

    /**
     * 查找路径
     * @param startSite
     * @param endSite
     * @return
     * @throws Exception
     */
    public RouteDetails buildRouteDetails(String startSite, String endSite) throws Exception {
        Trip trip = buildTrip(startSite, endSite);

        RouteDetails routeDetails = new RouteDetails(trip);


        // 如果起点站和终点站在一条线上 且中间没有换乘站则直接走是最近的，不用搜索路线
        MetroLine metroLine = tripOnline(trip);
        boolean b = tripOnTransferSite(trip, metroLine);
        if (b) {
            List<Site> range = metroLine.range(trip.getSetOut(), trip.getArrive());
            Route route = new Route();
            route.setSites(range);
            route.setLine(metroLine);
            RouteLine routeLine = new RouteLine();
            routeLine.add(route);

            routeDetails.add(routeLine);
        } else {
            List<List<TransferSite>> searchTrip = searchTrip(trip);
            routeDetails = buildRange(searchTrip, trip);
        }
        if (routeDetails.getLists().isEmpty()) {
            throw new Exception("没有查找到路线");
        }
        return routeDetails;
    }


    /**
     *  构建路径
     * @param result
     * @param trip
     * @return
     * @throws Exception
     */
    public RouteDetails buildRange(List<List<TransferSite>> result, Trip trip) throws Exception {
        RouteDetails routeDetails = new RouteDetails(trip);
        for (List<TransferSite> transferSites : result) {
            RouteLine routeLine = new RouteLine();
            if (transferSites.size() == 1) {
                // 只有一个换乘点
                TransferSite transferSite = transferSites.get(0);
                buildTripStartAndEnd(trip, transferSite, transferSite, routeLine);
                routeDetails.add(routeLine);
            } else {
                List<Route> buildRange = buildRange(transferSites);
                routeLine.addAll(buildRange);
                buildTripStartAndEnd(
                        trip,
                        transferSites.get(0),
                        transferSites.get(transferSites.size() - 1),
                        routeLine
                );
                routeDetails.add(routeLine);
            }
        }
        return routeDetails;
    }

    public static List<Route> buildRange(List<TransferSite> transferSites) throws Exception {
        List<Route> rt = new ArrayList<>();

        for (int i = 1; i < transferSites.size(); i++) {
            TransferSite t1 = transferSites.get(i);
            TransferSite t2 = transferSites.get(i - 1);

            Set<MetroLine> lines = new HashSet<>(t1.getMetroLines());
            lines.retainAll(t2.getMetroLines());
            MetroLine line = lines.iterator().next();

            Route route = new Route();
            route.setLine(line);
            route.addAll(line.range(t2.getSiteName(), t1.getSiteName()));
            rt.add(route);
        }
        log.debug("查找到的路径:" + rt);
        return rt;
    }

    /**
     * 如果起点和终点之间是否有在一条线
     * @param trip
     * @return
     */
    private MetroLine tripOnline(Trip trip) throws Exception {
        List<SiteDetails> setOutSites = buildAllSiteOnLine(trip.getSetOut());
        List<SiteDetails> arriveSites = buildAllSiteOnLine(trip.getArrive());

        for (SiteDetails setOutSite : setOutSites) {
            for (SiteDetails arriveSite : arriveSites) {
                if (setOutSite.getLine().getName().equals(arriveSite.getLine().getName()))
                    return setOutSite.getLine();
            }
        }
        return null;
    }

    /**
     * 如果起点和终点之间是否有有换乘站
     * @param trip
     * @return
     */
    private boolean tripOnTransferSite(Trip trip, MetroLine line) throws Exception {
        if (line == null) return false;
        List<Site> range = line.range(trip.getSetOut(), trip.getSetOut());
        Set<String> keySet = getTransferSiteMap().keySet();
        for (Site site : range) {
            if (keySet.contains(site.getName()))
                return true;
        }
        return false;
    }

    /**
     * 查找换乘点到起点和终点的路线
     * @param trip
     * @param t1 t1 到起点的路线
     * @param t2 t2 到终点
     * @param routeLine
     * @return
     * @throws Exception
     */
    public void buildTripStartAndEnd(Trip trip, TransferSite t1, TransferSite t2, RouteLine routeLine) throws Exception {
        // 起点到换乘点的距离
        List<SiteDetails> siteDetailsList = buildAllSiteOnLine(trip.getSetOut());
        for (SiteDetails siteDetails : siteDetailsList) {
            if (t1.getMetroLines().contains(siteDetails.getLine())) {
                MetroLine line = siteDetails.getLine();
                Route route = new Route();
                route.setLine(siteDetails.getLine());
                route.addAll(line.range(trip.getSetOut(), t1.getSiteName()));
                routeLine.getRoutes().add(0, route);
            }
        }

        // 换乘点到终点的距离
        List<SiteDetails> siteDe = buildAllSiteOnLine(trip.getArrive());
        for (SiteDetails siteDetails : siteDe) {
            if (t2.getMetroLines().contains(siteDetails.getLine())) {
                MetroLine line = siteDetails.getLine();
                Route route = new Route();
                route.setLine(siteDetails.getLine());
                route.addAll(line.range(t2.getSiteName(), trip.getArrive()));
                routeLine.getRoutes().add(route);
            }
        }

        log.debug("查找到的路径:" + routeLine.getRoutes());
    }

    /**
     * 搜索行程
     * @param trip 行程
     */
    public List<List<TransferSite>> searchTrip(Trip trip) {
        Set<String> checkSet = new HashSet<>();
        List<List<TransferSite>> line = new ArrayList<>();
        // 将起点放入检查队列 避免循环
        checkSet.add(trip.getSetOut());
        searchSite(trip,
                trip.getSetOutJoin(),
                checkSet,
                new ArrayList<>(),
                line);
        return line;
    }

    /**
     * 搜索站点
     * @param trip 行程
     * @param listSite 需要搜索的点
     * @param checkSet 已经走过的点
     * @param router 当前正在进行的路线
     * @param line 所有的能到达的路线
     */
    public void searchSite(Trip trip,
                                  Set<TransferSite> listSite,
                                  Set<String> checkSet,
                                  List<TransferSite> router,
                                  List<List<TransferSite>> line) {
        for (TransferSite transferSite : listSite) {
            if (checkSet.contains(transferSite.getSiteName())) {
                continue;
            }
            List<TransferSite> rt = new ArrayList<>(router);
            Set<String> checkTmp = new HashSet<>(checkSet);

            checkTmp.add(transferSite.getSiteName());
            rt.add(transferSite);

            if (trip.getArriveJoin().contains(transferSite)) {
                line.add(rt);
            } else {
                if (rt.size() < MAX_DEPTH) {
                    searchSite(trip, transferSite.getTransferSites(), checkTmp, rt, line);
                }
            }
        }
        log.debug("没找到终点");
    }

    /**
     * 查找这个线上的相邻换乘
     *
     * @param siteDetails
     * @return
     */
    public List<TransferSite> buildJoinSite(SiteDetails siteDetails) {
        MetroLine line = siteDetails.getLine();
        List<TransferSite> tmp = new ArrayList<>();
        List<String> siteNames = line.nextTransferSite(siteDetails.getSite().getName());
        for (String s : siteNames) {
            TransferSite transferSite = transferSiteMap.get(s);
            tmp.add(transferSite);
        }
        return tmp;
    }

    /**
     * 查找这个线上的相邻换乘
     * @param siteDetails
     * @return
     */
    public List<TransferSite> buildJoinSite(List<SiteDetails> siteDetails) {
        List<TransferSite> tmp = new ArrayList<>();
        for (SiteDetails siteDetail : siteDetails) {
            tmp.addAll(buildJoinSite(siteDetail));
        }
        return tmp;
    }

    /**
     * 查找站点在那条线上
     *
     * @param siteName
     */
    public SiteDetails buildSiteLine(String siteName) throws Exception {
        List<MetroLine> metroLines = getMetroLines();
        for (MetroLine line : metroLines) {
            for (Site site : line.getSites()) {
                if (site.getName().equals(siteName)) {
                    SiteDetails siteDetails = new SiteDetails();
                    siteDetails.setLine(line);
                    siteDetails.setSite(site);
                    return siteDetails;
                }
            }
        }
        throw new Exception("无法找到指定的车站[" + siteName + "]");
    }

    /**
     * 查找站点在那些线上
     *
     * @param siteName
     */
    public List<SiteDetails> buildAllSiteOnLine(String siteName) throws Exception {
        List<SiteDetails> list = new ArrayList<>();
        List<MetroLine> metroLines = getMetroLines();
        for (MetroLine line : metroLines) {
            for (Site site : line.getSites()) {
                if (site.getName().equals(siteName)) {
                    SiteDetails siteDetails = new SiteDetails();
                    siteDetails.setLine(line);
                    siteDetails.setSite(site);
                    list.add(siteDetails);
                }
            }
        }
        if (list.isEmpty())
            throw new Exception("没有在地图中找到[" + siteName + "]");
        return list;
    }

    /**
     * 构建相邻站点
     */
    public void buildAdjoinSite() {
        Collection<TransferSite> transferSites = transferSiteMap.values();
        for (TransferSite transferSite : transferSites) {
            Set<MetroLine> metroLines = transferSite.getMetroLines();
            for (MetroLine metroLine : metroLines) {
                List<String> transferSiteName = metroLine.nextTransferSite(transferSite.getSiteName());
                addAdjoinSite(transferSiteName, transferSite);
            }
        }
        log.debug("寻找相邻车站:" + transferSiteMap);
    }


    /**
     * 查找出所有的换乘站
     *
     * @return
     */
    public void buildTransferSite() {
        for (int i = 0; i < metroLines.size(); i++) {
            MetroLine metroLine = metroLines.get(i);
            Set<Site> site1 = metroLine.getSites();
            for (int k = i + 1; k < metroLines.size(); k++) {
                MetroLine line = metroLines.get(k);
                Set<Site> site2 = line.getSites();
                // 求得交点
                Set<Site> tmp = new HashSet<>(site1);
                tmp.retainAll(site2);
                addTransferSite(line, metroLine, tmp);
            }
        }
        log.debug("换乘站:" + transferSiteMap);
    }

    /**
     * 添加换乘站到集合中
     *
     * @param metroLine
     * @param line
     * @param tmp
     */
    private void addTransferSite(MetroLine metroLine,
                                 MetroLine line,
                                 Set<Site> tmp) {
        tmp.forEach(item -> {
            TransferSite transferSite = transferSiteMap.get(item.getName());
            if (transferSite == null) {
                transferSite = new TransferSite();
                transferSite.addMetroLine(metroLine);
                transferSite.addMetroLine(line);
                transferSite.setSiteName(item.getName());
                metroLine.addTransferSite(transferSite);
                line.addTransferSite(transferSite);
                transferSiteMap.put(item.getName(), transferSite);
            } else {
                transferSite.addMetroLine(metroLine);
                transferSite.addMetroLine(line);
                metroLine.addTransferSite(transferSite);
                line.addTransferSite(transferSite);
            }
        });
    }

    /**
     * 构建相邻站点
     */
    private void addAdjoinSite(List<String> transferSites, TransferSite site) {
        for (String s : transferSites) {
            TransferSite transferSite = transferSiteMap.get(s);
            site.addTransferSites(transferSite);
        }
        log.debug("寻找相邻车站:" + transferSiteMap);
    }
}
