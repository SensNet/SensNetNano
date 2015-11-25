import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.concurrent.LinkedBlockingQueue;

public class Uploader implements OnPromptReadyListener {
	private File device;
	private PrintWriter outWrite;
	private LinkedBlockingQueue<String> toPrompt = new LinkedBlockingQueue<String>();

	public Uploader(File device) throws InterruptedException, IOException {
		this.device = device;
		// SerialPort.setBaudRate(device.getAbsolutePath(), 9600);
		new Thread(new Runnable() {

			@Override
			public void run() {
				try (InputStreamReader read = new InputStreamReader(
						new FileInputStream(device))) {

					char tmp;
					char last = ' ';
					while ((tmp = (char) read.read()) != -1) {
						System.err.print(tmp);
						if (tmp == '>' && last == '\n') {
							promtReady();
						}
						last = tmp;
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.out.println("END.");

				System.exit(0);
			}
		}).start();
		outWrite = new PrintWriter(new FileOutputStream(device), true);
		outWrite.println();
	}

	public void initOS() throws InterruptedException, IOException {
		writeFile(new File("init.lua"));
		up();
		restart();
	}

	public void restart() throws InterruptedException {
		System.out.println("Restart scheduled.");
		toPrompt.put("node.restart()");
		Thread.sleep(5000);
		up();
	}

	private void up() throws InterruptedException {
		if (ready) {
			promtReady();
		}
	}

	public void writeFile(File in) throws IOException, InterruptedException {
		BufferedReader tempRead = new BufferedReader(new InputStreamReader(
				new FileInputStream(in)));
		String tmp = null;
		toPrompt.put("file.open([[" + in.getName() + "]],[[w+]])");
		while ((tmp = tempRead.readLine()) != null) {
			if (tmp.trim().isEmpty() || tmp.trim().startsWith("--")) {
				continue;
			}
			toPrompt.put("file.writeline([[" + tmp + "]])");
		}
		toPrompt.put("file.close()");
		tempRead.close();
		up();
	}

	public void runFileDirect(File in) throws IOException, InterruptedException {
		BufferedReader tempRead = new BufferedReader(new InputStreamReader(
				new FileInputStream(in)));
		String tmp = null;
		while ((tmp = tempRead.readLine()) != null) {
			if (tmp.trim().isEmpty() || tmp.trim().startsWith("--")) {
				continue;
			}
			toPrompt.put(tmp);
		}
		tempRead.close();
		up();
	}

	public void runFile(File in) throws IOException, InterruptedException {
		writeFile(in);
		toPrompt.put("dofile([[" + in.getName() + "]])");
		up();
	}

	private boolean ready = false;

	@Override
	public void promtReady() throws InterruptedException {
		if (toPrompt.isEmpty()) {
			ready = true;
			return;
		}
		outWrite.println(toPrompt.poll());
		ready = false;
	}

	public void runDevice(String string) throws InterruptedException {
		toPrompt.put("dofile([[" + string + "]]);");
		up();
	}

	public void runPrompt(String command) throws InterruptedException {
		toPrompt.put(command);
		up();
	}

	public boolean isBusy() {
		return !toPrompt.isEmpty();
	}

}
