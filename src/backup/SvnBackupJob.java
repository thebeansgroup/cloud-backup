package backup;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.wc.ISVNOptions;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNWCClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;
import org.tmatesoft.svn.core.wc.admin.SVNAdminClient;

public class SvnBackupJob extends BackupJob {

	protected ISVNAuthenticationManager authManager;
	protected ISVNOptions options;
	protected SVNAdminClient adminClient;
	protected SVNWCClient wcClient;

	/**
	 * 
	 * @param config
	 * @throws ConfigurationException
	 * @throws IOException 
	 */
	public SvnBackupJob(SvnBackupJobConfiguration config)
			throws ConfigurationException, IOException {
		super(config);
		// setup some generic svn tools
		authManager = SVNWCUtil.createDefaultAuthenticationManager("", "");
		options = SVNWCUtil.createDefaultOptions(true);
		adminClient = new SVNAdminClient(authManager, options);
		wcClient = new SVNWCClient(authManager, options);
	}

	/**
	 * @throws IOException 
	 * @throws IOException
	 * @throws
	 * 
	 */
	@Override
	public void executeBackup() throws IOException {
		Backup.getLog(this).info("Starting " + getConfig().getName());

		List<File> projects = getProjects(getConfig()
				.getSvnRepositoriesDirectory());

		for (File project : projects) {
			if (validateSvnDirectory(project)) {
				try {
					switch (getConfig().getBackupType()) {
					case INCREMENTAL:
						Backup.getLog(this).info("Running incremental backup");
						runIncrementalBackup(project);
						break;
					default:
					case FULL:
						Backup.getLog(this).info("Running full backup");
						runFullBackup(project);
						break;
					}
				} catch (SVNException e) {
					Backup.getLog(this).fatal(this, e);
					// @todo insert email
				} catch (FileNotFoundException e) {
					Backup.getLog(this).fatal(this, e);
					// @todo insert email
				}
			}
		}
		Backup.getLog(this).info("Finished " + getConfig().getName());
	}

	/**
	 * 
	 * @param project
	 * @throws SVNException
	 */
	protected File runFullBackup(File project) throws SVNException {
		try {
			Backup.getLog(this).info(
					"Trying to backup folder at location "
							+ project.getAbsolutePath());
			adminClient.doHotCopy(project, getTmpDir());
		} catch (SVNException e) {
			Backup.getLog(this).fatal(this, e);
			throw e;
		}

		return getConfig().getBackupDirectory();
	}

	/**
	 * 
	 * @param project
	 * @return
	 * @throws SVNException
	 * @throws IOException 
	 */
	protected File runIncrementalBackup(File project) throws SVNException,
			IOException {
		try {
			setTmpDir(new File(getConfig().getBackupDirectory() + File.separator + project.getName()));
			Backup.getLog(this).info(
					"Trying to backup folder at location "
							+ project.getAbsolutePath());
			FileOutputStream out = new FileOutputStream(getTmpDir());
			adminClient.doDump(project, out, SVNRevision.create(new Long(1)),
					SVNRevision.HEAD, true, false);
		} catch (SVNException e) {
			Backup.getLog(this).fatal(this, e);
			throw e;
		}

		return null;
	}

	/**
	 * 
	 * @param dir
	 * @return
	 */
	protected ArrayList<File> getProjects(File dir) {
		File[] files = dir.listFiles();
		ArrayList<File> projects = new ArrayList<File>();

		for (File file : files) {
			projects.add(file);
		}

		return projects;
	}

	/**
	 * 
	 * @param dir
	 * @return
	 */
	protected boolean validateSvnDirectory(File dir) {
		boolean valid = true;
		try {
			adminClient.doVerify(dir);
		} catch (SVNException e) {
			valid = false;
		}

		return valid;

	}

	/**
	 * @return
	 */
	@Override
	public SvnBackupJobConfiguration getConfig() {
		return (SvnBackupJobConfiguration) super.getConfig();
	}

	/**
	 * @return
	 */
	@Override
	public String getName() {
		return getConfig().getBackupType().name() + "." + super.getName();

	}
}
