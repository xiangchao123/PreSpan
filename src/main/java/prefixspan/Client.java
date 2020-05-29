package prefixspan;

/**
 * Created by xiangchao on 2020/2/26.
 */
public class Client {
    public static void main(String[] agrs){
        String filePath = "F:\\flood.txt";
        //最小支持度阈值率
        double minSupportRate = 0.01;

        PrefixSpanTool tool = new PrefixSpanTool(filePath, minSupportRate);
        tool.prefixSpanCalculate();
    }
}
