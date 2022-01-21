package kr.co.kdone.airone.data;

import kr.co.kdone.airone.utils.CommonUtils;

import static kr.co.kdone.airone.utils.ProtocolType.PROTOCOL_VER_ID1;
import static kr.co.kdone.airone.utils.ProtocolType.STX;

/**
 * UDP 통신을 위한 패킷
 */

public class MicomPacket {

    int stx = STX;
    int src;
    int dst;
    int ver = PROTOCOL_VER_ID1;
    int[] productID ;
    int cmd;
    int len =0;
    int[] data;
    int crc = 0xFFFF;

    public MicomPacket(int src, int dst, int cmd){
        this.src = src;
        this.cmd = cmd;
        this.dst = dst;
    }

    public MicomPacket(int src, int dst, int[] productID, int cmd, int len, int[] data){
        this.src = src;
        this.cmd = cmd;
        this.dst = dst;
        if(productID !=null){
            this.productID = new int[productID.length];
            System.arraycopy(productID,0,this.productID,0,productID.length);
        }

        if(data != null){
            this.len = data.length;
            this.data = new int[data.length];
            System.arraycopy(data,0,this.data,0,data.length);
        }
    }

    public MicomPacket(int src, int dst, int[] productID, int cmd, int[] data){
        this.src = src;
        this.cmd = cmd;
        this.dst = dst;
        if(productID !=null){
            this.productID = new int[productID.length];
            System.arraycopy(productID,0,this.productID,0,productID.length);
        }

        if(data != null){
            this.len = data.length;
            this.data = new int[data.length];
            System.arraycopy(data,0,this.data,0,data.length);
        }
    }

    public byte[] toBytes(){
        int dLen;
        if(data == null){
            dLen = 0;
        } else {
            dLen = data.length;
        }
        byte[] returnData = new byte[9+8+dLen];
        int index = 0;
        returnData[index++] = (byte)stx;
        returnData[index++] = (byte)src;
        returnData[index++] = (byte)dst;
        returnData[index++] = (byte)ver;


        if(productID !=null && productID.length == 8){
            for(int p:productID){
                returnData[index++] = (byte)p;
            }
        } else if (productID !=null && productID.length < 8){
            for(int p:productID){
                returnData[index++] = (byte)p;
            }
            for(int i = productID.length; i < 8 ; i++){
                returnData[index++] = (byte)0x00;
            }
        } else if (productID !=null && productID.length > 8){
            for(int i = 0; i < 8 ; i++){
                returnData[index++] = (byte)productID[i];
            }
        }
        else {
            for(int i = 0; i<8;i++){
                returnData[index++] = (byte)0;
            }
        }

        returnData[index++] = (byte)cmd;
        returnData[index++] = (byte)(len>>8);
        returnData[index++] = (byte)len;

        if(data !=null){
            for(int d:data){
                returnData[index++] = (byte)d;
            }
        }

        crc = CommonUtils.updateCRC(returnData,returnData.length-2);

        returnData[index++] = (byte)(crc>>8);
        returnData[index++] = (byte)crc;

        return returnData;
    }
}
