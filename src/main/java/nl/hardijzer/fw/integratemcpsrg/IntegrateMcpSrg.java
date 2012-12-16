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

package nl.hardijzer.fw.integratemcpsrg;


import java.io.*;
import java.util.*;

import au.com.bytecode.opencsv.*;

class CsvInfo {
	String name;
	String desc;
	public CsvInfo(String name, String desc) {
		this.name=name;
		this.desc=desc;
	}
}

public class IntegrateMcpSrg {
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
		if (args.length<4) {
			System.err.println("Usage: java -jar srgtool.jar integratemcp <server/client.srg> <fields.csv> <methods.csv> <side:client=0,server=1>");
			return;
		}
		int nSide=Integer.parseInt(args[3]);
		CSVReader csvFields=new CSVReader(new InputStreamReader(new FileInputStream(args[1])));
		CSVReader csvMethods=new CSVReader(new InputStreamReader(new FileInputStream(args[2])));
		Map<String,CsvInfo> mapFields=new HashMap<String,CsvInfo>();
		Map<String,CsvInfo> mapMethods=new HashMap<String,CsvInfo>();
		String[] headerFields=csvFields.readNext();
		String[] headerMethods=csvMethods.readNext();
		String[] lineParts;
		while ((lineParts=csvFields.readNext())!=null)
			if (Integer.parseInt(lineParts[2])==nSide)
				mapFields.put(lineParts[0],new CsvInfo(lineParts[1],lineParts[3]));
		while ((lineParts=csvMethods.readNext())!=null)
			if (Integer.parseInt(lineParts[2])==nSide)
				mapMethods.put(lineParts[0],new CsvInfo(lineParts[1],lineParts[3]));
		System.out.println("Loaded "+mapFields.size()+" fields and "+mapMethods.size()+" methods");
		BufferedReader brSrg=new BufferedReader(new InputStreamReader(new FileInputStream(args[0])));
		String line;
		while ((line=brSrg.readLine())!=null) {
			lineParts=line.split(" ");
			if (lineParts[0].equals("PK:")) {
				//Package, ignoring for now. We're not actually using that...
			} else if (lineParts[0].equals("CL:")) {
				//No need to adjust classes
				System.out.println(line);
			} else if (lineParts[0].equals("FD:")) {
				String[] fieldParts=lineParts[2].split("/");
				int nLast=fieldParts.length-1;
				CsvInfo newField=mapFields.get(fieldParts[nLast]);
				if (newField!=null) {
					fieldParts[nLast]=newField.name;
					lineParts[2]=join(fieldParts,"/");
					line=join(lineParts," ");
				}
				System.out.println(line);
			} else if (lineParts[0].equals("MD:")) {
				String[] methodParts=lineParts[3].split("/");
				int nLast=methodParts.length-1;
				CsvInfo newMethod=mapMethods.get(methodParts[nLast]);
				if (newMethod!=null) {
					methodParts[nLast]=newMethod.name;
					lineParts[3]=join(methodParts,"/");
					line=join(lineParts," ");
				}
				System.out.println(line);
			} else {
				System.err.println("Unknown command: '"+lineParts[0]+"', aborting");
				return;
			}
		}
	}

}
