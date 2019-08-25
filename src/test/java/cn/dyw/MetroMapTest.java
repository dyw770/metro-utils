package cn.dyw;

import org.junit.Test;

public class MetroMapTest {

    @Test
    public void buildRouteDetails() throws Exception {
        MetroMap metroMap = new MetroMap();
        metroMap.init("beijing-subway.txt");
        RouteDetails routeDetails = metroMap.buildRouteDetails("俸伯", "双井");
        RouteLine routeLine = routeDetails.minSiteRoute();
        System.out.println(routeLine.printRouter());
    }
}