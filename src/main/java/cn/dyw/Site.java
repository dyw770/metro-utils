package cn.dyw;

import lombok.Data;
import lombok.ToString;

/**
 * 站点
 *
 * @author ldt
 */
@Data
@ToString
public class Site implements Comparable<Site> {

    /**
     * 站点名
     */
    private String name;

    /**
     * 序号
     */
    private int index;

    public int compareTo(Site o) {
        return o.index - index;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Site)) return false;

        Site site = (Site) o;

        return name.equals(site.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
