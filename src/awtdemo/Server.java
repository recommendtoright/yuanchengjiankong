package awtdemo;



import java.awt.AWTException;
import java.awt.Event;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.*;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

import awtdemo.KeyInfo;

/**
 * 服务端QQ563095756
 *
 */
public class Server implements Serializable {
	private static Properties  properties=new Properties();
	private static ServertWindow servertWindow;
    private static String fileDir="c:/";
    public static void main(String[] args) throws IOException  {
    	servertWindow=new ServertWindow();
    	 servertWindow.repaintMessage("读取配置");
    	new Server().getProperties("init.properties");
    	int port=Integer.parseInt(properties.getProperty("port"));
    	int sleepTime=Integer.parseInt(properties.getProperty("sleepTime"));
        fileDir=properties.getProperty("fileSaveDir");
    	
        ServerSocket server = null;
		try {
			server = new ServerSocket(port);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			 servertWindow.repaintMessage("端口启动异常"+e.getMessage());
			return;
		}
        servertWindow.repaintMessage("服务器已经正常启动");
        servertWindow.repaintMessage("本机地址端口:"+getHostIp()+":"+port);
        do {
        	servertWindow.repaintMessage("监听客户端");
            Socket socket = server.accept();//等待接收请求,阻塞方法
            System.out.println("有客户端连接，地址:" + socket.getInetAddress().getHostAddress());
            servertWindow.repaintMessage("有客户端连接，地址:" + socket.getInetAddress().getHostAddress());
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            //将客户端与服务器端链接的输出流交个ImageThread处理
            ImageThread imageThreadRun = new ImageThread(dos,sleepTime);
            Thread imageThread=new     Thread(imageThreadRun);
            imageThread.start();

            new Thread(new EventThread(servertWindow,socket.getInputStream(),fileDir)).start();
        } while (true);

    }
    /**
     * 获取本机ip地址不是0:0:0那个地址
     * @return
     */
    private static String getHostIp(){
		try{
			Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
			while (allNetInterfaces.hasMoreElements()){
				NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
				Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
				while (addresses.hasMoreElements()){
					InetAddress ip = (InetAddress) addresses.nextElement();
					if (ip != null 
							&& ip instanceof Inet4Address
                    		&& !ip.isLoopbackAddress() //loopback地址即本机地址，IPv4的loopback范围是127.0.0.0 ~ 127.255.255.255
                    		&& ip.getHostAddress().indexOf(":")==-1){
						System.out.println("本机的IP = " + ip.getHostAddress());
						return ip.getHostAddress();
					} 
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
    
    public  void  getProperties(String fileName) {
    	InputStream iFile;
		try {
			iFile= this.getClass().getClassLoader().getResourceAsStream(fileName);
			properties.load(iFile);
			iFile.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			servertWindow.repaintMessage("文件打开异常");
			System.exit(-1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			servertWindow.repaintMessage("文件读取异常");
			System.exit(-1);
		}
		
    
    }
}


class ServertWindow extends JFrame implements Serializable {
	
	private JTextArea area;
	
	
	
		public void repaintMessage(String str) {
			String str1=area.getText();
			area.setText(str1+"\n"+str);
			this.repaint();
		}
		
		public ServertWindow() {
			this.setTitle("控制程序服务端");
			area = new JTextArea();
			area.setSize(300, 300);
			area.setEditable(false);
			JPanel p = new JPanel();
			p.add(area);
			JScrollPane scroll = new JScrollPane(p);// 给p面板添加滚动条
			this.add(scroll);
			this.setSize(300, 300);
			this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			this.setVisible(true);
			
		}
}


/**
 * 用来处理接收过来的鼠标事件或者键盘事件
 */
class EventThread implements Runnable, Serializable {
    private ObjectInputStream ois;
    private Robot robot;
    InputStream stream;
    private String fileDir;
    private ServertWindow servertWindow;
    public EventThread(ServertWindow servertWindow,InputStream is ,String fileDir) {
        try {
            this.servertWindow=servertWindow;
            this.stream=is;
            this.fileDir=fileDir;
            this.ois = new ObjectInputStream(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {
        try {
            robot = new Robot();
            while (true) {
                Object o=null;
                   o  = ois.readObject();
                if (o instanceof MouseWheelEvent) {
                    mouseWheelEvent((MouseWheelEvent) o);
                } else if (o instanceof KeyInfo) {
                    keyEvent((KeyInfo) o);//处理鼠标滚轮事件
                } else if (o instanceof MouseEvent) {
                    mouseEvent((MouseEvent) o);//处理鼠标事件
                }else{
                    FileSendEvent(o);
                }

            }
        } catch (AWTException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                ois.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void FileSendEvent(Object o)throws  Exception{
        Map o1 = (Map) o;

        FileSendThread fileSendThreadRun=new FileSendThread(servertWindow,o1,fileDir);
        servertWindow.repaintMessage("接收文件，开始文件传输");
            fileSendThreadRun.setSend(true);
            Thread fileSendThread=new Thread(fileSendThreadRun);
            fileSendThread.start();

    }
    /**
     * 鼠标事件
     */
    public void mouseEvent(MouseEvent e) {

        int type = e.getID();
        if (type == Event.MOUSE_MOVE) {//鼠标移动草走
            robot.mouseMove(e.getX(), e.getY());
        } else if (type == Event.MOUSE_DOWN) {//鼠标按下操作
            type = getMouseKey(e.getButton());
            robot.mousePress(type);
        } else if (type == Event.MOUSE_UP) {//鼠标按上来操作
            robot.mouseRelease(getMouseKey(e.getButton()));
        } else if (type == Event.MOUSE_DRAG) {//鼠标按住移动操作
            int x = e.getX();
            int y = e.getY();
            robot.mouseMove(x, y);//鼠标拖动
        }
    }


    /**
     * 滚轮事件
     */
    public void mouseWheelEvent(MouseWheelEvent e) {
        if (e.getPreciseWheelRotation() > 0) {//鼠标向上滑动
            robot.mouseWheel(1);
        } else {//鼠标向下滑动
            robot.mouseWheel(-1);
        }
    }

    /**
     * 键盘事件
     */
    public void keyEvent(KeyInfo info) {
        int type = info.getEvent();//拿到事件类型
        if (type == Event.KEY_PRESS) {
            robot.keyPress(info.getKey_code());
        } else if (type == Event.KEY_RELEASE) {
            robot.keyRelease(info.getKey_code());

        }
    }

    /**
     * 返回鼠标的真正事件，鼠标时间不能直接处理，需要进过转换
     *
     */
    public int getMouseKey(int button) {
        if (button == MouseEvent.BUTTON1) {//鼠标左键
            return InputEvent.BUTTON1_MASK;
        } else if (button == MouseEvent.BUTTON2) {//鼠标右键
            return InputEvent.BUTTON2_MASK;
        } else if (button == MouseEvent.BUTTON3) {//滚轮
            return InputEvent.BUTTON3_MASK;
        } else {
            return 0;
        }
    }

}
