package prefixspan;

import java.util.ArrayList;

/**
 * Created by xiangchao on 2020/2/26.
 */
public class Sequence {
    // 序列内的项集
    private ArrayList<String> itemSetList;

    public Sequence() {
        this.itemSetList = new ArrayList<>();
    }

    public ArrayList<String> getItemSetList() {
        return itemSetList;
    }

    public void setItemSetList(ArrayList<String> itemSetList) {
        this.itemSetList = itemSetList;
    }


    /**
     * 删除单个项
     *
     * @param s
     *            待删除项
     */
    public void deleteSingleItem(String s) {
        ArrayList<String> deleteItems = new ArrayList<>();
        for (int i = 0; i < this.itemSetList.size(); i++) {
            if (this.itemSetList.get(i).equals(s)) {
                    deleteItems.add(this.itemSetList.get(i));
            }
        }
        this.itemSetList.removeAll(deleteItems);
    }

    /**
     * 提取项s之后所得的序列
     *
     * @param s
     *            目标提取项s
     */
    public Sequence extractItem(String s) {

        Sequence extractSeq = new Sequence();
        if(this.getItemSetList().contains(s)){
            int index = this.getItemSetList().indexOf(s);
            if(index==this.getItemSetList().size()-1){
                return extractSeq;
            }
            else {
                for(int j=index+1;j<this.getItemSetList().size();j++){
                    extractSeq.getItemSetList().add(this.getItemSetList().get(j));
                }
            }
        }
        return extractSeq;
    }



    /**
     * 深拷贝一个序列
     *
     * @return
     */
    public Sequence copySeqence() {
        Sequence copySeq = new Sequence();
        ItemSet tempItemSet;
        ArrayList<String> items;

        for (String s : this.itemSetList) {

            copySeq.getItemSetList().add(s);
        }

        return copySeq;
    }



    /**
     * 判断strList2是否是strList1的子序列
     *
     * @param strList1
     * @param strList2
     * @return
     */
    public boolean strArrayContains(ArrayList<String> strList1,
                                    ArrayList<String> strList2) {
        boolean isContained = false;

        for (int i = 0; i < strList1.size() - strList2.size() + 1; i++) {
            isContained = true;

            for (int j = 0, k = i; j < strList2.size(); j++, k++) {
                if (!strList1.get(k).equals(strList2.get(j))) {
                    isContained = false;
                    break;
                }
            }

            if (isContained) {
                break;
            }
        }

        return isContained;
    }
}

