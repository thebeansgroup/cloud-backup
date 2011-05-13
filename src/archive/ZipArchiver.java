package archive;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import backup.Backup;
import backup.BackupJob;

public class ZipArchiver extends Archiver {

	static final int BUFFER = 2048;

	/**
	 * 
	 */
	@Override
	public File compress(BackupJob job) throws IOException,
			FileNotFoundException {

		Backup.getLog(this).info("Starting to compress...");

		try {
			File zip = new File(job.getTmpDir().getParentFile()
					.getAbsolutePath()
					+ File.separator + job.getName() + ".zip");
			ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zip));
			compressDirectory(job.getTmpDir(), out);
			job.deleteTmpDirectory();
			job.setTmpDir(new File(zip.getAbsolutePath()));
			out.close();
		} catch (IOException e) {
			throw e;
		} catch (RuntimeException e) {
			Backup.getLog(this).fatal(this, e);// ("File/Folder could not be zipped");
		}

		Backup.getLog(this).info("Finished compressing...");

		return null;
	}

	/**
	 * 
	 * @param dir
	 * @param out
	 * @throws IOException
	 */
	protected void compressDirectory(File dir, ZipOutputStream out)
			throws IOException {
		byte data[] = new byte[BUFFER];

		File[] files;

		if (dir.isFile()) {
			files = new File[0];
			files[0] = dir;
		} else {
			// get a list of files from current directory
			files = dir.listFiles();
		}

		if (files != null && files.length > 0) {
			for (File file : files) {

				if (file.isDirectory()) {
					compressDirectory(file, out);
					continue;
				}

				FileInputStream in = new FileInputStream(file.getAbsolutePath());
				out.putNextEntry(new ZipEntry(file.getAbsolutePath()));
				int len;
				while ((len = in.read(data)) > 0) {
					out.write(data, 0, len);
				}
				out.closeEntry();
				in.close();
			}
		}
	}

}
