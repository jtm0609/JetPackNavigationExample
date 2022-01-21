package kr.co.kdone.airone.udp;

public interface INetworkTransmission {

	public void setParameters(String ip, int port);
	public boolean open();
	public void close();
	public boolean send(String text);
	public void onReceive(byte[] buffer, int length);
	public void onError(Exception e);
}
