package awtdemo;



import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class FileSendThread implements Runnable {
    private boolean send = false;
    private String fileDir = null;
    private Map fileMap = new HashMap<>();
    private ServertWindow servertWindow;
    FileSendThread(ServertWindow servertWindow,Map fileMap, String fileDir) {
        this.fileMap = fileMap;
        this.fileDir = fileDir;
        this.servertWindow=servertWindow;
    }

    @Override
    public void run() {
        File file = new File(fileDir);
        if (!file.exists()) {
            file.mkdirs();
        }
        try {
            byte[] bytes = (byte[]) fileMap.get("content");
            String fileName = (String) fileMap.get("filename");
            file = new File(fileDir+File.separator+fileName);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bytes);
            fos.flush();
            fos.close();
            this.send = false;
            servertWindow.repaintMessage("文件接收成功,路径地址："+fileDir+File.separator+fileName);
//            System.out.println("======== 文件接收成功 ========");
        } catch (IOException e) {
            e.printStackTrace();
            this.send = false;
            servertWindow.repaintMessage("文件接收失败,失败原因："+e.getMessage());
        } finally {
            this.send = false;
        }

    }

    public boolean isSend() {
        return send;
    }

    public void setSend(boolean send) {
        this.send = send;
    }


    public String getFileDir() {
        return fileDir;
    }

    public void setFileDir(String fileDir) {
        this.fileDir = fileDir;
    }
}
