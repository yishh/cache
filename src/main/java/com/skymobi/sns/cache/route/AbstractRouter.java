package com.skymobi.sns.cache.route;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: liweijing
 * Date: 11-12-9
 * Time: 上午11:21
 * To change this template use File | Settings | File Templates.
 */
public abstract  class AbstractRouter implements KeyRouter{
    protected String defaultHost;
    protected Collection<String> hosts = new ArrayList<String>();


    @Override
    public String getDefaultHost() {
        return defaultHost;
    }

    @Override
    public Collection<String> getHosts() {
        return hosts;
    }

    public void setDefaultHost(String defaultHost) {
        this.defaultHost = defaultHost;
    }

    public void setHosts(Collection<String> hosts) {
        this.hosts = hosts;
    }
}
