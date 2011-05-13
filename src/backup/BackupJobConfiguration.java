package backup;

import java.io.File;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.SubnodeConfiguration;

public abstract class BackupJobConfiguration {

	protected SubnodeConfiguration config;

	public BackupJobConfiguration(SubnodeConfiguration config)
			throws ConfigurationException {
		this.config = config;

	}

	/**
	 * This returns the name of the job
	 * 
	 * @return
	 */
	public String getName() {
		return getConfig().getString("job[@name]");
	}

	/**
	 * 
	 * @return String
	 */
	public String getAdapter() {
		return getConfig().getString("job[@adapter]");
	}

	/**
	 * 
	 * @return String
	 */
	public boolean getArchive() {
		boolean archive = false;
		if (getConfig().getString("job.backup-directory.archived[@type]") != null) {
			archive = true;
		}
		
		return archive;
	}

	/**
	 * 
	 * @return String
	 */
	public String getArchiveType() {
		return getConfig().getString("backup-directory.archived[@type]");
	}

	/**
	 * 
	 * @return String
	 */
	public String getEncrypt() {
		return getConfig().getString("backup-directory.encrypted[@type]");
	}

	/**
	 * 
	 * @return String
	 */
	public File getBackupDirectory() {
		return new File(getConfig().getString("job.backup-directory[@path]"));
	}

	/**
	 * @return the config
	 */
	public SubnodeConfiguration getConfig() {
		return config;
	}

	/**
	 * @param config
	 *            the config to set
	 */
	public void setConfig(SubnodeConfiguration config) {
		this.config = config;
	}
}
