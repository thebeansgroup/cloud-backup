package backup;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import org.apache.commons.configuration.ConfigurationException;

import archive.ZipArchiver;

import storage.S3StorageEngine;

public abstract class BackupJob {

	protected File stateFile;
	protected BackupJobConfiguration config;
	protected File tmpDir;

	/**
	 * 
	 * @param config
	 * @throws ConfigurationException
	 * @throws IOException
	 */
	public BackupJob(BackupJobConfiguration config)
			throws ConfigurationException, IOException {
		validateConfiguration(config);
		this.config = config;
		setTmpDir(new File(File.separator + "tmp" + File.separator
				+ UUID.randomUUID()));
		createStateFile();
	}

	public void execute() throws IOException {
		executeBackup();
		if (getConfig().getArchive()) {
			executeArchive();
		}
		executeSave();
		executeShutdown();
		writeToStateFile();
	}

	public abstract void executeBackup() throws IOException;

	/**
	 * 
	 */
	public void executeArchive() {
		ZipArchiver archiver = new ZipArchiver();
		try {
			archiver.compress(this);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param config
	 * @throws ConfigurationException
	 */
	protected void validateConfiguration(BackupJobConfiguration config)
			throws ConfigurationException {
		if (config.getBackupDirectory().isFile()) {
			throw new ConfigurationException(
					"Please make sure the backup dir path points to a folder");
		}
	}

	/**
	 * 
	 */
	public void executeShutdown() {
		deleteTmpDirectory();
	}

	/**
	 * @return the config
	 */
	public BackupJobConfiguration getConfig() {
		return config;
	}

	/**
	 * @param config
	 *            the config to set
	 */
	public void setConfig(BackupJobConfiguration config) {
		this.config = config;
	}

	/**
	 * @todo refactor storage engine to enable multiple engines
	 * 
	 * @param file
	 * @throws IOException
	 */
	protected void executeSave() throws IOException {
		S3StorageEngine storageEngine = new S3StorageEngine();
		storageEngine.save(this);
	}

	/**
	 * 
	 * 
	 * @return
	 */
	public String getBackupName() {
		return getConfig().getName();
	}

	/**
	 * top level delete tmp dir function, just a wrapper for overloaded function
	 */
	public void deleteTmpDirectory() {
		deleteTmpDirectory(tmpDir);
	}

	/**
	 * Overloaded delete Tmp dir to enable recursive invocation
	 * 
	 * @param file
	 */
	protected void deleteTmpDirectory(File file) {
		if (file.exists() && file.canWrite()) {
			if (file.isDirectory()) {
				File[] files = file.listFiles();
				for (File child : files) {
					if (child.isDirectory()) {
						deleteTmpDirectory(child);
					} else {
						child.delete();
					}
				}
			}
			file.delete();
		}
	}

	/**
	 * @throws IOException
	 * 
	 */
	public void writeToStateFile() throws IOException {
	}

	/**
	 * 
	 * @return
	 * @throws FileNotFoundException
	 */
	public InputStream readStateFile() {
		File stateFile = getStateFile();
		InputStream stateStream = null;

		if (stateFile != null) {
			try {
				stateStream = new FileInputStream(stateFile);
			} catch (FileNotFoundException e) {

			}
		}

		return stateStream;
	}

	/**
	 * @return the tmpDir
	 */
	public File getTmpDir() {
		return tmpDir;
	}

	/**
	 * @param tmpDir
	 *            the tmpDir to set
	 * @throws IOException
	 */
	public void setTmpDir(File tmpDir) throws IOException {
		Backup.getLog(this).info("Setting tmp backup dir to: " + tmpDir);
		tmpDir.mkdir();
		this.tmpDir = tmpDir;
	}

	/**
	 * 
	 * @return String
	 */
	public String getName() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

		return formatter.format(new Date());
	}

	/**
	 * 
	 * @return boolean
	 * @throws IOException
	 */
	protected boolean createStateFile() throws IOException {
		setStateFile(new File(getConfig().getName().replaceAll("[^a-zA-Z0-9-]",
				"-").toLowerCase()));
		Backup.getLog(this).info(
				"Creating new state file at position "
						+ getStateFile().getAbsolutePath());

		return getStateFile().createNewFile();
	}

	/**
	 * @return the stateFile
	 */
	public File getStateFile() {
		return stateFile;
	}

	/**
	 * @param stateFile
	 *            the stateFile to set
	 */
	public void setStateFile(File stateFile) {
		this.stateFile = stateFile;
	}
}
