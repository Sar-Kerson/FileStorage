package com.FileServer;

import java.io.*;
import java.net.*;
import java.util.*;

public class ThreadPoolSupport implements IOStrategy { // ThreadPoolSupport.java
	private ArrayList threads = new ArrayList();
	private UDPThread udpThread = new UDPThread();
	private final int INIT_THREADS = 50;
	private final int MAX_THREADS = 100;
	private IOStrategy ios = null;

	public ThreadPoolSupport(IOStrategy ios) { // �����̳߳�
		//���ļ������л���ȡ�洢�ڵ���Ϣ
        File file = new File("storageInfo.dat");
        try
        {
			ObjectInputStream input = new ObjectInputStream(new FileInputStream(file));
            FileTransfer.storageInfos = (LinkedList<StorageInfo>) input.readObject();
            System.out.println("StorageNode imformation: ");
            for(int i = 0; i < FileTransfer.storageInfos.size(); i++) {
                System.out.println(FileTransfer.storageInfos.get(i).nodeName + ' ' +
                        FileTransfer.storageInfos.get(i).host + ' ' +
                        FileTransfer.storageInfos.get(i).port + ' ' +
                        FileTransfer.storageInfos.get(i).volume + ' ' +
                        FileTransfer.storageInfos.get(i).volumeLeft + ' ' +
                        FileTransfer.storageInfos.get(i).fileNumber + ' ' +
                        FileTransfer.storageInfos.get(i).isOK);
            }
            input.close();
        }
        catch (Exception e)
        {
        	System.out.println("storageInfo get falied");
            e.printStackTrace();
        }

        //���ļ������л���ȡ�ļ���Ϣ
        File file1 = new File("fileInfo.dat");
        try
        {
            //ִ�з����л���ȡ
			ObjectInputStream input = new ObjectInputStream(new FileInputStream(file1));
            FileTransfer.fileInfoMap = (HashMap)input.readObject();
            input.close();
        }
        catch (Exception e)
        {
        	System.out.println("fileInfo get failed");
            e.printStackTrace();
        }

        udpThread.start();

		this.ios = ios;
		for (int i = 0; i < INIT_THREADS; i++) {
			IOThread2 t = new IOThread2(ios); // ����Э����󣬵��ǻ�û��socket
			t.start(); // �����̣߳������߳������wait
			threads.add(t);
		}
		try {
			Thread.sleep(300);
		} catch (Exception e) {
		} // �ȴ��̳߳ص��̶߳������С�
	}

	public void service(Socket socket) { // �����̳߳أ��ҵ�һ�����е��̣߳��ѿͻ��˽�������������
		IOThread2 t = null;
		boolean found = false;
		for (int i = 0; i < threads.size(); i++) {
			t = (IOThread2) threads.get(i);
			if (t.isIdle()) {
				found = true;
				break;
			}
		}
		if (!found) // �̳߳��е��̶߳�æ��û�а취�ˣ�ֻ�д���
		{ // һ���߳��ˣ�ͬʱ��ӵ��̳߳��С�
			if(threads.size() < MAX_THREADS) {
				t = new IOThread2(ios);
				t.start();
				try {
					Thread.sleep(300);
				} catch (Exception e) {
				}
				threads.add(t);
			}else{
				//����д

			}

		}
		t.setSocket(socket); // ���������˵�socket���󴫵ݸ�������е��߳�,���俪ʼִ��Э�飬��ͻ����ṩ����
	}
}
