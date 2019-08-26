package cn.dyw;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 路线
 */
@Data
public class RouteLine {

    /**
     * 路线
     */
    private List<Route> routes = new ArrayList<>();

    public void add(Route route) {
        routes.add(route);
    }

    public void addAll(List<Route> routes) {
        this.routes.addAll(routes);
    }

    /**
     * 站点数
     * @return
     */
    public int siteNum() {
        int sum = 0;
        for (int i = 0; i < routes.size(); i++) {
            List<Site> sites = routes.get(i).getSites();
            sum += sites.size();
        }
        return (sum - routes.size());
    }

    /**
     * 打印路线
     * @return
     */
    public String printRouter() {
        int sum = 0;
        StringBuilder rt = new StringBuilder();
        String lineName = "";
        for (int i = 0; i < routes.size(); i++) {
            Route route = routes.get(i);
            List<Site> sites = route.getSites();
            sum += sites.size();
            if (!lineName.equals(route.getLine().getName())) {
                lineName = route.getLine().getName();
                if (i != 0) {
                    rt.append(lineName.trim()).append("\n");
                }
            }
            for (int k = 0; k < sites.size(); k++) {
                if (i != 0 && k == 0) {

                } else {
                    rt.append(sites.get(k).getName().trim()).append("\n");
                }
            }
        }
        return (sum - routes.size()) + "\n" + rt.toString().trim();
    }
}
