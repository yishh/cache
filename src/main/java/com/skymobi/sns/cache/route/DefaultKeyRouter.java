package com.skymobi.sns.cache.route;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: liweijing
 * Date: 11-11-23
 * Time: 下午3:51
 * To change this template use File | Settings | File Templates.
 */
public class DefaultKeyRouter extends AbstractRouter implements KeyRouter {

    private String defaultHost;
    private Map<String, String> routes = new HashMap<String, String>();
    private Map<String, Pattern> patterns = new HashMap<String, Pattern>();


    public DefaultKeyRouter(String defaultHost, Map<String, String> routes) {
        this.defaultHost = defaultHost;
        for(String key: routes.keySet()){
            addRoute(key, routes.get(key));
        }
    }

    public void addRoute(String route, String address) {
        routes.put(route, address);
        Pattern pattern = Pattern.compile(route);
        patterns.put(route, pattern);
    }

    @Override
    public String getHost(String key) {
        for(Map.Entry<String, String> route: routes.entrySet()){
            if(matchRoute(key, route.getKey())){
                return route.getValue();
            }
        }
        return defaultHost;
    }

    private boolean matchRoute(String key, String route)  {

        Pattern pattern = patterns.get(route);
        Matcher matcher = pattern.matcher(key);
        return  matcher.matches();
    }

    public Map<String, String> getRoutes() {
        return routes;
    }

//    public void setRoutes(Map<String, String> routes) {
//        this.routes = routes;
//    }

    public String getDefaultHost() {
        return defaultHost;
    }

    @Override
    public Collection<String> getHosts() {
        return routes.values();
    }

    public void setDefaultHost(String defaultHost) {
        this.defaultHost = defaultHost;
    }
}
