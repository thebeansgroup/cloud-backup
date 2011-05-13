package backup;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.configuration.ConfigurationException;

public class ScribeBackupJob extends BackupJob {

	public static final String DATE_FORMAT = "yyyy-MM-dd";

	public ScribeBackupJob(ScribeBackupJobConfiguration config)
			throws ConfigurationException, IOException {
		super(config);
	}

	@Override
	public void executeBackup() {
		File[] files = getConfig().getBackupDirectory().listFiles();
		for (File file : files) {
			backupFile(file);
		}
	}

	/**
	 * 
	 * @param file
	 */
	public void backupFile(File file) {
		try {
			Backup.getLog(this).info("Backing up: " + file.getName());
			if (file.isDirectory()) {
				new File(getTmpDir() + File.separator + file.getName()).mkdir();
				for (File child : file.listFiles()) {
					backupFile(child);
				}
			} else if (shouldBackup(file)) {
				FileReader in = new FileReader(file.getAbsolutePath());
				File backupFile = new File(getTmpDir() + File.separator
						+ file.getParentFile().getName() + File.separator
						+ file.getName());
				backupFile.createNewFile();
				FileWriter out = new FileWriter(backupFile);
				int c;
				while ((c = in.read()) != -1) {
					out.write(c);
				}
				in.close();
				out.close();
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 */
	public boolean shouldBackup(File file) throws ParseException, IOException {
		boolean shouldBackup = true;
		Calendar today = Calendar.getInstance();
		Calendar lastModified = Calendar.getInstance();
		lastModified.setTime(new Date(file.lastModified()));

		if (today.after(lastModified)) {
			InputStream fstream = readStateFile();
			if (fstream != null) {
				DataInputStream in = new DataInputStream(fstream);
				BufferedReader br = new BufferedReader(
						new InputStreamReader(in));
				Calendar lastBackup = Calendar.getInstance();
				SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
				String line = br.readLine();
				if (line != null && !line.isEmpty()) {
					lastBackup.setTime((Date) dateFormat.parse(line));
					if (lastBackup.after(lastModified)) {
						shouldBackup = false;
					}
				}
			} else {
				shouldBackup = false;
			}
		} else {
			shouldBackup = false;
		}

		return shouldBackup;
	}

	@Override
	public void writeToStateFile() throws IOException {
		super.writeToStateFile();
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
			RandomAccessFile state = new RandomAccessFile(getStateFile(), "rw");
			state.writeBytes(dateFormat.format(new Date()));

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * @return
	 */
	@Override
	public ScribeBackupJobConfiguration getConfig() {
		return (ScribeBackupJobConfiguration) super.getConfig();
	}
}
