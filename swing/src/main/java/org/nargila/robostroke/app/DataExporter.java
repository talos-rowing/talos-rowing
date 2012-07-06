package org.nargila.robostroke.app;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.nargila.robostroke.common.Pair;
import org.nargila.robostroke.data.DataRecord;
import org.nargila.robostroke.data.DataRecord.Type;
import org.nargila.robostroke.data.FileDataInput;

class DataExporter {

	private final HashMap<DataRecord.Type, Pair<File, BufferedWriter>> outputMap = new HashMap<DataRecord.Type, Pair<File,BufferedWriter>>();

	private final Set<Type> exportSet;

	private final File dataFile;

	private boolean cancelled;

	DataExporter(File dataFile, Set<DataRecord.Type> exportSet) {
		this.dataFile = dataFile;
		this.exportSet = exportSet;
	}

	void cancel() {
		this.cancelled = true;
	}

	private String join(Object[] arr, String sep) {
		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < arr.length; ++i) {
			sb.append(arr[i]);

			if (i + 1 < arr.length) {
				sb.append(sep);
			}
		}

		return sb.toString();
	}

	void export() throws IOException {

		BufferedReader reader = null;

		try {

			final long dataLength = dataFile.length();
			
			reader = new BufferedReader(new FileReader(dataFile));

			String line;

			long currentPos = 0;
			while (!cancelled && (line = reader.readLine()) != null) {

				currentPos += line.length() + 1;
				
				onProgress(currentPos / (double)dataLength);
				
				Pair<Long,DataRecord> p = FileDataInput.parseRecord(line);

				if (p != null) {
					DataRecord record = p.second;
					Type type = record.type;
					if (type.isExportableEvent && exportSet.contains(type)) {
						Pair<File, BufferedWriter> outInfo = outputMap.get(type);

						if (outInfo == null) {
							File f = File.createTempFile(type.name(), ".csv");
							f.deleteOnExit();
							outInfo = Pair.create(f, new BufferedWriter(new FileWriter(f)));
							outputMap.put(type, outInfo);

							outInfo.second.write("timestamp," + join(type.getDataExporter().getColumnNames(), ",") + "\n");
						}

						outInfo.second.write(p.first + "," + join(type.getDataExporter().exportData(record.data), ",") + "\n");
					}
				}
			}
			
			
			if (!cancelled) {
				
				JFileChooser chooser = new JFileChooser();
				
				chooser.setFileFilter(new FileFilter() {
					
					@Override
					public String getDescription() {
						return "Zip Files";
					}
					
					@Override
					public boolean accept(File f) {
						return f.getName().endsWith(".zip");
					}
				});
				
				chooser.setDialogType(JFileChooser.SAVE_DIALOG);
				
				if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(null)) {

					File f = chooser.getSelectedFile();
					
					onCreatezip();

					createZip(f);
				}
			}
		} finally {

			if (reader != null) try { reader.close(); } catch (Exception e) {}

			for (Pair<File, BufferedWriter> outInfo: outputMap.values()) {
				try { outInfo.second.close(); } catch (Exception e) {}
				outInfo.first.delete();
			}
			
			onFinish();
		}
	}

	protected void onCreatezip() {
	}

	void copy(InputStream in, ZipOutputStream out) throws IOException {
		
		byte[] buff = new byte[4096];
		
		for (int i = in.read(buff); i != -1; i = in.read(buff)) {
			out.write(buff, 0, i);
		}
		
		in.close();
	}
	
	private void createZip(File zip) throws IOException {
		
		//File zip = File.createTempFile("talos-rowing-export", ".zip");
				
		ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zip));
		
		for (Type type: exportSet) {
			
			Pair<File, BufferedWriter> outInfo = outputMap.get(type);
			
			if (outInfo != null) {
				outInfo.second.close();
				ZipEntry entry = new ZipEntry(type.name().toLowerCase() + ".csv");
				zos.putNextEntry(entry);
				
				copy(new FileInputStream(outInfo.first), zos);
				
				zos.closeEntry();
			}
		}
		
		zos.close();
	}

	protected void onFinish() {
	}
	
	protected void onProgress(double d) {
	}
}

