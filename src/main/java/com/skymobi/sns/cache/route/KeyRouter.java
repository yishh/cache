package com.skymobi.sns.cache.route;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: liweijing
 * Date: 11-11-23
 * Time: 下午3:49
 * To change this template use File | Settings | File Templates.
 */
public interface KeyRouter {
    String getHost(String key);
    String getDefaultHost();
    Collection<String> getHosts();
//    Map<String, String> getRoutes();

}
