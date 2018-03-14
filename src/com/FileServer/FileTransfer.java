package com.FileServer;

import java.net.*;
import java.io.*;
import java.util.*;

/**
 * Created by Sar-Kerson on 2017/7/6.
 */
public class FileTransfer implements IOStrategy{
    //һ�����ļ��ľ�̬ͬ���ϼ�
    static Map<String, FileInfo> fileInfoMap = Collections.synchronizedMap(new HashMap<>());
    //����Ӧ��Ҫ��һ����ڵ�ľ�̬ͬ���ϼ�
    static List<StorageInfo> storageInfos = Collections.synchronizedList(new LinkedList<>());

    public void service(Socket socket) {
        try {
            DataInputStream dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

            int command = dis.readInt();

            switch (command) {
                //�ϴ��ļ�
                case 1:
                    try{
                        String UUID = Tools2.getUUID(); //��ȡUUID
                        String fileName = dis.readUTF();//��ȡ�ļ�ԭʼ��
                        long length = dis.readLong();//��ȡ�ļ���С
                        String mainHost = "127.0.0.1";//Ԥ��ָ��һЩ��Чֵ
                        int mainPort = 9000;
                        String backHost = "127.0.0.1";
                        int backPort = 9000;

                        //���ݽ�㸺������
                        Collections.sort(storageInfos, Collections.reverseOrder());
                        //������
                        if(storageInfos.size() > 2) {
                            if(storageInfos.get(0).isOK) {
                                mainHost = storageInfos.get(0).host;
                                mainPort = storageInfos.get(0).port;
                                storageInfos.get(0).volumeLeft -= length;
                                storageInfos.get(0).fileNumber++;
                            }else {
                                System.out.println("No StorageNode work");
                            }
                            if(storageInfos.get(1).isOK) {
                                backHost = storageInfos.get(1).host;
                                backPort = storageInfos.get(1).port;
                                storageInfos.get(1).volumeLeft -= length;
                                storageInfos.get(1).fileNumber++;
                            }else {
                                System.out.println("No Back StorageNode");
                            }
                        }else if(storageInfos.size() == 1) {
                            if(storageInfos.get(0).isOK) {
                                mainHost = storageInfos.get(0).host;
                                mainPort = storageInfos.get(0).port;
                                storageInfos.get(0).volumeLeft -= length;
                                storageInfos.get(0).fileNumber++;
                            }else {
                                System.out.println("No StorageNode work");
                            }

                        }else {
                            System.out.println("No StorageNode exit");
                            System.exit(1);
                        }

                        //����UUID��newһ��fileInfo���󣬴浽��̬ͬ���ϼ���
                        fileInfoMap.put(UUID, new FileInfo(UUID, fileName, length, mainHost, mainPort, backHost, backPort));
                        //System.out.println("new fileInfo succeed");

                        //����Ϣ���л����ļ���洢
                        ObjectOutputStream output = null;
                        try {
                            output = new ObjectOutputStream(new FileOutputStream("fileInfo.dat"));
                            output.writeObject(fileInfoMap);
                            output.flush();
                            output.close();
                        }catch (IOException e) {
                            if(output != null) {
                                output.close();
                            }
                            System.out.println("File Serializable failed");
                        }
                        ObjectOutputStream output1 = null;
                        try {
                            output1 = new ObjectOutputStream(new FileOutputStream("storageInfo.dat"));
                            output1.writeObject(storageInfos);
                            output1.flush();
                            output1.close();
                        }catch (IOException e) {
                            if(output1 != null) {
                                output1.close();
                            }
                            System.out.println("File Serializable failed");
                        }

                        dos.writeUTF(UUID);//���ر�����
                        dos.writeUTF(mainHost);//����Storage���ڵ��ַ
                        dos.writeInt(mainPort);//����Storage���ڵ�˿�
                        dos.writeUTF(backHost);//���ر��ýڵ��ַ
                        dos.writeInt(backPort);//���ر��ýڵ�˿�
                        dos.flush();

                        System.out.println(UUID);
                        System.out.println(mainHost);
                        System.out.println(mainPort);
                        System.out.println(backHost);
                        System.out.println(backPort);

                        socket.close();
                    }catch (Exception e) {
                        socket.close();
                        System.out.println("File upload failed");
                    }
                    break;
                //�����ļ�
                case 2:
                    try{
                        String UUID = dis.readUTF();//��ȡ�ļ�������

                        System.out.println(UUID);

                        FileInfo fileInfo = fileInfoMap.get(UUID);//��ȡ�ļ���Ϣ����

                        System.out.println(fileInfo.fileName);

                        if(fileInfo == null) {
                            dos.writeBoolean(false);
                        }else {
                            dos.writeBoolean(true);
                        }
                        dos.flush();

                        dos.writeUTF(fileInfo.fileName);//��֪ԭʼ�ļ���
                        dos.writeLong(fileInfo.length);//��֪����
                        dos.writeUTF(fileInfo.mainHost);//��֪���ڵ��ַ
                        dos.writeInt(fileInfo.mainPort);//��֪�˿ں�
                        dos.writeUTF(fileInfo.backHost);//��֪���ýڵ��ַ
                        dos.writeInt(fileInfo.backPort);//��֪���ýڵ�˿�
                        dos.flush();

                        System.out.println(fileInfo.fileName);

                        socket.close();
                    }catch (Exception e) {
                        socket.close();
                        System.out.println("File download failed");
                    }

                    break;
                //ɾ���ļ�
                case 3:
                    try{
                        String UUID = dis.readUTF();//��ȡ�ļ�������
                        FileInfo fileInfo = fileInfoMap.get(UUID);//��ȡ�ļ���Ϣ����

                        if(fileInfo == null) {
                            dos.writeBoolean(false);
                        }else {
                            dos.writeBoolean(true);
                        }
                        dos.flush();

                        System.out.println("write succeed");

                        dos.writeUTF(fileInfo.mainHost);//��֪���ڵ��ַ
                        dos.writeInt(fileInfo.mainPort);//��֪�˿ں�
                        dos.writeUTF(fileInfo.backHost);//��֪���ýڵ��ַ
                        dos.writeInt(fileInfo.backPort);//��֪���ýڵ�˿�
                        dos.flush();

                        //���Ĵ洢�ڵ��fileNumber��volumeLeft����Ϣ
                        for(StorageInfo element : storageInfos) {
                            if(element.host.equals(fileInfo.mainHost)
                                    && element.port == fileInfo.mainPort) {
                                element.fileNumber--;
                                element.volumeLeft += fileInfo.length;
                            }
                            if(element.host.equals(fileInfo.backHost)
                                    && element.port == fileInfo.backPort) {
                                element.fileNumber--;
                                element.volumeLeft += fileInfo.length;
                            }
                        }

                        System.out.println("Changed succeed");

                        fileInfoMap.remove(UUID);//���ļ��б��ｫ��ɾ��

                        ObjectOutputStream output = null;
                        try {
                            output = new ObjectOutputStream(new FileOutputStream("fileInfo.dat"));
                            output.writeObject(fileInfoMap);
                            output.flush();
                            output.close();
                        }catch (IOException e) {
                            if(output != null) {
                                output.close();
                            }
                            System.out.println("File Serializable failed");
                        }
                        ObjectOutputStream output1 = null;
                        try {
                            output1 = new ObjectOutputStream(new FileOutputStream("storageInfo.dat"));
                            output1.writeObject(storageInfos);
                            output1.flush();
                            output1.close();
                        }catch (IOException e) {
                            if(output1 != null) {
                                output1.close();
                            }
                            System.out.println("File Serializable failed");
                        }

                        socket.close();
                        System.out.println("File Serializable succeed");

                    }catch (Exception e) {
                        socket.close();
                        System.out.println("File delete failed");
                    }

                    break;
                //��س��������ļ���Ϣ
                case 4:
                    try{
                        Set<FileInfo> fileInfoSet = new HashSet<>(fileInfoMap.values());

                        dos.writeInt(fileInfoMap.size());//�����ļ���Ϣӳ���Ĵ�С
                        dos.flush();

                        for(FileInfo element : fileInfoSet) {
                            dos.writeUTF(element.UUID);
                            dos.writeUTF(element.fileName);
                            dos.writeLong(element.length);
                            dos.writeUTF(element.mainHost);
                            dos.writeInt(element.mainPort);
                            dos.writeUTF(element.backHost);
                            dos.writeInt(element.backPort);
                            dos.flush();
                        }

                        socket.close();
                    }catch (Exception e) {
                        socket.close();
                        System.out.println("Monitor fileInoformation failed");
                    }

                    //��س�������ڵ���Ϣ
                case 5:
                    try{
                        dos.writeInt(storageInfos.size());//���ؽڵ���Ϣ���Ա�Ĵ�С
                        dos.flush();

                        for(StorageInfo element : storageInfos) {
                            dos.writeUTF(element.nodeName);
                            dos.writeUTF(element.host);
                            dos.writeInt(element.port);
                            dos.writeLong(element.volume);
                            dos.writeLong(element.volumeLeft);
                            dos.writeInt(element.fileNumber);
                            dos.writeBoolean(element.isOK);
                            dos.flush();
                        }
                        socket.close();

                    }catch (Exception e) {
                        socket.close();
                        System.out.println("Monitor storageInformation failed");
                    }
            }
            System.out.println("File transfer succeed");
        }catch (Exception e) {
            System.out.println("File transfer failed.");
        }
    }
}