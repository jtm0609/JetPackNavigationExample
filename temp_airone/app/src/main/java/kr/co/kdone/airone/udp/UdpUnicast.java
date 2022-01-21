package kr.co.kdone.airone.udp;

import android.os.StrictMode;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import kr.co.kdone.airone.utils.CommonUtils;

public class UdpUnicast implements INetworkTransmission {

    private static final String TAG = "UdpUnicast";
    private static final int BUFFER_SIZE = 2048;

    private String ip;
    private int port = 48899;
    private DatagramSocket socket;
    private DatagramPacket packetToSend;
    private InetAddress inetAddress;
    private ReceiveData receiveData;
    private UdpUnicastListener listener;
    private byte[] buffer = new byte[BUFFER_SIZE];

    /**
     * @return the ip
     */
    public String getIp() {
        return ip;
    }

    /**
     * @param ip the ip to set
     */
    public void setIp(String ip) {
        this.ip = ip;
    }

    /**
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * @param port the port to set
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * @param listener the listener to set
     */
    public void setListener(UdpUnicastListener listener) {
        this.listener = listener;
    }

    /**
     * @return the listener
     */
    public UdpUnicastListener getListener() {
        return listener;
    }

    public UdpUnicast(String ip, int port) {
        this();
        this.ip = ip;
        this.port = port;
    }

    public UdpUnicast() {
        super();
        forceStrictMode();
    }

    /**
     * Open udp socket
     */
    public synchronized boolean open() {

        try {
            inetAddress = InetAddress.getByName(ip);
            CommonUtils.customLog(TAG, "open_inetAddress : " + inetAddress, Log.ERROR);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return false;
        }

        try {
            if (socket != null) {
                socket.close();
                socket = null;
                CommonUtils.customLog(TAG, "open socket is not null", Log.ERROR);
            }
            socket = new DatagramSocket(null);
            socket.setReuseAddress(true);
            socket.setSoTimeout(100000);
            socket.bind(new InetSocketAddress(port));
        } catch (SocketException e) {
            e.printStackTrace();
            return false;
        }

        //receive response
        try {
            if(null != receiveData){
                receiveData.stop();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        receiveData = new ReceiveData();
        receiveData.start();
        return true;
    }

    /**
     * Close udp socket
     */
    public synchronized void close() {
        listener = null;
        stopReceive();
        if (socket != null) {
            socket.close();
        }
    }

    /**
     * send message
     *
     * @param text the message to broadcast
     */
    public synchronized boolean send(String text) {
        if (socket == null) {
            return false;
        }

        if (text == null) {
            return true;
        }

        CommonUtils.customLog(TAG, "send String lastCommand : " + text + "inetAddress : " + inetAddress + " / port : " + port, Log.ERROR);

        packetToSend = new DatagramPacket(text.getBytes(), text.getBytes().length, inetAddress, port);

        //send data
        try {
            socket.send(packetToSend);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            CommonUtils.customLog(TAG, "socket Send error : " + e.toString(), Log.ERROR);
            return false;
        }
    }

    public synchronized boolean send(byte[] bytes) {
        String t = "";

        if (bytes == null) {
            return true;
        }

        if (socket == null) {
            return false;
        }

        packetToSend = new DatagramPacket(bytes, bytes.length, inetAddress, port);

        for (int i = 0; i < bytes.length; i++) {
            t += String.format("%02x ", bytes[i]);
        }

        CommonUtils.customLog(TAG, "sendData : " + t + " inetAddress : " + inetAddress + " / port : " + port, Log.ERROR);

        //send data
        try {
            if (socket.isClosed()) {
                CommonUtils.customLog(TAG, "socket is closed", Log.ERROR);
                return false;
            }

            socket.send(packetToSend);

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            CommonUtils.customLog(TAG, "SEND IOException reason : " + e.toString(), Log.ERROR);
            return false;
        }
    }

    /**
     * Stop to receive
     */
    public void stopReceive() {
        if (receiveData != null) {
            receiveData.stop();
        }
    }

    public interface UdpUnicastListener {
        public void onReceived(byte[] data, int length);

        public void onError(Exception e);
    }

    private class ReceiveData implements Runnable {

        private boolean stop;
        private Thread thread;

        private ReceiveData() {
            if (thread != null) {
                thread.interrupt();
                thread = null;

                CommonUtils.customLog(TAG, "ReceiveData thread not null", Log.ERROR);
            }
            thread = new Thread(this);
        }

        @Override
        public void run() {

            while (!stop) {
                try {
                    DatagramPacket packetToReceive = new DatagramPacket(buffer, BUFFER_SIZE);
                    socket.receive(packetToReceive);
                    onReceive(buffer, packetToReceive.getLength());

                    String d = new String(buffer, 0, packetToReceive.getLength());
                    CommonUtils.customLog(TAG, "ReceiveData receievd : " + d, Log.ERROR);
                } catch (SocketTimeoutException e) {
                    onError(e);
                    CommonUtils.customLog(TAG, "Receive packet timeout!", Log.ERROR);
                } catch (IOException e1) {
                    onError(e1);
                    CommonUtils.customLog(TAG, "ReceivedData Socket is closed!", Log.ERROR);
                }
            }
        }

        void start() {
            thread.start();
        }

        void stop() {
            stop = true;

            try {
                if(null != thread && !thread.isInterrupted()) thread.interrupt();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        boolean isStoped() {
            return stop;
        }
    }

    @Override
    public void setParameters(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    @Override
    public void onReceive(byte[] buffer, int length) {
        if (listener != null) {
            listener.onReceived(buffer, length);
        }
    }

    @Override
    public void onError(Exception e) {
        if (listener != null) {
            listener.onError(e);
        }
    }

    private void forceStrictMode() {
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
    }
}
