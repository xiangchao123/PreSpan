package prefixspan;

import java.util.ArrayList;

/**
 * Created by xiangchao on 2020/2/27.
 */
public class AlarmPattern {
    private ArrayList<Sequence> sequenceArrayList;

    public AlarmPattern() {
        sequenceArrayList = new ArrayList<>();
    }
    public ArrayList<Sequence> getSequenceArrayList() {
        return sequenceArrayList;
    }

}
