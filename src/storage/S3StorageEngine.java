package storage;

import java.io.IOException;

import backup.Backup;
import backup.BackupJob;

import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

public class S3StorageEngine {

	protected AmazonS3 s3;
	protected String bucketName;

	/**
	 * 
	 * @throws IOException
	 */
	public S3StorageEngine() throws IOException {
		s3 = new AmazonS3Client(new PropertiesCredentials(this.getClass()
				.getResourceAsStream("AwsCredentials.properties")));
	}

	/**
	 * 
	 * @param file
	 */
	public void save(BackupJob backupJob) {
		bucketName = prepareBucket(backupJob.getBackupName());
		s3.putObject(bucketName, backupJob.getName(), backupJob.getTmpDir());
	}

	/**
	 * 
	 * @param bucketName
	 * @return
	 */
	protected String prepareBucket(String backupName) {
		bucketName = "tbg." + getBucketName(backupName);
		Backup.getLog(this).info("Creating new bucket " + bucketName);
		if (!s3.doesBucketExist(bucketName)) {
			Backup.getLog(this).info("Creating new bucket " + bucketName);
			s3.createBucket(bucketName);
		}

		return bucketName;
	}

	/**
	 * 
	 * @param backupName
	 * @return
	 */
	protected String getBucketName(String backupName) {

		return backupName.replaceAll("[^a-zA-Z0-9.-]", "-").toLowerCase();
	}

}
