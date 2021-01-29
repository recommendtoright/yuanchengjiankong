package awtdemo;


//屏幕截取器和发送器，这里需要拿到socket的out流
import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.sun.image.codec.jpeg.*;
/**
* 用来将图片数据发送
* @author 哑元
*
*/
public class ImageThread implements Runnable{
DataOutputStream dos = null;    //数据输出流
int sleepTime=50;
public ImageThread(DataOutputStream dos,int sleepTime){
    this.dos = dos;
    this.sleepTime=sleepTime;
}
@Override
public void run() {
    try {
        Robot robot = new Robot();
        //截取整个屏幕
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        /*
        int width = (int)dimension.getWidth();
        int height = (int)dimension.getWidth();
        Rectangle rec = new Rectangle(0,0,width,height);
        */
        Rectangle rec = new Rectangle(dimension);
        BufferedImage image;
        byte imageBytes[];
        while(true){
            image = robot.createScreenCapture(rec);
            imageBytes = getImageBytes(image);
            dos.writeInt(imageBytes.length);
            dos.write(imageBytes);
            dos.flush();
            Thread.sleep(sleepTime);   //线程睡眠
        }

    } catch (AWTException e) {
        e.printStackTrace();
    } catch (ImageFormatException e) {
        e.printStackTrace();
    } catch (IOException e) {
        e.printStackTrace();
    } catch (InterruptedException e) {
        e.printStackTrace();
    }finally{
        try {
            if(dos!= null)  dos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
/**
 *  压缩图片
 * @param 需要压缩的图片
 * @return 压缩后的byte数组
 * @throws IOException 
 * @throws ImageFormatException 
 */
public byte[] getImageBytes(BufferedImage image) throws ImageFormatException, IOException{
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    //压缩器压缩，先拿到存放到byte输出流中
    JPEGImageEncoder jpegd = JPEGCodec.createJPEGEncoder(baos);
    //将iamge压缩
    jpegd.encode(image);
    //转换成byte数组
    return baos.toByteArray();  
}

}