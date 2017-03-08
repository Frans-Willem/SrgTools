/*
 * Copyright 2012 Frans-Willem Hardijzer
 * This file is part of SrgTools.
 *
 * SrgTools is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SrgTools is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with SrgTools.  If not, see <http://www.gnu.org/licenses/>.
 */

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
			System.err.println("Usage: java -jar srgtool.jar reverse <srg file>");
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
