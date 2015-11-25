import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class Main {
	public static void main(String[] args) throws InterruptedException,
			IOException {
		Uploader up = new Uploader(new File("/dev/ttyUSB1"));
		try (Scanner s = new Scanner(System.in)) {
			boolean exit = false;
			String tmp;
			while ((tmp = s.nextLine()) != null && !exit) {
				switch (tmp.trim()) {
				case "exit":
					exit = true;
					continue;
				case "restart":
					up.restart();
					continue;
				case "init":
					up.initOS();
					continue;
				default:
					break;
				}
				String[] parts = tmp.split(" ");
				switch (parts[0]) {
				case "run":
					up.runFile(new File(parts[1]));
					continue;
				case "rrun":
					up.restart();
					Thread.sleep(10000);
					up.runFile(new File(parts[1]));
					continue;
				case "runDevice":
					up.runDevice(parts[1]);
					continue;
				case "upload":
					up.writeFile(new File(parts[1]));
					continue;
				case "init":
					up.initOS();
					System.out
							.println("Waiting for device and all pending tasks..");
					Thread.sleep(10000);
					up.writeFile(new File(parts[1]));
					up.runPrompt("file.open([[init.lua]], [[a+]]) file.writeline([[dofile('"
							+ parts[1] + "')]]) file.close()");
					up.restart();
				default:
					break;
				}
				up.runPrompt(tmp);
			}
		}
		System.exit(0);
	}
}
