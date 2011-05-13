package backup;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tmatesoft.svn.core.SVNException;

public class Backup {

	/**
	 * 
	 * @param args
	 * @throws Exception 
	 * @throws ConfigurationException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 */
	public static void main(String[] args) throws Exception {
		try {
			getLog(Backup.class.getName()).info("Starting backup");
			BackupJobFactory factory = new BackupJobFactory(
					new File(
							"/var/www/html/development/projects/s3backup/trunk/s3backup/src/backup.xml"));
			LinkedList<BackupJob> jobs = factory.getJobs();
			Backup.run(jobs);
			getLog(Backup.class.getName()).info("Finshed backup");
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 
	 * @param jobs
	 * @throws SVNException
	 * @throws IOException 
	 */
	protected static void run(LinkedList<BackupJob> jobs) throws IOException {
		Iterator<BackupJob> it = jobs.iterator();

		while (it.hasNext()) {
			BackupJob job = it.next();
			job.execute();
		}
	}

	/**
	 * 
	 * @param object
	 * @return
	 */
	public static Log getLog(Object object) {
		return LogFactory.getLog(object.getClass());
	}
	
	/**
	 * 
	 * @param object
	 * @return
	 */
	public static Log getLog(String object) {
		return LogFactory.getLog(object);
	}

}
