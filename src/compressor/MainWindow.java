package compressor;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.DirectoryDialog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Label;
import org.eclipse.wb.swt.SWTResourceManager;

// TODO: Fix dialog cancel causes silent crash

public class MainWindow {

	protected Shell shell;

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			MainWindow window = new MainWindow();
			window.open();
		} catch (Exception e) {
			return;
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shell = new Shell();
		shell.setSize(450, 300);
		shell.setText("720压缩（绿色版）");
		
		final ProgressBar convertBar = new ProgressBar(shell, SWT.NONE);
		convertBar.setMinimum(0);
		convertBar.setMaximum(100);
		convertBar.setBounds(81, 156, 262, 28);
		
		final Label convertState = new Label(shell, SWT.NONE);
		convertState.setFont(SWTResourceManager.getFont("微软雅黑", 9, SWT.NORMAL));
		convertState.setBounds(163, 194, 104, 28);
		convertState.setText("✔压缩成功");
		convertState.setVisible(false);
		
		Button chooseFiles = new Button(shell, SWT.NONE);
		chooseFiles.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				String[] paths = handleChooseFiles();
				for(int i = 0; i < paths.length; i++) {
					System.out.println(paths[i]);
				}
				try {
					int end = paths[0].length() - 1;
					while(paths[0].charAt(end) != '\\') {
						end--;
					}
					end++;
					convertState.setVisible(true);
					convertState.setText("🛠压缩中...");
					zipFiles(paths, paths[0].substring(0, end), convertBar);
					convertState.setText("✔压缩成功");
					generateDialog(SWT.ICON_WORKING | SWT.OK, "转换成功", "完成");
				}
				catch(IOException ex) {
				}
			}
		});
		chooseFiles.setBounds(153, 40, 114, 34);
		chooseFiles.setText("选择文件");
		
		Button chooseDirectory = new Button(shell, SWT.NONE);
		chooseDirectory.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				String directoryPath = handleChooseDirectory();
		        FileOutputStream fos = null;
				try {
					fos = new FileOutputStream(directoryPath + ".zip");
				} catch (FileNotFoundException e1) {
					return;
				}
		        ZipOutputStream zipOut = new ZipOutputStream(fos);
		        File fileToZip = new File(directoryPath);
		        try {
					convertState.setVisible(true);
					convertState.setText("🛠压缩中...");
		        	convertBar.setSelection(0);
					zipDirectory(fileToZip, fileToZip.getName(), zipOut, convertBar);
	            	zipOut.close();
		        	convertBar.setSelection(100);
					convertState.setText("✔压缩成功");
					generateDialog(SWT.ICON_WORKING | SWT.OK, "转换成功", "完成");
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				if(directoryPath != null) {
					// filePathLabel.setText(directoryPath);
				}
			}
		});
		chooseDirectory.setBounds(153, 98, 114, 34);
		chooseDirectory.setText("选择文件夹");

	}
	
	private void generateDialog(int SWT_ICON, String title, String message) {
		MessageBox box = new MessageBox(shell, SWT_ICON);
		box.setText(title);
		box.setMessage(message);
		box.open();
	}
	
	public String[] handleChooseFiles() {
		FileDialog fileDialog = new FileDialog(shell, SWT.MULTI);
		fileDialog.setFilterExtensions(new String[] { "*.*" });
		fileDialog.setFilterPath("c:\\");
		fileDialog.open();
		String[] names = fileDialog.getFileNames();
		String path = fileDialog.getFilterPath();
		for(int i = 0; i < names.length; i++) {
			names[i] = path + "\\" + names[i];
		}
		System.out.println("AA" + names[0]);
		return names;
	}
	
	public String handleChooseDirectory() {
		DirectoryDialog directoryDialog = new DirectoryDialog(shell, SWT.OPEN);
		String result = directoryDialog.open();
		return result;
	}
	
	public static void zipFiles(String[] files, String path, ProgressBar convertBar) throws IOException{
        List<String> srcFiles = Arrays.asList(files);
        FileOutputStream fos = new FileOutputStream(files[0] + ".zip");
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        convertBar.setSelection(10);
        int counter = 0;
        for (String srcFile : srcFiles) {
            File fileToZip = new File(srcFile);
            FileInputStream fis = new FileInputStream(fileToZip);
            ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
            zipOut.putNextEntry(zipEntry);

            byte[] bytes = new byte[1024];
            int length;
            while((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
            fis.close();
            convertBar.setSelection(10 + counter / files.length * 80);
            counter++;
        }
        zipOut.close();
        fos.close();
        convertBar.setSelection(100);
	}
	
    public static void zipDirectory(File fileToZip, String fileName, ZipOutputStream zipOut, ProgressBar convertBar) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            if (fileName.endsWith("/")) {
                zipOut.putNextEntry(new ZipEntry(fileName));
                zipOut.closeEntry();
            } else {
                zipOut.putNextEntry(new ZipEntry(fileName + "/"));
                zipOut.closeEntry();
            }
            File[] children = fileToZip.listFiles();
            for (File childFile : children) {
            	convertBar.setSelection(50);
            	zipDirectory(childFile, fileName + "/" + childFile.getName(), zipOut, convertBar);
            }
            return;
        }
        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        fis.close();
    }
    
    public static String getNameAfterSlash(String name) {
    	int end = name.length() - 1;
    	while(name.charAt(end) != '\\') {
    		end--;
    	}
    	end++;
    	return name.substring(end, name.length());
    }
}
