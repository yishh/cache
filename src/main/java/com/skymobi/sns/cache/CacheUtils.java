package com.skymobi.sns.cache;

import com.skymobi.sns.cache.annotation.CacheKey;
import org.apache.commons.lang.StringUtils;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParserContext;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import redis.clients.util.SafeEncoder;

/**
 * User: liweijing
 * Date: 11-7-29
 * Time: 下午3:19
 */
public class CacheUtils {





    public static int getExpire(String timeExpress, int defaultExpire) {
        if (StringUtils.isEmpty(timeExpress)) {
            return defaultExpire;
        }
        return parseDuration(timeExpress);
    }

    static Pattern days = Pattern.compile("^([0-9]+)d$");
    static Pattern hours = Pattern.compile("^([0-9]+)h$");
    static Pattern minutes = Pattern.compile("^([0-9]+)mi?n$");
    static Pattern seconds = Pattern.compile("^([0-9]+)s$");

    /**
     * Parse a duration
     *
     * @param duration 3h, 2mn, 7s
     * @return The number of seconds
     */


    public static int parseDuration(String duration) {
        if (duration == null) {
            return 60 * 60 * 24 * 30;
        }
        int toAdd = -1;
        if (days.matcher(duration).matches()) {
            Matcher matcher = days.matcher(duration);
            matcher.matches();
            toAdd = Integer.parseInt(matcher.group(1)) * (60 * 60) * 24;
        } else if (hours.matcher(duration).matches()) {
            Matcher matcher = hours.matcher(duration);
            matcher.matches();
            toAdd = Integer.parseInt(matcher.group(1)) * (60 * 60);
        } else if (minutes.matcher(duration).matches()) {
            Matcher matcher = minutes.matcher(duration);
            matcher.matches();
            toAdd = Integer.parseInt(matcher.group(1)) * (60);
        } else if (seconds.matcher(duration).matches()) {
            Matcher matcher = seconds.matcher(duration);
            matcher.matches();
            toAdd = Integer.parseInt(matcher.group(1));
        }
        if (toAdd == -1) {
            throw new IllegalArgumentException("Invalid duration pattern : " + duration);
        }
        return toAdd;
    }

    public static String parseSimpleKey(String template, Object[] parameters) {
        ExpressionParser parser = new SpelExpressionParser();
        ParserContext templateContext = new TemplateParserContext();
        String expressTemplate = template.replaceAll("\\$\\{([^\\$]+){1}\\}", "#{#$1}");

        Expression expression = parser.parseExpression(expressTemplate, templateContext);

        StandardEvaluationContext context = new StandardEvaluationContext();
        int i = 0;
        for (Object obj : parameters) {
            context.setVariable(String.format("p%s", i), obj);
            i++;
        }
        return expression.getValue(context, String.class);
    }

    public static String parseCacheKey(CacheKey cacheKey, Object[] parameters) {
        String template = cacheKey.template();
        if (cacheKey.simple()) {
            return template;
        }

        String[] els = cacheKey.els();
        if (els.length == 0) {
            return parseSimpleKey(template, parameters);
        }
        ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext teslaContext = new StandardEvaluationContext();
        teslaContext.setVariable("p", parameters);
        String[] parsedArgs = new String[els.length];
        int i = 0;
        for (String el : els) {
            String invention = parser.parseExpression(el).getValue(teslaContext, String.class);
            parsedArgs[i] = invention;
            i++;
        }

        return String.format(template, parsedArgs);
    }


//    public static void main(String[] args){
//        ExpressionParser parser = new SpelExpressionParser();
//        ParserContext templateContext = new TemplateParserContext();
//        Expression expression = parser.parseExpression("TEST/#{#p0}", templateContext);
//
//        StandardEvaluationContext context = new StandardEvaluationContext();
//
//        Map<String, String> testMap = new HashMap<String, String>();
//        testMap.put("name","test");
//
//        class User{
//            public String name;
//        }
//        User user = new User();
//        user.name="huigh";
//        Object[] parameters = new Object[]{2, 3, user};
//        context.setVariable("p", parameters);
//        context.setVariable("p0", parameters[1]);
//
//        String invention = expression.getValue(context, String.class);
//
//        Pattern p = Pattern.compile("\\$\\{(\\w+)\\}");
//
//        String test =  "TEST/${p0}/${p1}";
////        Matcher m = p.matcher(test);
////        m.matches();
////        System.out.println(m.groupCount());
////        while (m.find()) {
////            System.out.println(m.group(0));
////
//////            m.find();
////            System.out.println(test);
////        }
//        test = test.replaceAll("\\$\\{(\\w+){1}\\}", "#{#$1}");
//        System.out.println(parseSimpleKey("TEST/${p0}/${p1}/${p2.name}", parameters));
////        System.out.println(test);
////        String invention = parser.parseExpression("TEST/#{p[0]}/#{p[1]}").getValue(teslaContext, String.class);
////        System.out.println(invention);
//    }
}
