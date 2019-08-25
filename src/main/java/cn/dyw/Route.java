package cn.dyw;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 一段路径
 */
@Data
public class Route {

    private List<Site> sites = new ArrayList<>();

    private MetroLine line;

    public void add(Site site) {
        sites.add(site);
    }

    public void addAll(List<Site> sites) {
        this.sites.addAll(sites);
    }

    @Override
    public String toString() {
        return "Route{" +
                "sites=" + sites +
                ", line=" + line.getName() +
                '}';
    }
}
