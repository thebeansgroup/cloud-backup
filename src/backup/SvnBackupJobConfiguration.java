package backup;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.lang.ArrayUtils;

public class SvnBackupJobConfiguration extends BackupJobConfiguration {

	/**
	 * 
	 * @param config
	 * @throws ConfigurationException
	 */
	public SvnBackupJobConfiguration(SubnodeConfiguration config)
			throws ConfigurationException {
		super(config);
	}

	/**
	 * 
	 * @return
	 */
	public File getSvnRepositoriesDirectory() {
		return new File(getConfig().getString("job.svn.repositories[@path]"));
	}

	/**
	 * 
	 * @return
	 */
	public BackupType getBackupType() {
		BackupType type = BackupType.INCREMENTAL;
		List days = getConfig().getList("job.schedule.full");
		SimpleDateFormat formatter = new SimpleDateFormat("E");

		if (days.contains(formatter.format(new Date()))) {
			type = BackupType.FULL;
		}

		return type;
	}

	/**
	 * 
	 * @author vincent
	 *
	 */
	public enum BackupType {
		FULL, INCREMENTAL
	}
}
