package archive;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import backup.BackupJob;

public abstract class Archiver {

	public abstract File compress(BackupJob job) throws FileNotFoundException, IOException;
	
}
