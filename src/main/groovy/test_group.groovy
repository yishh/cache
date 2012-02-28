import java.util.regex.Pattern
import java.util.regex.Matcher
/**
 * Created by IntelliJ IDEA.
 * User: liweijing
 * Date: 11-12-9
 * Time: 上午11:34
 * To change this template use File | Settings | File Templates.
 */

String src1 = "ACTIVITY/LIST/12345";
String re1  = "ACTIVITY/LIST/(\\d+)\$";
Pattern p = Pattern.compile(re1);
Matcher m = p.matcher(src1);
//println m.groupCount()

if(m.matches()){
    for(int i=0;i<m.groupCount();i++){
        println m.group(i+1)
    }
//    println m.group(1)
//    String tmp = m.group();
//    println("结果:"+tmp);
}