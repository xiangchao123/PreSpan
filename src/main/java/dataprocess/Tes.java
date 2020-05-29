package dataprocess;

import org.junit.Test;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

/**
 * Created by xiangchao on 2020/3/3.
 */
public class Tes {
    @Test
    public void test1(){
        String s = "abc";
        ByteBuffer bf = ByteBuffer.allocate(10);
        bf.put(s.getBytes());
        System.out.println(bf.position());
        bf.flip();
        System.out.println(bf.position());
        System.out.println(bf.limit());
        System.out.println(bf.capacity());
        bf.put(s.getBytes());
        bf.flip();
        byte[] d = new byte[bf.limit()];
        bf.get(d,0,2);
        System.out.println(new String(d,0,2));
        System.out.println(bf.position());
        System.out.println(bf.limit());
        System.out.println(bf.capacity());
        bf.mark();
        bf.get(d,2,1);
        System.out.println(new String(d,2,1));
        System.out.println(bf.position());
        System.out.println(bf.limit());
        System.out.println(bf.capacity());
        bf.reset();
        System.out.println(bf.position());
        bf.clear();
        System.out.println(bf.position());
        System.out.println(bf.limit());
        System.out.println(bf.capacity());
    }

    //使用非直接缓冲区
    @Test
    public void test2() throws IOException {
        FileOutputStream fos = null;
        FileInputStream fis = null;
        FileChannel incha = null;
        FileChannel outcha = null;
        try {
            fos = new FileOutputStream("2.png");
            fis = new FileInputStream("1.png");
            incha = fis.getChannel();
            outcha = fos.getChannel();
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            while (incha.read(byteBuffer)!=-1){
                byteBuffer.flip();
                outcha.write(byteBuffer);
                byteBuffer.clear();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        outcha.close();
        incha.close();
        fos.close();
        fis.close();
    }

    //使用直接缓冲区(内存映射文件)
    @Test
    public void test3() throws Exception {
        FileChannel incha = FileChannel.open(Paths.get("1.png"), StandardOpenOption.READ);
        FileChannel outcha = FileChannel.open(Paths.get("3.png"),StandardOpenOption.WRITE,StandardOpenOption.READ,StandardOpenOption.CREATE);

        MappedByteBuffer inmap = incha.map(FileChannel.MapMode.READ_ONLY, 0, incha.size());
        MappedByteBuffer outmap = outcha.map(FileChannel.MapMode.READ_WRITE, 0, incha.size());

        byte[] dis = new byte[inmap.limit()];
        inmap.get(dis);
        outmap.put(dis);

        incha.close();
        outcha.close();
    }

    //通道之间的数据传输（直接缓冲区）
    @Test
    public void test4() throws Exception {
        FileChannel incha = FileChannel.open(Paths.get("1.png"), StandardOpenOption.READ);
        FileChannel outcha = FileChannel.open(Paths.get("5.png"),StandardOpenOption.WRITE,StandardOpenOption.READ,StandardOpenOption.CREATE);
        incha.transferTo(0,incha.size(),outcha);
        outcha.transferFrom(incha,0,incha.size());
        incha.close();
        outcha.close();
    }

    //分散与聚集
    @Test
    public void test5() throws IOException {
        RandomAccessFile randomAccessFile = new RandomAccessFile("input.txt","rw");

        FileChannel channel = randomAccessFile.getChannel();

        ByteBuffer bf1 = ByteBuffer.allocate(5);
        ByteBuffer bf2 = ByteBuffer.allocate(10);

        ByteBuffer[] bfs = {bf1,bf2};

        channel.read(bfs);
        for(ByteBuffer byteBuffer:bfs){
            byteBuffer.flip();
        }
        System.out.println(new String(bf1.array(),0,bf1.limit()));
        System.out.println("------------------");
        System.out.println(new String(bf2.array(),0,bf2.limit()));

        RandomAccessFile randomAccessFile1 = new RandomAccessFile("input_2.txt","rw");
        FileChannel channel1 = randomAccessFile1.getChannel();
        channel1.write(bfs);
    }

    //字符集
    @Test
    public void test6(){
       Map<String, Charset> map = Charset.availableCharsets();
        Set<Map.Entry<String, Charset>> entries = map.entrySet();
        for(Map.Entry<String,Charset> entry:entries){
            System.out.println(entry.getKey()+":"+entry.getValue());
        }
    }

    //编码与解码
    @Test
    public void test7() throws Exception {
        Charset c1 = Charset.forName("GBK");
        //获取编码器
        CharsetEncoder ce = c1.newEncoder();
        //获取解码器
        CharsetDecoder cd = c1.newDecoder();
        CharBuffer cbf = CharBuffer.allocate(100);
        cbf.put("平安健康快乐");
        cbf.flip();
        ByteBuffer bBuf = ce.encode(cbf);
//        bBuf.flip();
        CharBuffer cBuf = cd.decode(bBuf);
        System.out.println(cBuf.toString());
    }
}
