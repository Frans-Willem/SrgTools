package nl.hardijzer.fw.chainsrg;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

class Method implements Comparable<Method> {
	public String name;
	public String desc;
	public Method(String strName, String strArguments) {
		this.name=strName;
		this.desc=strArguments;
	}
	
	@Override
	public int compareTo(Method b) {
		int cmpName=name.compareTo(b.name);
		int cmpDesc=desc.compareTo(b.desc);
		if (cmpName!=0) return cmpName;
		return cmpDesc;
	}
}

public class ChainSrg {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		Map<String,String> mapClassesPrev=new TreeMap<String,String>();
		Map<String,String> mapFieldsPrev=new TreeMap<String,String>();
		Map<Method,Method> mapMethodsPrev=new TreeMap<Method,Method>();
		for (int i=args.length-1; i>=0; i--) {
			Map<String,String> mapClasses=new TreeMap<String,String>();
			Map<String,String> mapFields=new TreeMap<String,String>();
			Map<Method,Method> mapMethods=new TreeMap<Method,Method>();
			BufferedReader brSrg=new BufferedReader(new InputStreamReader(new FileInputStream(args[i])));
			String line;
			while ((line=brSrg.readLine())!=null) {
				String[] lineParts=line.split(" ");
				String tmp;
				if (lineParts[0].equals("PK:")) {
					//Package, ignoring for now. We're not actually using that anywhere
				} else if (lineParts[0].equals("CL:")) {
					String strFrom=lineParts[1];
					String strTo=lineParts[2];
					String strToMapped=mapClassesPrev.get(strTo);
					if (strToMapped!=null) {
						mapClassesPrev.remove(strTo);
						strTo=strToMapped;
					}
					mapClasses.put(strFrom, strTo);
				} else if (lineParts[0].equals("FD:")) {
					String strFrom=lineParts[1];
					String strTo=lineParts[2];
					String strToMapped=mapFieldsPrev.get(strTo);
					if (strToMapped!=null) {
						mapFieldsPrev.remove(strTo);
						strTo=strToMapped;
					}
					mapFields.put(strFrom,strTo);
				} else if (lineParts[0].equals("MD:")) {
					Method mFrom=new Method(lineParts[1],lineParts[2]);
					Method mTo=new Method(lineParts[3],lineParts[4]);
					Method mToMapped=mapMethodsPrev.get(mTo);
					if (mToMapped!=null) {
						mapMethodsPrev.remove(mTo);
						mTo=mToMapped;
					}
					mapMethods.put(mFrom,mTo);
				} else {
					System.err.println("Unknown command: '"+lineParts[0]+"', aborting");
					return;
				}
			}
			
			//Insert all non-extended rules
			for (String strFrom : mapClassesPrev.keySet())
				mapClasses.put(strFrom,mapClassesPrev.get(strFrom));
			for (String strFrom : mapFieldsPrev.keySet())
				mapFields.put(strFrom,mapFieldsPrev.get(strFrom));
			for (Method mFrom : mapMethodsPrev.keySet())
				mapMethods.put(mFrom,mapMethodsPrev.get(mFrom));
			mapClassesPrev=mapClasses;
			mapFieldsPrev=mapFields;
			mapMethodsPrev=mapMethods;
		}
		
		for (String strFrom : mapClassesPrev.keySet())
			System.out.println("CL: "+strFrom+" "+mapClassesPrev.get(strFrom));
		for (String strFrom : mapFieldsPrev.keySet())
			System.out.println("FD: "+strFrom+" "+mapFieldsPrev.get(strFrom));
		for (Method mFrom : mapMethodsPrev.keySet()) {
			Method mTo=mapMethodsPrev.get(mFrom);
			System.out.println("MD: "+mFrom.name+" "+mFrom.desc+" "+mTo.name+" "+mTo.desc);
		}
	}

}
