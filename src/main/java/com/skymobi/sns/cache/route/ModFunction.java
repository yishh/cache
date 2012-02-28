package com.skymobi.sns.cache.route;


import org.apache.commons.beanutils.ConvertUtils;

/**
 * Created by IntelliJ IDEA.
 * User: liweijing
 * Date: 11-12-9
 * Time: 上午11:11
 * To change this template use File | Settings | File Templates.
 */
public class ModFunction implements Function{
    
    private int mod;

    public ModFunction(int mod){
        setMod(mod);
    }
    @Override
    public int locate(String key, Object... args) {
        if(args.length < 1){
            throw new UnsupportedOperationException("mod function need one number argument");
        }
        Object obj = args[0];
        Long number = (Long) ConvertUtils.convert(obj, Long.class);
        
        return (int) (number % mod);
    }

    public int getMod() {
        return mod;
    }

    public void setMod(int mod) {
        this.mod = mod;
    }
}
