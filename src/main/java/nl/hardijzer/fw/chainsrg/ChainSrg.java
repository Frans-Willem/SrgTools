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

package nl.hardijzer.fw.chainsrg;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.SimpleRemapper;

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

class MappedClass {
	public String strNewName;
	public Map<String,String> mapFields;
	public Map<Method,Method> mapMethods;
	
	public MappedClass(String strNewName) {
		this.strNewName=strNewName;
		this.mapFields=new TreeMap<String,String>();
		this.mapMethods=new TreeMap<Method,Method>();
	}
}

public class ChainSrg {

	//Chain fields, similar to chain
	public static Map<String,String> chainFields(Map<String,String> a, Map<String,String> b) {
		Map<String,String> c=new TreeMap<String,String>();
		for (String strFrom : a.keySet()) {
			String strToA=a.get(strFrom);
			String strToB=b.get(strToA);
			if (strToB!=null) {
				b.remove(strToA);
				c.put(strFrom, strToB);
			} else {
				c.put(strFrom, strToA);
			}
		}
		for (String strFrom : b.keySet()) {
			c.put(strFrom, b.get(strFrom));
		}
		return c;
	}
	
	//Chain methods
	public static Map<Method,Method> chainMethods(Map<Method,Method> a, Map<Method,Method> b, Remapper btoa, Remapper btoc) {
		Map<Method,Method> c=new TreeMap<Method,Method>();
		for (Method mFrom : a.keySet()) {
			Method mToA=a.get(mFrom);
			Method mToB=b.get(mToA);
			if (mToB!=null) {
				//We have a mapping from a to b, and b to c, so yay!
				b.remove(mToA);
				c.put(mFrom, mToB);
			} else {
				//We only have a mapping from a to b, so we need to add b to c (descriptor only)
				c.put(mFrom,new Method(mToA.name,btoc.mapMethodDesc(mToA.desc)));
			}
		}
		for (Method mFrom : b.keySet()) {
			//We only have a mapping from b to c, to we need to add a to b (descriptor only)
			c.put(new Method(mFrom.name,btoa.mapMethodDesc(mFrom.desc)), b.get(mFrom));
		}
		return c;
	}
	
	public static Map<String,MappedClass> chain(Map<String,MappedClass> a, Map<String,MappedClass> b) {
		Map<String,String> aback=new TreeMap<String,String>();
		Map<String,String> bforward=new TreeMap<String,String>();
		for (String strFrom : a.keySet())
			aback.put(a.get(strFrom).strNewName,strFrom);
		for (String strFrom : b.keySet())
			bforward.put(strFrom,b.get(strFrom).strNewName);
		Remapper btoa=new SimpleRemapper(aback);
		Remapper btoc=new SimpleRemapper(bforward);
		
		Map<String,MappedClass> c=new TreeMap<String,MappedClass>();
		for (String strFrom : a.keySet()) {
			MappedClass mappedA=a.get(strFrom);
			MappedClass mappedB=b.get(mappedA.strNewName);
			if (mappedB!=null) {
				//Remove from b, so we know which ones we already had.
				b.remove(mappedA.strNewName);
				//Uh-oh, chaining here!
				MappedClass mappedC=new MappedClass(mappedB.strNewName);
				mappedC.mapFields=chainFields(mappedA.mapFields,mappedB.mapFields);
				mappedC.mapMethods=chainMethods(mappedA.mapMethods,mappedB.mapMethods,btoa,btoc);
				c.put(strFrom, mappedC);
			} else {
				//We're safe, no actual chaining
				c.put(strFrom, mappedA);
			}
		}
		//Add in the unchained ones from B
		for (String strFrom : b.keySet()) {
			c.put(strFrom, b.get(strFrom));
		}
		return c;
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		Map<String,MappedClass> mapClasses=new TreeMap<String,MappedClass>();
		for (int i=0; i<args.length; i++) {
			Map<String,MappedClass> mapClassesCurrent=new TreeMap<String,MappedClass>();
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
					MappedClass mapped=mapClassesCurrent.get(strFrom);
					if (mapped!=null) {
						if (!mapped.strNewName.equals(strTo)) {
							System.err.println("Inconsistent class mapping");
							return;
						}
					} else {
						mapClassesCurrent.put(strFrom, new MappedClass(strTo));
					}
				} else if (lineParts[0].equals("FD:")) {
					String strFrom=lineParts[1];
					String strTo=lineParts[2];
					int nSplitFrom=strFrom.lastIndexOf('/');
					int nSplitTo=strTo.lastIndexOf('/');
					if (nSplitFrom==-1 || nSplitTo==-1) {
						System.err.println("ERROR: Invalid field specification");
						return;
					}
					String strFromClass=strFrom.substring(0,nSplitFrom);
					strFrom=strFrom.substring(nSplitFrom+1);
					String strToClass=strTo.substring(0,nSplitTo);
					strTo=strTo.substring(nSplitTo+1);
					
					MappedClass mappedClass=mapClassesCurrent.get(strFromClass);
					if (mappedClass!=null) {
						if (!mappedClass.strNewName.equals(strToClass)) {
							System.err.println("Inconsistent class mapping");
							return;
						}
					} else {
						mapClassesCurrent.put(strFromClass,mappedClass=new MappedClass(strToClass));
					}
					String mappedField=mappedClass.mapFields.get(strFrom);
					if (mappedField!=null) {
						if (!mappedField.equals(strTo)) {
							System.err.println("Inconsistent field mapping");
							return;
						}
					} else {
						mappedClass.mapFields.put(strFrom,strTo);
					}
				} else if (lineParts[0].equals("MD:")) {
					String strFrom=lineParts[1];
					String strTo=lineParts[3];
					int nSplitFrom=strFrom.lastIndexOf('/');
					int nSplitTo=strTo.lastIndexOf('/');
					if (nSplitFrom==-1 || nSplitTo==-1) {
						System.err.println("ERROR: Invalid field specification");
						return;
					}
					String strFromClass=strFrom.substring(0,nSplitFrom);
					strFrom=strFrom.substring(nSplitFrom+1);
					String strToClass=strTo.substring(0,nSplitTo);
					strTo=strTo.substring(nSplitTo+1);
					
					MappedClass mappedClass=mapClassesCurrent.get(strFromClass);
					if (mappedClass!=null) {
						if (!mappedClass.strNewName.equals(strToClass)) {
							System.err.println("Inconsistent class mapping");
							return;
						}
					} else {
						mapClassesCurrent.put(strFromClass,mappedClass=new MappedClass(strToClass));
					}
					Method mFrom=new Method(strFrom,lineParts[2]);
					Method mTo=new Method(strTo,lineParts[4]);
					
					Method mappedMethod=mappedClass.mapMethods.get(mFrom);
					if (mappedMethod!=null) {
						if (mappedMethod.compareTo(mTo)!=0) {
							System.err.println("Inconsistent method mapping");
							return;
						}
					} else {
						mappedClass.mapMethods.put(mFrom,mTo);
					}
				} else {
					System.err.println("Unknown command: '"+lineParts[0]+"', aborting");
					return;
				}
			}
			mapClasses=chain(mapClasses,mapClassesCurrent);
		}
		
		for (String strFrom : mapClasses.keySet()) {
			String strTo=mapClasses.get(strFrom).strNewName;
			if (!strFrom.equals(strTo))
				System.out.println("CL: "+strFrom+" "+strTo);
		}
		for (String strFromClass : mapClasses.keySet()) {
			MappedClass mappedClass=mapClasses.get(strFromClass);
			String strToClass=mappedClass.strNewName;
			for (String strFrom : mappedClass.mapFields.keySet()) {
				String strTo=mappedClass.mapFields.get(strFrom);
				if (!strFromClass.equals(strToClass) || !strFrom.equals(strTo))
					System.out.println("FD: "+strFromClass+"/"+strFrom+" "+strToClass+"/"+strTo);
			}
		}
		for (String strFromClass : mapClasses.keySet()) {
			MappedClass mappedClass=mapClasses.get(strFromClass);
			String strToClass=mappedClass.strNewName;
			for (Method mFrom : mappedClass.mapMethods.keySet()) {
				Method mTo=mappedClass.mapMethods.get(mFrom);
				if (!strFromClass.equals(strToClass) || mFrom.compareTo(mTo)!=0)
					System.out.println("MD: "+strFromClass+"/"+mFrom.name+" "+mFrom.desc+" "+strToClass+"/"+mTo.name+" "+mTo.desc);
			}
		}
	}

}
