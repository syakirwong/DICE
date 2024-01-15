package com.alliance.dicerecommendation.utility;

import com.alliance.dicerecommendation.constant.SFTPConstant;
import com.alliance.dicerecommendation.constant.SFTPProfile;
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

	// public SFTPUtil(){
	// 	String environment = SFTPConstant.getInstance().getEnvironment();
	// 	if (environment != null && environment.equalsIgnoreCase("UAT")) {
	// 		init(SystemParam.getInstance().getMftSFTPProfile());
	// 	}
	// 	else if (environment != null && environment.equalsIgnoreCase("PROD"))
	// 		init(SystemParam.getInstance().getMftSFTPProfile());
	// 	else
	// 		init(SystemParam.getInstance().getSFTPProfile2());
	// }

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

	public Boolean downloadMFTFile(String fileName) throws SftpException {
		log.info("SFTP :: START Attempting to download [" + fileName + "] from directory [" + channelSftp.pwd() + "]");
		Boolean isDownloaded = false;
		try {
			if (Boolean.parseBoolean(SystemParam.getInstance().getMock())) {
				log.info("Mock is enabled. Skipping download.");
				return isDownloaded;
			}

			log.info("SFTP :: downloading [" + fileName + "] now");

			if (channelSftp != null && channelSftp.isConnected()) {
				channelSftp.get(fileName, "tmp"); // Download the file directly to the destinationPath
				log.info("SFTP :: success downloading file : {}", fileName);
				isDownloaded = true;
			} else {
				log.info("SFTP :: channelSftp is null / channelSftp is not Connected");
				isDownloaded = false;
			}
		} catch (Exception e) {
			log.error("SFTP :: Error while transferring file through SFTP : {}", e);
			isDownloaded = false;
			return isDownloaded;
		}finally {
			// closing resources
			terminate();
		}
		return isDownloaded;
	}
}
