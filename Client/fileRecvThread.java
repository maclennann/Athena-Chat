
import java.awt.Color;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.Socket;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Norm
 */
public class fileRecvThread extends Thread {
	String fromUser;
	String filePath;
	String fileSize;
	String toUser;
	String username;
	String socketIP;
	
	public fileRecvThread(String afromUser, String afilePath, String afileSize, String atoUser, String ausername, String connectIP){
		fromUser = afromUser;
		filePath = afilePath;
		fileSize = afileSize;
		toUser = atoUser;
		username = ausername;
		socketIP = connectIP;
	}

	public void run() {
		try{
			//JOptionPane.showMessageDialog(null, "Receiving a file.");
			Socket fileSocket = null;
			while(fileSocket == null){
				fileSocket = new Socket(socketIP, 7779);//"71.232.78.143", 7779);
			}
			//JOptionPane.showMessageDialog(null, "Connected to user.");
			InputStream is = fileSocket.getInputStream();

			byte[] mybytearray = new byte[Integer.parseInt(fileSize)];

			//Get the filename
			String filePathReplace = filePath.replace("\\", ",");
			String[] filePathArray = filePathReplace.split(",");
			int arrSize = filePathArray.length;

			//TODO Check to see if the downloads folder exists!
			FileOutputStream fos = new FileOutputStream("users/" + username + "/downloads/" + filePathArray[arrSize-1]);
			//JOptionPane.showMessageDialog(null, "Opened file for writiiiiing.");
			Progress frame = new Progress(filePathArray[arrSize-1],Integer.parseInt(fileSize));
			frame.pack();
			frame.setVisible(true);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			//JOptionPane.showMessageDialog(null, "Reading " +fileSize+" bytes.");
			StopWatch s = new StopWatch();
			s.start();
			int bytesRead = is.read(mybytearray, 0, mybytearray.length);
			int current = bytesRead;
			//JOptionPane.showMessageDialog(null, "Read "+current+"bytes on first read.");
			//Reconstruct the file
			//TODO Write in chunks so we can write large files withouth running out of memory
			do { //System.out.println("loop");
				frame.iterate(current,(int)s.getElapsedTime());
				bytesRead = is.read(mybytearray, current, (mybytearray.length-current));

				//System.out.println(bytesRead);
				if(bytesRead >= 0) current += bytesRead;

				/*if ((Athena.clientResource.tabPanels.containsKey(fromUser))) {
					try{
						MapTextArea print = (MapTextArea) Athena.clientResource.tabPanels.get(fromUser);
						print.writeToTextArea("Transfer is "+Double.parseDouble(String.valueOf(current))/Double.parseDouble(fileSize)*100.0+"% done.\n", print.getSetHeaderFont(Color.gray));
					}catch(Exception e){System.out.println("OOOOOOOOOPs");}
				}*/
				//JOptionPane.showMessageDialog(null,Double.parseDouble(String.valueOf(current))/Double.parseDouble(fileSize)*100.0);
				System.out.println("Transfer is "+Double.parseDouble(String.valueOf(current))/Double.parseDouble(fileSize)*100.0+"% done.\n");

			} while(bytesRead != 0);
			//JOptionPane.showMessageDialog(null, "Done reading bytes, read "+current+" bytes.");
			s.stop();
			if ((Athena.clientResource.tabPanels.containsKey(fromUser))) {
					try{
						MapTextArea print = (MapTextArea) Athena.clientResource.tabPanels.get(fromUser);
						print.writeToTextArea("Transfer completed in "+s.getElapsedTime()+" ms.\nDecrypting file, please wait...\n", print.getSetHeaderFont(Color.gray));
					}catch(Exception e){System.out.println("OOOOOOOOOPs");}
				}
			//System.out.println("Encrypted file: "+new String(mybytearray));
			//byte[] something = new BigInteger(mybytearray).toByteArray();
			//System.out.println("Something: "+String.valueOf(something));
			byte[] decryptedFile = Athena.decryptAES(toUser,mybytearray);//something);
			//System.out.println("Decrypted file: "+new String(decryptedFile));

	//		byte[] decryptedFile = decryptAES(fromUser, mybytearray);

			//byte[] decryptedFileByteArray = decryptedFile;
			
			bos.write(decryptedFile);
			bos.flush();
			bos.close();
			fos.close();
			is.close();
			fileSocket.close();
			if ((Athena.clientResource.tabPanels.containsKey(fromUser))) {
					try{
						MapTextArea print = (MapTextArea) Athena.clientResource.tabPanels.get(fromUser);
						print.writeToTextArea("File decrypted and stored in downloads directory.\n", print.getSetHeaderFont(Color.gray));
					}catch(Exception e){System.out.println("OOOOOOOOOPs");}
				}
			System.gc();

			System.out.println("elapsed time in milliseconds: " + s.getElapsedTime());
		}catch(Exception e){}
	}
}
