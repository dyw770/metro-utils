package cn.dyw;

import lombok.Data;
import lombok.extern.log4j.Log4j;

import java.util.*;

/**
 * 所有查到到的路径
 */
@Data
@Log4j
public class RouteDetails {

    private List<RouteLine> lists;

    private Trip trip;

    public RouteDetails(Trip trip) {
        this.lists = new ArrayList<>();
        this.trip = trip;
    }

    public void add(RouteLine routeLine) {
        lists.add(routeLine);
    }

    public void addAll(List<RouteLine> routeLines) {
        this.lists.addAll(routeLines);
    }

    public RouteLine minSiteRoute() {
        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < lists.size(); i++) {
            List<Route> routes = lists.get(i).getRoutes();
            int sum = 0;
            for (int k = 0; k < routes.size(); k++) {
                for (Route route : routes) {
                    sum += route.getSites().size();
                }
            }
            map.put(i, sum - 1);
        }

        Map.Entry<Integer, Integer> min = map.entrySet()
                .stream()
                .min(
                        Comparator.comparingInt(Map.Entry::getValue)
                )
                .get();
        RouteLine routeLine = lists.get(min.getKey());
        log.debug("查找到最短路线:[" + min.getValue() + "] ==" + routeLine);
        return routeLine;
    }
}
