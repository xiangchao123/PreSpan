package prefixspan;

import java.util.ArrayList;

/**
 * Created by xiangchao on 2020/2/26.
 */
public class ItemSet {
    // 项集内的字符
    private ArrayList<String> items;

    public ItemSet(String[] str) {
        items = new ArrayList<>();
        for (String s : str) {
            items.add(s);
        }
    }

    public ItemSet(ArrayList<String> itemsList) {
        this.items = itemsList;
    }

    public ItemSet(String s) {
        items = new ArrayList<>();
        for (int i = 0; i < s.length(); i++) {
            items.add(s.charAt(i) + "");
        }
    }

    public ArrayList<String> getItems() {
        return items;
    }

    public void setItems(ArrayList<String> items) {
        this.items = items;
    }

    /**
     * 获取项集最后1个元素
     *
     * @return
     */
    public String getLastValue() {
        int size = this.items.size();

        return this.items.get(size - 1);
    }

}
