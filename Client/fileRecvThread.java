
import java.awt.Color;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.Socket;

/**
 *
 * @author OlympuSoft
 */
public class fileRecvThread extends Thread {
	String fromUser; //User that is sending the file (used to look up session key)
	String filePath; //The name of the file
	String fileSize; //The size of the file
	String toUser;   //Is this the same as fromUser?
	String username; //The username that invoked this thread
	String socketIP; //The IP of the "server"
	
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
			//Get rid of the leading /
			String socketIPReplace = socketIP.replace("/", "");
			Socket fileSocket = null;

			//Keep trying to connect to the sender on port 7779
			//TODO Made rudimentary timeout after 15 seconds. Is this an okay time?
			int i=0;
			while(fileSocket == null && i < 15){
				fileSocket = new Socket(socketIPReplace, 7779);
				i++;
				Thread.sleep(1000);
			}
			if(fileSocket == null) return;

			//Get the inputstream from the socket to get the file from
			InputStream is = fileSocket.getInputStream();

			//Make a byte[] to hold the file coming in from the inputstream
			byte[] mybytearray = new byte[Integer.parseInt(fileSize)];

			//Get the filename
			String filePathReplace = filePath.replace("\\", ",");
			String[] filePathArray = filePathReplace.split(",");
			int arrSize = filePathArray.length;

			//Open the file for writing
			//TODO Check to see if the downloads folder exists!
			FileOutputStream fos = new FileOutputStream("users/" + username + "/downloads/" + filePathArray[arrSize-1]);
			BufferedOutputStream bos = new BufferedOutputStream(fos);

			//Start the progress bar with 100% being the total number of bytes were are reading in
			Progress frame = new Progress(filePathArray[arrSize-1],Integer.parseInt(fileSize));
			frame.pack();
			frame.setVisible(true);

			//Start the stopwatch so we know how much time as elapsed
			StopWatch s = new StopWatch();
			s.start();

			//Take the first read
			int bytesRead = is.read(mybytearray, 0, mybytearray.length);
			int current = bytesRead;

			//Read in the remaining bytes
			//TODO Write in chunks so we can write large files without running out of memory
			do {
				//Update the progress bar
				frame.iterate(current,(int)s.getElapsedTime());

				//Read in more bytes
				bytesRead = is.read(mybytearray, current, (mybytearray.length-current));

				//If we read any bytes, update the total. Else we are done
				if(bytesRead >= 0) current += bytesRead;

				if(Athena.debug >=1) {
					Athena.writeLog("Transfer is "+Double.parseDouble(String.valueOf(current))/Double.parseDouble(fileSize)*100.0+"% done.\n");
				}

			} while(bytesRead != 0);

			//Transfer is done, stop the stopwatch
			s.stop();

			//Notify the user
			if ((Athena.clientResource.tabPanels.containsKey(fromUser))) {
					try{
						MapTextArea print = (MapTextArea) Athena.clientResource.tabPanels.get(fromUser);
						print.writeToTextArea("Transfer completed in "+s.getElapsedTime()+" ms.\nDecrypting file, please wait...\n", print.getSetHeaderFont(Color.gray));
					}catch(Exception e){Athena.writeLog("OOOOOOOOOPs");}
			}

			//Decrypt the file
			byte[] decryptedFile = Athena.decryptAES(toUser,mybytearray);

			//Write the byte[] to the open file
			bos.write(decryptedFile);
			bos.flush();

			//Button everything up
			bos.close();
			fos.close();
			is.close();
			fileSocket.close();

			//Notify the user that their file is done
			if ((Athena.clientResource.tabPanels.containsKey(fromUser))) {
					try{
						MapTextArea print = (MapTextArea) Athena.clientResource.tabPanels.get(fromUser);
						print.writeToTextArea("File decrypted and stored in downloads directory.\n", print.getSetHeaderFont(Color.gray));
					}catch(Exception e){Athena.writeLog("OOOOOOOOOPs");}
				}
			System.gc();

			if( Athena.debug >= 1) {
				Athena.writeLog("elapsed time in milliseconds: " + s.getElapsedTime());
			}
		}catch(Exception e){}
	}
}
