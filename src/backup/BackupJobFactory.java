package backup;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import org.apache.commons.lang.WordUtils;

public class BackupJobFactory {

	private XMLConfiguration config;
	private LinkedList<BackupJob> jobs;

	/**
	 * 
	 * @param configFile
	 * @throws ConfigurationException
	 */
	public BackupJobFactory(File configFile) throws ConfigurationException {
		setConfig(new XMLConfiguration(configFile));
		getConfig().load();
	}

	/**
	 * @param config
	 *            the config to set
	 */
	public void setConfig(XMLConfiguration config) {
		this.config = config;
	}

	/**
	 * @return the config
	 */
	public XMLConfiguration getConfig() {
		return config;
	}

	/**
	 * @param jobs
	 *            the jobs to set
	 */
	public void setJobs(LinkedList<BackupJob> jobs) {
		this.jobs = jobs;
	}

	/**
	 * @return the jobs
	 * @throws ClassNotFoundException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 * @throws ConfigurationException
	 */
	public LinkedList<BackupJob> getJobs() throws ClassNotFoundException,
			SecurityException, IllegalArgumentException,
			InstantiationException, IllegalAccessException,
			NoSuchMethodException, InvocationTargetException,
			ConfigurationException {
		initJobs();

		return jobs;
	}

	/**
	 * 
	 * @return
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 */
	private LinkedList<BackupJob> initJobs() throws ClassNotFoundException,
			InstantiationException, IllegalAccessException, SecurityException,
			NoSuchMethodException, IllegalArgumentException,
			InvocationTargetException, ConfigurationException {

		List<SubnodeConfiguration> jobList = getConfig().configurationsAt(
				"jobs(0)");
		jobs = new LinkedList<BackupJob>();

		Backup.getLog(this).info("Preparing " + jobList.size() + " backups");

		for (SubnodeConfiguration subConfig : jobList) {

			String className = "backup."
					+ WordUtils
							.capitalize(subConfig.getString("job[@adapter]"))
					+ "BackupJobConfiguration";

			Class cls = Class.forName(className);
			Class configTypes[] = new Class[1];
			configTypes[0] = subConfig.getClass();
			Constructor configCt = cls.getConstructor(configTypes);
			Object arglist[] = new Object[1];
			arglist[0] = subConfig;
			BackupJobConfiguration jobConfig = (BackupJobConfiguration) configCt
					.newInstance(arglist);

			className = "backup."
					+ WordUtils
							.capitalize(subConfig.getString("job[@adapter]"))
					+ "BackupJob";

			cls = Class.forName(className);
			Class jobTypes[] = new Class[1];
			jobTypes[0] = jobConfig.getClass();
			Constructor jobCt = cls.getConstructor(jobTypes);
			Object jobArglist[] = new Object[1];
			jobArglist[0] = jobConfig;

			jobs.add((BackupJob) jobCt.newInstance(jobArglist));
		}

		return jobs;
	}

}
