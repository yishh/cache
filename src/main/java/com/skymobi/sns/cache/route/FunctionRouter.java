package com.skymobi.sns.cache.route;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: liweijing
 * Date: 11-12-9
 * Time: 上午11:02
 * To change this template use File | Settings | File Templates.
 */
public class FunctionRouter extends AbstractRouter {
    Logger logger = LoggerFactory.getLogger(FunctionRouter.class);
//    protected Function function;



    private Map<String, Function> patterns = new HashMap<String, Function>();
    private Map<Pattern, Function> buildPatterns = new HashMap<Pattern, Function>();
    
    public FunctionRouter(Map<String, Function> patterns){
        this.patterns = patterns;
        for(String key: this.patterns.keySet()){
            Pattern pattern = Pattern.compile(key);
            buildPatterns.put(pattern, patterns.get(key));
        }
    }

    Object[] extractArgs(String key, Pattern pattern){
        Matcher m = pattern.matcher(key);
        if(!m.matches()) {
            return null;
        }
        logger.debug("key [{}] match {}", key, pattern.toString());
        Object[] args = new Object[m.groupCount()];
        for(int i=0; i<m.groupCount(); i++){
            args[i] = m.group(i+1);
        }
        return args;
    }
    @Override
    public String getHost(String key) {
        for(Pattern pattern: buildPatterns.keySet()){
            Object[] args = extractArgs(key, pattern);
            if(args!=null){
                int index =  buildPatterns.get(pattern).locate(key, args);
                String host = ((List<String>)hosts).get(index);
                logger.debug("key [{}] route to host [{}]", key, host);
                return host;
            }
        }
        logger.info("key [{}]  match nothing, return default", key);
        return defaultHost;
    }


    public Map<String, Function> getPatterns() {
        return patterns;
    }
}
