package com.alliance.diceanalytics.utility;

import com.alliance.diceanalytics.constant.SFTPConstant;
import com.alliance.diceanalytics.constant.SFTPProfile;
import com.alliance.diceanalytics.model.FileForUpload;
import com.jcraft.jsch.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

@Slf4j
public class SFTPUtil {
	protected ChannelSftp channelSftp = null;
	protected Session session = null;
	protected Channel channel = null;

	public SFTPUtil(SFTPProfile profile) {
		init(profile);
	}

	public SFTPUtil(){
		String environment = SFTPConstant.getInstance().getEnvironment();
		if (environment != null && environment.equalsIgnoreCase("UAT")) {
			init(SystemParam.getInstance().getMftSFTPProfile());
		}
		else if (environment != null && environment.equalsIgnoreCase("PROD"))
			init(SystemParam.getInstance().getMftSFTPProfile());
		else
			init(SystemParam.getInstance().getSFTPProfile2());
	}

	public void init(SFTPProfile profile) {
		log.info("SFTP :: Creating SFTP Connection");
		String environment = SFTPConstant.getInstance().getEnvironment();

		try {
			JSch jsch = new JSch();
			byte[] privateKey;
			byte[] publicKey;

			 if (environment != null && environment.equalsIgnoreCase("UAT")) {
				privateKey = IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("mft_id_rsa"));
				publicKey = IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("mft_id_rsa.pub"));
			} else {
				// PROD
				privateKey = IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("ploan-cv_id_rsa"));
				publicKey = IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("ploan-cv_id_rsa.pub"));
			}

			log.debug("SFTP :: Adding identity");
			try {
				jsch.addIdentity("dbos", privateKey, publicKey, null);
				// Don't think name matters... will find out
				log.debug("SFTP :: Identity [dbos] added");
			} catch (Exception e) {
				log.debug("SFTP :: Exception when adding identity");
				if (log.isDebugEnabled())
					e.printStackTrace();
			}

			session = jsch.getSession(
					profile.getUser(),
					profile.getHost(),
					Integer.parseInt(profile.getPort())
			);
			session.setPassword(profile.getPassword());
			Properties config = new Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);

			// Initiate connection
			session.connect();
			log.debug("SFTP :: Connected to {}.", profile.getHost());
			channel = session.openChannel("sftp");
			channel.connect();
			log.debug("SFTP :: Channel opened and connected.");
			channelSftp = (ChannelSftp) channel;
			log.debug("SFTP :: PWD -> {}", channelSftp.pwd());
			Vector fileList = channelSftp.ls(profile.getPath());
			for (Object o : fileList) {
				log.debug("Vector : {}", o.toString());
			}
			// Finished initializing connection
			log.info("SFTP :: Moving directories");
			if (StringUtils.isNotBlank(profile.getPath())) {
				String[] folders = profile.getPath().split("/");
				for (String folder : folders) {
					if (folder.length() > 0) {
						try {
							channelSftp.cd(folder);
							log.info(channelSftp.pwd());
						} catch (SftpException e) {
							channelSftp.mkdir(folder);
							channelSftp.cd(folder);
							log.error(channelSftp.pwd());
						}
					}
				}
			}
			log.info("SFTP :: Arrived at destination directory -> {}", channelSftp.pwd());

		} catch (JSchException | SftpException | IOException e) {
			log.error("SFTP :: Exception during session due to {} - {}.", e.getMessage(), e.getCause());
			if (log.isDebugEnabled())
				e.printStackTrace();

		}
	}

	public void terminate() {
		if (channelSftp != null) {
			channelSftp.exit();
			log.debug("SFTP :: SFTP Channel exited.");
			channelSftp.disconnect();
			log.debug("SFTP :: SFTP Channel disconnected.");
		}
		if (channel != null) {
			channel.disconnect();
			log.debug("SFTP :: Channel disconnected.");
		}
		if (session != null) {
			session.disconnect();
			log.debug("SFTP :: Host Session disconnected.");
		}
	}

	public Boolean transferFile(InputStream f, String fileName) {
		try {
			if (Boolean.parseBoolean(SFTPConstant.getInstance().getMock())) {
				return true;
			}

			log.debug("SFTP :: Attempting to transfer [" + fileName + "] now");

			if (channelSftp != null && channelSftp.isConnected()) {
				log.debug("SFTP :: Starting file transfer process");
				channelSftp.put(f, fileName);
				log.debug("SFTP :: File successfully transferred");
				f.close();
				terminate();
				return true;
			} else {
				log.debug("SFTP :: channelSftp is null / channelSftp is not Connected");
			}
		} catch (Exception e) {
			log.error("SFTP :: Error while transferring file through SFTP");
			if (log.isDebugEnabled())
				e.printStackTrace();
		}
		return false;
	}

	public Boolean transferFiles(List<FileForUpload> files) {
		try {
			if (Boolean.parseBoolean(SystemParam.getInstance().getMock())) {
				return true;
			}
			for (FileForUpload file : files) {
				log.debug("SFTP :: Attempting to transfer [" + file.getFileName() + "] now");

				if (channelSftp != null && channelSftp.isConnected()) {
					log.debug("SFTP :: Starting file transfer process");
					channelSftp.put(new ByteArrayInputStream(file.getFileData()), file.getFileName());
					log.debug("SFTP :: File successfully transferred");
				} else {
					log.debug("SFTP :: channelSftp is null / channelSftp is not Connected");
				}
			}
			terminate();
			return true;
		} catch (Exception e) {
			log.error("SFTP :: Error while transferring file through SFTP");
			if (log.isDebugEnabled())
				e.printStackTrace();
		}
		return false;
	}

	public byte[] downloadFile(String fileName) {
		byte[] fileToDownload = null;
		try {
			if (Boolean.parseBoolean(SystemParam.getInstance().getMock())) {
				return null;
			}

			log.debug("SFTP :: Attempting to download [" + fileName + "] now");

			if (channelSftp != null && channelSftp.isConnected()) {
				log.debug("Attempting to obtain file");

				log.debug("==================================");
				channelSftp.ls(channelSftp.pwd()).forEach(vec -> log.debug("SFTP :: LS (ForEach) -> {}", vec.toString()));
				log.debug("==================================");

				fileToDownload = IOUtils.toByteArray(channelSftp.get(fileName));
			} else {
				log.debug("SFTP :: channelSftp is null / channelSftp is not Connected");
			}
		} catch (Exception e) {
			log.error("SFTP :: Error while transferring file through SFTP");
			if (log.isDebugEnabled())
				e.printStackTrace();
		}
		terminate();
		return fileToDownload;
	}

	public File saveAsFile(String fileName, byte[] bytes) throws IOException {
		File file = new File(fileName);
		if(file.getParentFile() != null) {
			file.getParentFile().mkdirs();
		}
		file.createNewFile();

		try (FileOutputStream outputStream = new FileOutputStream(file, false)) {
			outputStream.write(bytes);
		}

		return file;
	}

	// public byte[] getMFTFile(String fileName) throws SftpException {
	// 	log.info("SFTP :: START Attempting to download [" + fileName + "] from directory [" + channelSftp.pwd() + "]");
    
	// 	byte[] fileToDownload = null;
	// 	try {
	// 		if (Boolean.parseBoolean(SystemParam.getInstance().getMock())) {
	// 			return null;
	// 		}

	// 		log.info("SFTP :: downloading [" + fileName + "] now");

	// 		if (channelSftp != null && channelSftp.isConnected()) {

	// 			log.info("================List Start===============");
	// 			channelSftp.ls(channelSftp.pwd()).forEach(vec -> log.info("SFTP :: LS (ForEach) -> {}", vec.toString()));
	// 			log.info("=================List End================");

	// 			fileToDownload = IOUtils.toByteArray(channelSftp.get(fileName));
	// 			if (fileToDownload !=null) {
	// 				log.info("SFTP :: success get file : {}", fileName);
	// 			}
	// 		} else {
	// 			log.info("SFTP :: channelSftp is null / channelSftp is not Connected");
	// 		}
	// 	} catch (Exception e) {
	// 		log.error("SFTP :: Error while transferring file through SFTP : {}", e);
	// 		if (log.isDebugEnabled())
	// 			e.printStackTrace();
	// 	}
	// 	terminate();
	// 	return fileToDownload;
	// }

	// // public File saveAsFile(String fileName, byte[] bytes) throws IOException {
	// // 	File file = new File(fileName);
	// // 	if(file.getParentFile() != null) {
	// // 		file.getParentFile().mkdirs();
	// // 	}
	// // 	file.createNewFile();

	// // 	try (FileOutputStream outputStream = new FileOutputStream(file, false)) {
	// // 		outputStream.write(bytes);
	// // 	}

	// // 	return file;
	// // }

	// // public File downloadAndSaveFile(String fileName, String localFilePath) {
	// // 	byte[] fileBytes = null;
	// // 	File savedFile = null;
		
	// // 	try {	
	// // 		// Download the file
	// // 		fileBytes = getMFTFile(fileName);
			
	// // 		if (fileBytes != null) {
	// // 			// Specify the local path where you want to save the file
	// // 			String filePath = localFilePath + File.separator + fileName;
				
	// // 			// Save the file locally
	// // 			savedFile = saveAsFile(filePath, fileBytes);
	// // 		}
	// // 	} catch (Exception e) {
	// // 		// Handle exceptions accordingly
	// // 		e.printStackTrace();
	// // 	}
		
	// // 	return savedFile;
	// // }

	// public List<String> listFiles(String directoryPath) {
	// 	List<String> fileList = new ArrayList<>();
	// 	try {
	// 		if (channelSftp != null && channelSftp.isConnected()) {
	// 			log.info("Attempting to list files in directory: {}", directoryPath);
	// 			channelSftp.cd(directoryPath); // Change directory path

	// 			Vector<ChannelSftp.LsEntry> files = channelSftp.ls(".");
	// 			for (ChannelSftp.LsEntry entry : files) {
	// 				if (!entry.getFilename().equals(".") && !entry.getFilename().equals("..")) {
	// 					fileList.add(entry.getFilename());
	// 				}
	// 			}

	// 			log.info("Files in directory {}: {}", directoryPath, fileList);
	// 		} else {
	// 			log.info("SFTP :: channelSftp is null / channelSftp is not Connected");
	// 		}
	// 	} catch (SftpException e) {
	// 		log.error("SFTP :: Error while listing files in directory {} : {}", directoryPath, e);
	// 		if (log.isDebugEnabled()) {
	// 			e.printStackTrace();
	// 		}
	// 	}
	// 	return fileList;
	// }

	// public void downloadMFTFile(String fileName) throws SftpException {
	// 		log.info("SFTP :: START Attempting to download [" + fileName + "] from directory [" + channelSftp.pwd() + "]");
		
	// 		try {
	// 			if (Boolean.parseBoolean(SystemParam.getInstance().getMock())) {
	// 				log.info("Mock is enabled. Skipping download.");
	// 				return;
	// 			}

	// 			log.info("SFTP :: downloading [" + fileName + "] now");

	// 			if (channelSftp != null && channelSftp.isConnected()) {
	// 				channelSftp.get(fileName, "tmp"); // Download the file directly to the destinationPath
	// 				log.info("SFTP :: success downloading file : {}", fileName);
	// 			} else {
	// 				log.info("SFTP :: channelSftp is null / channelSftp is not Connected");
	// 			}
	// 		} catch (Exception e) {
	// 			log.error("SFTP :: Error while transferring file through SFTP : {}", e);
	// 			if (log.isDebugEnabled())
	// 				e.printStackTrace();
	// 		}finally {
	// 			terminate();
	// 		}
	// 	}
}
