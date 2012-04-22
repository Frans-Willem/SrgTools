package nl.hardijzer.fw.reversesrg;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class ReverseSrg {
	private static String join(String[] parts, String delim) {
		StringBuilder b=new StringBuilder();
		for (int i=0; i<parts.length; i++) {
			if (i!=0) b.append(delim);
			b.append(parts[i]);
		}
		return b.toString();
	}
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		if (args.length<1) {
			System.err.println("Usage: java -classpath SrgTools.jar ReverseSrg <srg file>");
			return;
		}
		BufferedReader brSrg=new BufferedReader(new InputStreamReader(new FileInputStream(args[0])));
		String line;
		while ((line=brSrg.readLine())!=null) {
			String[] lineParts=line.split(" ");
			String tmp;
			if (lineParts[0].equals("PK:") || lineParts[0].equals("CL:") || lineParts[0].equals("FD:")) {
				tmp=lineParts[1];
				lineParts[1]=lineParts[2];
				lineParts[2]=tmp;
				line=join(lineParts," ");
				System.out.println(line);
			} else if (lineParts[0].equals("MD:")) {
				tmp=lineParts[1];
				lineParts[1]=lineParts[3];
				lineParts[3]=tmp;
				tmp=lineParts[2];
				lineParts[2]=lineParts[4];
				lineParts[4]=tmp;
				line=join(lineParts," ");
				System.out.println(line);
			} else {
				System.err.println("Unknown command: '"+lineParts[0]+"', aborting");
				return;
			}
		}
	}

}
