package face.camera.beans.ble;

import android.os.Handler;
import android.os.Message;
import android.widget.Button;
import android.widget.ImageButton;

public class SendMessage {

	/**
	 * 发送各种消息
	 * 
	 * @param shandler
	 * @param str
	 * @param type
	 */

	// 连接信息
	public static void sendMessage(Handler shandler, String str, int type) {
		Message msg_listData = new Message();
		msg_listData.what = type;
		msg_listData.obj = str;
		shandler.sendMessage(msg_listData);
	}

	// 数据信息
	public static void sendMessage(Handler shandler, byte[] data, int type) {
		Message msg_listData = new Message();
		msg_listData.what = type;
		msg_listData.obj = data;
		shandler.sendMessage(msg_listData);
	}

	// 按键信息
	public static void sendMessage(Handler shandler, Button data,
                                   int type) {
		Message msg_listData = new Message();
		msg_listData.what = type;
		msg_listData.obj = data;
		shandler.sendMessage(msg_listData);
	}

	// 信号强度
	public static void sendMessage(Handler shandler, int data, int type) {
		Message msg_listData = new Message();
		msg_listData.what = type;
		msg_listData.obj = data;
		shandler.sendMessage(msg_listData);
	}

	// 额外按键信息
	public static void sendMessage(Handler shandler, ImageButton data, int type) {
		Message msg_listData = new Message();
		msg_listData.what = type;
		msg_listData.obj = data;
		shandler.sendMessage(msg_listData);
	}

}
