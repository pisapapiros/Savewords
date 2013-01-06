package sw.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;

import android.os.Environment;

public class Filemanager {
	public static boolean isExternalStorageReadOnly() {
		String extStorageState = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
			return true;
		}
		return false;
	}

	public static boolean isExternalStorageAvailable() {
		String extStorageState = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
			return true;
		}
		return false;
	}

	public void writeFile(String filename, String textfile) {
		try {
			if (isExternalStorageAvailable() && !isExternalStorageReadOnly()) {
				File file = new File(Environment.getExternalStorageDirectory(),
						filename);
				FileWriter TextOut = new FileWriter(file, true);
				TextOut.write(textfile);
				TextOut.close();
			}
		} catch (Exception e) {
			System.out.println("catch");
		}
	}

	public String readFile(String filename) {
		try {
			if (isExternalStorageAvailable()) {
				File file = new File(Environment.getExternalStorageDirectory(),
						filename);
				BufferedReader br = new BufferedReader(new InputStreamReader(
						new FileInputStream(file)));
				String t = br.readLine();
				br.close();
				return t;
			} else {
				return "";
			}
		} catch (Exception e) {
			return "";
		}
	}
}
