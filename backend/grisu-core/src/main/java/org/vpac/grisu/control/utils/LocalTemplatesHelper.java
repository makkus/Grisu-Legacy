package org.vpac.grisu.control.utils;

import grith.jgrith.Init;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.log4j.Logger;

public class LocalTemplatesHelper {

	static final Logger myLogger = Logger.getLogger(LocalTemplatesHelper.class
			.getName());

	public static final File GRISU_DIRECTORY = new File(System
			.getProperty("user.home"), ".grisu");
	public static final File TEMPLATES_AVAILABLE_DIR = new File(
			GRISU_DIRECTORY, "templates_available");
	public static final File GLITE_DIRECTORY = new File(System
			.getProperty("user.home"), ".glite");

	public static final File GLOBUS_CONFIG_DIR = new File(GRISU_DIRECTORY,
	"globus");

	public static void copyFile(File in, File out) throws IOException {
		FileChannel inChannel = new FileInputStream(in).getChannel();
		FileChannel outChannel = new FileOutputStream(out).getChannel();
		try {
			inChannel.transferTo(0, inChannel.size(), outChannel);
		} catch (IOException e) {
			throw e;
		} finally {
			if (inChannel != null) {
				inChannel.close();
			}
			if (outChannel != null) {
				outChannel.close();
			}
		}
	}

	/**
	 * Extracts the files in the vomses.zip file in the directory
	 * $HOME/.glite/vomses These files are pointing Grix to the voms/vomrs
	 * server(s) the APACGrid is using.
	 * 
	 * @throws Exception
	 */
	public static void copyTemplatesAndMaybeGlobusFolder() throws Exception {

		if (!TEMPLATES_AVAILABLE_DIR.exists() || !GRISU_DIRECTORY.exists()) {
			createGrisuDirectories();
		}

		if (TEMPLATES_AVAILABLE_DIR.list().length == 0) {

			myLogger.debug("Filling templates_available folder with a set of base templates...");
			int BUFFER_SIZE = 8192;
			int count;
			byte data[] = new byte[BUFFER_SIZE];

			InputStream in = Init.class
			.getResourceAsStream("/templates_available.zip");
			ZipInputStream zipStream = new ZipInputStream(in);

			BufferedOutputStream dest = null;

			try {

				ZipEntry entry = null;

				while ((entry = zipStream.getNextEntry()) != null) {

					if (!entry.isDirectory()) {

						myLogger.debug("Template name: " + entry.getName());
						File vomses_file = new File(TEMPLATES_AVAILABLE_DIR,
								entry.getName());

						// Write the file to the file system and overwrite
						// possible
						// old files with the same name
						FileOutputStream fos = new FileOutputStream(vomses_file);
						dest = new BufferedOutputStream(fos, BUFFER_SIZE);
						while ((count = zipStream.read(data, 0, BUFFER_SIZE)) != -1) {
							dest.write(data, 0, count);
						}
						dest.flush();
						dest.close();
					}
				}
			} catch (Exception e) {
				myLogger.error(e);
			}
		} else {
			myLogger.debug("Templates folder already contains files. Not copying any into it...");
		}

		// copy globus floder if not already there
		try {
			if (!GLOBUS_CONFIG_DIR.exists()) {
				unzipFileToDir("/globus.zip", GRISU_DIRECTORY);
			}
		} catch (Exception e) {
			myLogger.error(e);
		}

	}

	/**
	 * Creates the grisu directory if it doesn't exist yet.
	 * 
	 * @throws Exception
	 *             if something goes wrong
	 */
	public static void createGrisuDirectories() throws Exception {

		if (!GRISU_DIRECTORY.exists()) {
			if (!GRISU_DIRECTORY.mkdirs()) {
				myLogger.error("Could not create vomses directory.");
				throw new Exception(
						"Could not create grisu directory. Please set permissions for "
						+ GRISU_DIRECTORY.toString()
						+ " to be created.");
			}
		}

		if (!TEMPLATES_AVAILABLE_DIR.exists()) {
			if (!TEMPLATES_AVAILABLE_DIR.mkdirs()) {
				myLogger.error("Could not create available_vomses directory.");
				throw new Exception(
						"Could not create templates_available directory. Please set permissions for "
						+ TEMPLATES_AVAILABLE_DIR.toString()
						+ " to be created.");
			}
		}
	}

	private static void unzipFileToDir(String zipFileResourcePath,
			File targetDir) {

		int BUFFER_SIZE = 8192;
		int count;
		byte data[] = new byte[BUFFER_SIZE];

		InputStream in = Init.class.getResourceAsStream(zipFileResourcePath);
		ZipInputStream zipstream = new ZipInputStream(in);

		BufferedOutputStream dest = null;

		try {

			ZipEntry entry = null;

			while ((entry = zipstream.getNextEntry()) != null) {
				myLogger.debug("Entry: " + entry.getName());
				String filePath = GRISU_DIRECTORY.getAbsolutePath()
				+ File.separator + entry.getName();

				if (!entry.isDirectory()) {

					// File vomses_file = new File(TEMPLATES_AVAILABLE_DIR,
					// entry.getName());
					File vomses_file = new File(filePath);

					// Write the file to the file system and overwrite possible
					// old files with the same name
					FileOutputStream fos = new FileOutputStream(vomses_file);
					dest = new BufferedOutputStream(fos, BUFFER_SIZE);
					while ((count = zipstream.read(data, 0, BUFFER_SIZE)) != -1) {
						dest.write(data, 0, count);
					}
					dest.flush();
					dest.close();
				} else {
					// new File(GRISU_DIRECTORY,entry.getName()).mkdirs();
					new File(filePath).mkdirs();
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			myLogger.error(e);
		}

	}

}
