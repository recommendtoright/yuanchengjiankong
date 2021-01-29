package awtdemo;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;


/**
 * 客户端
 */
public class Client {

	public static void main(String args[])  {
		 String input=JOptionPane.showInputDialog("请输入地址:端口");
		//String input="192.168.120.239:8088";
		 String ip="";
		 int port=0;
		 if(null==input|| "".equals(input.trim())) {
			 JOptionPane.showMessageDialog(null, "地址初始失败" , "地址初始失败" ,JOptionPane.ERROR_MESSAGE);
			 return;
		 }
		 String[] l=input.split(":");
		 if(l.length!=2) {
			 JOptionPane.showMessageDialog(null, "地址初始失败" , "写入格式错误" ,JOptionPane.ERROR_MESSAGE);
			 return;
		 }
		 try {
			 ip=l[0];
			 port=Integer.parseInt(l[1]);
		 }catch (Exception e) {
			// TODO: handle exception
			 JOptionPane.showMessageDialog(null, "地址初始失败" , "地址端口初始化失败" ,JOptionPane.ERROR_MESSAGE);
			 return;
		}
		
		 
		Socket s = null;
		try {
			s = new Socket(ip, port);
		} catch (UnknownHostException e) {
			// TODO: handle exception
			 JOptionPane.showMessageDialog(null, "网络连接失败" , "网络连接失败" ,JOptionPane.ERROR_MESSAGE);
			 return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			 JOptionPane.showMessageDialog(null, "网络连接失败" , "IO读取失败" ,JOptionPane.ERROR_MESSAGE);
			 return;
		}
		try {
			DataInputStream dis = new DataInputStream(s.getInputStream());
			ClientWindow cw = new ClientWindow(s.getOutputStream(),s.getInputStream());
			byte[] imageBytes;
			while (true) {
				imageBytes = new byte[dis.readInt()]; // 先拿到传过来的数组长度
				dis.readFully(imageBytes); // 所有的数据存放到byte中
				cw.repainImage(imageBytes);
			}
		}catch (Exception e) {
			// TODO: handle exception
			JOptionPane.showMessageDialog(null, "远程失败" , "远程过程失败："+e.getMessage() ,JOptionPane.ERROR_MESSAGE);
			 return;
		}
	
	}
}

/**
 * 客户端窗体
 */
class ClientWindow extends JFrame implements Serializable {
	private ObjectOutputStream oos;
	private OutputStream socketOutputStream;
	private InputStream socketInputStream;
	private JLabel label;
	JButton sendButton;
	JButton endSendButton;
	// 重写背景图片方法
	public void repainImage(byte[] imageBytes) {
		label.setIcon(new ImageIcon(imageBytes));
		this.repaint();
	}
	
	
	public ClientWindow(OutputStream ost,InputStream ist) throws IOException {
		
		this.socketOutputStream=ost;
		this.socketInputStream=ist;
		this.oos = new ObjectOutputStream(ost);
		this.setTitle("远程控制程序");
		label = new JLabel();
		JPanel bp = new JPanel();
		JPanel p = new JPanel();
		sendButton=new JButton("文件传输");
//		endSendButton=new JButton("远程页面不显示?点我试试");
		this.setLayout(new BorderLayout());
		p.add(label);
		bp.add(sendButton);
//		bp.add(endSendButton);
		JScrollPane scroll = new JScrollPane(p);// 给p面板添加滚动条
		this.add(bp,BorderLayout.NORTH);
		this.add(scroll,BorderLayout.CENTER);
		this.setSize(1024, 768);
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		this.setVisible(true);
		this.setLabelListener();//设置面板各种事件
		this.setButtonListener();
	}

	
	public void setButtonListener() {
//		endSendButton.addActionListener(new ActionListener() {
//			
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				// TODO Auto-generated method stub
//				sendFile("endFileSend");
//			}
//		});
		sendButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				sendFile("startFileSend");
			}
		});
	}
	
	/**
	 * 设置面板各种事件
	 */
	public void setLabelListener() {

		label.addKeyListener(new KeyListener() {
			
			
			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub
				System.out.println(2);
			}

			@Override
			public void keyReleased(KeyEvent e) {
				KeyInfo info = new KeyInfo(e.getID(), e.getKeyCode());
				System.out.println(1);
				sendEvent(info);
			}

			@Override
			public void keyPressed(KeyEvent e) {
				KeyInfo info = new KeyInfo(e.getID(), e.getKeyCode());
				sendEvent(info);
			}
		});
		label.addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseMoved(MouseEvent e) {
				// TODO Auto-generated method stub
				sendEvent(e);
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				// TODO Auto-generated method stub
				sendEvent(e);
			}
		});
		label.addMouseWheelListener(new MouseWheelListener() {
			
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				// TODO Auto-generated method stub
				sendEvent(e);
			}
		});
		label.addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {
				label.requestFocus();
				sendEvent(e);
					
			}

			@Override
			public void mousePressed(MouseEvent e) {
				sendEvent(e);

			}

			@Override
			public void mouseClicked(MouseEvent e) {
				sendEvent(e);

			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub

			}
		});
	}
	public void sendEvent(InputEvent event) {
		try {
			oos.writeObject(event);

		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public void sendEvent(KeyInfo event) {
		try {
			oos.writeObject(event);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	public void sendFile(Object o) {	
		try {
			//File file=new File("‪d://VOneManagerMan.exe");
			 String input=JOptionPane.showInputDialog("文件路径");
			File file=new File(input);
			if(!file.exists()|| file.isDirectory()) {
				JOptionPane.showMessageDialog(null, "文件选择失败" , "文件选择失败,请选择正确路径" ,JOptionPane.ERROR_MESSAGE);
				return;
			}
			String fileName=file.getName();
			long useableSpace=file.getUsableSpace();
			FileInputStream fis=new FileInputStream(file);
			
			ByteArrayOutputStream dos = new ByteArrayOutputStream();
            // 文件名和长度
//            dos.writeUTF(file.getName());
//            dos.writeLong(file.length());
            byte[] bytes = new byte[1024];
            int length = 0;
            length = fis.read(bytes, 0, 1024);
            while(length != -1) {
                dos.write(bytes, 0, length);
                length = fis.read(bytes, 0, 1024);
            }
            HashMap map=new HashMap<>();
            map.put("key", "send");
            map.put("filename", file.getName());
            map.put("filelength", file.getUsableSpace());	
            map.put("content", dos.toByteArray());
            oos.writeObject(map);
           // System.out.println("======== 文件传输成功 ========");
        	JOptionPane.showMessageDialog(null, "文件发送成功" , "文件发送成功" ,JOptionPane.INFORMATION_MESSAGE);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
}