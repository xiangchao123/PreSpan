package dataprocess;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

/**
 * Created by xiangchao on 2020/2/28.
 */
public class DataProcess {
    private Connection conn  = DatabaseUtil.getMyconnection();
    private PreparedStatement ps = null;
    private Statement st = null;
    private ResultSet rs = null;
    private ResultSet rstmp = null;
    private HashMap<String,String> hashMap = null;
    //设置颤动时间
    private static final long PERIOD = 900000;
    //设置flood的持续时间
    private static final long FLOOD_INTERVAL =600000;
    //设置flood的最小个数
    private static final int EACH_OF_NUMBER_FLOOD = 4;
    private BufferedWriter bw = null;

    public  void  chatterRemove() {
        try {
            getNumerAlarm();
            System.out.println(hashMap.size());
            if(hashMap!=null){
//                conn.setAutoCommit(false);
//                st = conn.createStatement();
//                for(String key:hashMap.keySet()){
//                    try {
//                        String[] split = key.split("#");
//                        singlealarmChatterRemove(split[0],split[1]);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//                st.executeBatch();
//                conn.commit();

              // 开始构造序列，并存入txt文本
               getNoChatterAlarm();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }  finally {
            DatabaseUtil.close(rs,rstmp,ps,st,conn);
        }

        }

    //获取警报总类数
    public void getNumerAlarm() throws Exception {
        hashMap = new HashMap<>();
        String sql = "SELECT a.UNIT_ID,a.KPI_ID from (select UNIT_ID,KPI_ID,DB_TIME,CLEAR_TIME FROM iaas_alert_2 " +
                "GROUP BY UNIT_ID,KPI_ID,DB_TIME,CLEAR_TIME)a GROUP BY a.UNIT_ID,a.KPI_ID ";
        ps = conn.prepareStatement(sql);
        rs = ps.executeQuery();
        int count =1;
        while(rs.next()){
            String s = rs.getString(1)+"#"+rs.getString(2);
            String num = String.valueOf(count);
            count++;
            hashMap.put(s,num);
        }
    }

    //对每个警报移除其颤动实例
    public void singlealarmChatterRemove(String unit,String kpi) throws Exception {
        //获取每个警报发生的记录
        String sql = "SELECT a.UNIT_ID,a.KPI_ID,a.DB_TIME,a.CLEAR_TIME from (select UNIT_ID,KPI_ID,DB_TIME," +
                "CLEAR_TIME FROM iaas_alert_2 GROUP BY UNIT_ID,KPI_ID,DB_TIME,CLEAR_TIME)a WHERE a.UNIT_ID=? " +
                "and a.KPI_ID = ? ORDER BY a.DB_TIME  ";
        ps = conn.prepareStatement(sql);
        ps.setString(1,unit);
        ps.setString(2,kpi);
        rs = ps.executeQuery();
        int count = 1;
        //时区有问题，修改url链接参数即可
        Timestamp timestamp = null;
        while (rs.next()){
            if(count==1){
                count++;
                st.addBatch("INSERT into remove_chatteralarm(UNIT_ID,KPI_ID,DB_TIME,CLEAR_TIME) VALUES (" +
                        "'"+rs.getString(1)+"','"+rs.getString(2)+"','"+rs.getTimestamp(3)+"','"+rs.getTimestamp(4)+"')");
                timestamp = rs.getTimestamp(3);
                continue;
            }
            if(rs.getTimestamp(3).getTime()-timestamp.getTime()>PERIOD){
               timestamp = rs.getTimestamp(3);
               st.addBatch("INSERT into remove_chatteralarm(UNIT_ID,KPI_ID,DB_TIME,CLEAR_TIME) VALUES (" +
                       "'"+rs.getString(1)+"','"+rs.getString(2)+"','"+rs.getTimestamp(3)+"','"+rs.getTimestamp(4)+"')");
           }
        }
    }

    //获取去除颤动警报后的数据
    public void getNoChatterAlarm() throws SQLException, IOException, ParseException {
        String sql = "select UNIT_ID,KPI_ID,DB_TIME from remove_chatteralarm order by DB_Time";
        ps = conn.prepareStatement(sql);
        rs = ps.executeQuery();
        Date currenttime = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        currenttime = simpleDateFormat.parse("2019-01-01 00:00:00");
        ArrayList<ArrayList<String>> sequence = new ArrayList<>();
        while (rs.next()){
            if(simpleDateFormat.parse(simpleDateFormat.format((rs.getTimestamp(3)))).getTime()>currenttime.getTime()) {
//                System.out.println(simpleDateFormat.format((rs.getTimestamp(3))));
                currenttime = simpleDateFormat.parse(simpleDateFormat.format((rs.getTimestamp(3))));
                //统计条数
                String sql1 = "SELECT count(*) from (select UNIT_ID,KPI_ID from remove_chatteralarm " +
                        "where DB_TIME<=? and DB_TIME>=?)a";
                ps = conn.prepareStatement(sql1);

                ps.setObject(1, simpleDateFormat.format(currenttime.getTime() + FLOOD_INTERVAL));
                ps.setObject(2, simpleDateFormat.format(currenttime));
                rstmp = ps.executeQuery();
                int number = 0;
                while (rstmp.next()) {
                    number = rstmp.getInt(1);
                }
                if (number >= EACH_OF_NUMBER_FLOOD) {
                    //获取flood的每个警报
                    String sql2 = "select UNIT_ID,KPI_ID,DB_TIME from remove_chatteralarm where " +
                            "DB_TIME <= ? and DB_TIME>=?";
                    ps = conn.prepareStatement(sql2);
                    ps.setObject(1, simpleDateFormat.format(currenttime.getTime() + FLOOD_INTERVAL));
                    ps.setObject(2, simpleDateFormat.format(currenttime));
                    rstmp = ps.executeQuery();
                    ArrayList<String> flood = new ArrayList<>();
//                    System.out.println(simpleDateFormat.format(currenttime.getTime() + FLOOD_INTERVAL));
                    while (rstmp.next()) {
//                        System.out.print(rstmp.getTimestamp(3)+"  ");
                        flood.add(hashMap.get(rstmp.getString(1) + "#" + rstmp.getString(2)));
                    }
//                    System.out.println();
                    System.out.println(flood);
                    sequence.add(flood);
                    currenttime = new Timestamp(currenttime.getTime() + FLOOD_INTERVAL);
                }
            }
        }
        bw= new BufferedWriter(new FileWriter("F:\\flood.txt"));
        for(ArrayList<String> flood:sequence){
            for(String str:flood){
                bw.write(str+" ");
            }
            bw.write("\n");
            bw.flush();
        }
        bw.close();

    }
}
