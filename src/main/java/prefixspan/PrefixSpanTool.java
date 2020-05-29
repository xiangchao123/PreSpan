package prefixspan;

import prefixspan.AlarmPattern;
import prefixspan.ItemSet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by xiangchao on 2020/2/26.
 */
public class PrefixSpanTool {
    private static final int MIN_PAT=2;
    // 测试数据文件地址
    private String filePath;
    // 最小支持度阈值比例
    private double minSupportRate;
    // 最小支持度，通过序列总数乘以阈值比例计算
    private int minSupport;
    // 原始序列组
    private ArrayList<Sequence> totalSeqs;
    // 所有的单一项，用于递归枚举
    private ArrayList<String> singleItems;
    //频繁模式的存取结果
    HashMap<String, AlarmPattern> stringAlarmPatternHashMap;

    public PrefixSpanTool(String filePath, double minSupportRate) {
        this.filePath = filePath;
        this.minSupportRate = minSupportRate;
        readDataFile();
    }

    /**
     * 从文件中读取数据
     */
    private void readDataFile() {
        File file = new File(filePath);
        ArrayList<String[]> dataArray = new ArrayList<String[]>();

        try {
            BufferedReader in = new BufferedReader(new FileReader(file));
            String str;
            String[] tempArray;
            while ((str = in.readLine()) != null) {
                tempArray = str.split(" ");
                dataArray.add(tempArray);
            }
            in.close();
        } catch (IOException e) {
            e.getStackTrace();
        }

        minSupport = (int) (dataArray.size() * minSupportRate);
        totalSeqs = new ArrayList<>();
        Sequence tempSeq;
        for (String[] str : dataArray) {
            tempSeq = new Sequence();
            for (String s : str) {
                tempSeq.getItemSetList().add(s);
            }
            totalSeqs.add(tempSeq);
        }

        System.out.println("原始序列数据：");
        outputSeqence(totalSeqs);
    }

    /**
     * 输出序列列表内容
     *
     * @param seqList
     *            待输出序列列表
     */
    private void outputSeqence(ArrayList<Sequence> seqList) {
        for (Sequence seq : seqList) {
            System.out.print("<");
            for (String s : seq.getItemSetList()) {
                    System.out.print(s + " ");
            }
            System.out.println(">");
        }
    }

    /**
     * 移除初始序列中不满足最小支持度阈值的单项
     */
    private void removeInitSeqsItem() {
        int count = 0;
        HashMap<String, Integer> itemMap = new HashMap<>();
        singleItems = new ArrayList<>();

        for (Sequence seq : totalSeqs) {
                for (String s : seq.getItemSetList()) {
                    if (!itemMap.containsKey(s)) {
                        itemMap.put(s, 1);
                    }
                }
        }

        String key;
        for (Map.Entry entry : itemMap.entrySet()) {
            count = 0;
            key = (String) entry.getKey();
            for (Sequence seq : totalSeqs) {
                if (seq.getItemSetList().contains(key)) {
                    count++;
                }
            }

            itemMap.put(key, count);

        }

        for (Map.Entry entry : itemMap.entrySet()) {
            key = (String) entry.getKey();
            count = (int) entry.getValue();

            if (count < minSupport) {
                // 如果支持度阈值小于所得的最小支持度阈值，则删除该项
                for (Sequence seq : totalSeqs) {
                    seq.deleteSingleItem(key);
                }
            } else {
                singleItems.add(key);
            }
        }

        Collections.sort(singleItems);
    }

    /**
     * 递归搜索满足条件的序列模式
     *
     * @param beforeSeq
     *            前缀序列
     * @param afterSeqList
     *            后缀序列列表
     */
    private void recursiveSearchSeqs(Sequence beforeSeq,
                                     ArrayList<Sequence> afterSeqList) {
        ItemSet tempItemSet;
        Sequence tempSeq2;
        Sequence tempSeq;
        ArrayList<Sequence> tempSeqList = new ArrayList<>();

        for (String s : singleItems) {
            // 分成2种形式递归，以<a>为起始项，第一种直接加入独立项集遍历<a,a>,<a,b> <a,c>..
            if (isLargerThanMinSupport(s, afterSeqList)) {
                tempSeq = beforeSeq.copySeqence();
                tempSeq.getItemSetList().add(s);
                stringAlarmPatternHashMap.get(tempSeq.getItemSetList().get(0)).getSequenceArrayList().add(tempSeq);
                tempSeqList = new ArrayList<>();
                for (Sequence seq : afterSeqList) {
                    if (seq.getItemSetList().contains(s)) {
                        tempSeq2 = seq.extractItem(s);
                        if(tempSeq2.getItemSetList().size()!=0) {
                            tempSeqList.add(tempSeq2);
                        }
                    }
                }
                recursiveSearchSeqs(tempSeq, tempSeqList);
            }
        }
    }

    /**
     * 所传入的项组合在所给定序列中的支持度是否超过阈值
     *
     * @param s
     *            所需匹配的项
     * @param seqList
     *            比较序列数据
     * @return
     */
    private boolean isLargerThanMinSupport(String s, ArrayList<Sequence> seqList) {
        boolean isLarge = false;
        int count = 0;

        for (Sequence seq : seqList) {
            if (seq.getItemSetList().contains(s)) {
                count++;
            }
        }

        if (count >= minSupport) {
            isLarge = true;
        }

        return isLarge;
    }

    /**
     * 序列模式分析计算
     */
    public void prefixSpanCalculate() {
        Sequence seq;
        Sequence tempSeq;
        ArrayList<Sequence> tempSeqList = new ArrayList<>();
        ItemSet itemSet;
        stringAlarmPatternHashMap = new HashMap<>();
        removeInitSeqsItem();


        for (String s : singleItems) {
            // 从最开始的a,b,d开始递归往下寻找频繁序列模式
            seq = new Sequence();
            seq.getItemSetList().add(s);
            AlarmPattern alarmPattern = new AlarmPattern();
            alarmPattern.getSequenceArrayList().add(seq);
            stringAlarmPatternHashMap.put(s,alarmPattern);

            if (isLargerThanMinSupport(s, totalSeqs)) {
                tempSeqList = new ArrayList<>();
                for (Sequence s2 : totalSeqs) {
                    // 判断单一项是否包含于在序列中，包含才进行提取操作
                    if (s2.getItemSetList().contains(s)) {
                        tempSeq = s2.extractItem(s);
                        if(tempSeq.getItemSetList().size()!=0) {
                            tempSeqList.add(tempSeq);
                        }
                    }
                }
                recursiveSearchSeqs(seq, tempSeqList);
            }
        }

        printTotalFreSeqs();
    }

    /**
     * 按模式类别输出频繁序列模式
     */
    private void printTotalFreSeqs() {
        System.out.println("序列模式挖掘结果：");
        for(String s :singleItems){
            AlarmPattern tempalarm = stringAlarmPatternHashMap.get(s);
            ArrayList<Sequence> deletseq = new ArrayList<>();
            if(tempalarm!=null){
                for(int i=0;i<tempalarm.getSequenceArrayList().size();i++){
                    ArrayList<String> s1 = tempalarm.getSequenceArrayList().get(i).getItemSetList();
                    if(s1.size()<=MIN_PAT){
                        deletseq.add(tempalarm.getSequenceArrayList().get(i));
                    }
                    for(int j=0;j<tempalarm.getSequenceArrayList().size();j++){
                        //本身不比较
                        if(i==j){
                            continue;
                        }
                         ArrayList<String> s2 = tempalarm.getSequenceArrayList().get(j).getItemSetList();
                        boolean iscontain = false;
                        int count = 0;
                        int index=0;
                        for(int index_s1=0;index_s1<s1.size();index_s1++){
                            for(int index_s2=index;index_s2<s2.size();index_s2++){
                                if(s1.get(index_s1).equals(s2.get(index_s2))){
                                    count++;
                                    index = index_s2+1;
                                    break;
                                }
                            }
                        }
                        if(count==s1.size()){
                            iscontain = true;
                        }
                        if(iscontain){
                            deletseq.add(tempalarm.getSequenceArrayList().get(i));
                            break;
                        }
                    }
                }
                tempalarm.getSequenceArrayList().removeAll(deletseq);
                for(Sequence sequence:tempalarm.getSequenceArrayList()){
                    System.out.println(sequence.getItemSetList());
                }
            }
        }
    }
}

