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

package nl.hardijzer.fw.srgcollisions;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.objectweb.asm.*;

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

class ClassInfo {
	public String strName;
	public Set<String> sFields;
	public Set<Method> sMethods;
	public Set<String> sImplements;
	public String strSuperName;
	
	public ClassInfo() {
		this.sFields=new TreeSet<String>();
		this.sMethods=new TreeSet<Method>();
		this.sImplements=new TreeSet<String>();
		this.strName="";
		this.strSuperName="";
	}
}

class ClassInfoCollector implements ClassVisitor {
	ClassInfo info;
	public ClassInfoCollector() {
		info=new ClassInfo();
	}
	@Override
	public void visit(int version, int access, String name, String signature,
			String superName, String[] interfaces) {
		info.strName=name;
		info.strSuperName=superName;
		if (interfaces!=null)
			for (String i : interfaces)
				info.sImplements.add(i);
	}
	@Override
	public AnnotationVisitor visitAnnotation(String arg0, boolean arg1) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void visitAttribute(Attribute arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void visitEnd() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public FieldVisitor visitField(int access, String name, String desc,
			String signature, Object value) {
		info.sFields.add(name);
		return null;
	}
	@Override
	public void visitInnerClass(String arg0, String arg1, String arg2, int arg3) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
			String signature, String[] exceptions) {
		info.sMethods.add(new Method(name,desc));
		return null;
	}
	@Override
	public void visitOuterClass(String arg0, String arg1, String arg2) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void visitSource(String arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}
	
}


public class SrgCollisions {
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		String strInputSrg=null;
		String strInputJar=null;
		for (int i=0; i<args.length; i++) {
			if (args[i].equals("--srg"))
				strInputSrg=args[++i];
			else if (args[i].equals("--in"))
				strInputJar=args[++i];
			else
				System.err.println("Unknown option: "+args[i]);
		}
		if (strInputSrg==null || strInputJar==null) {
			System.err.println("Usage: java -jar srgtool.jar collisions --srg file.srg --in file.jar");
			return;
		}
		Map<String,MappedClass> mapClasses=new TreeMap<String,MappedClass>();
		BufferedReader brSrg=new BufferedReader(new InputStreamReader(new FileInputStream(strInputSrg)));
		String line;
		while ((line=brSrg.readLine())!=null) {
			String[] lineParts=line.split(" ");
			String tmp;
			if (lineParts[0].equals("PK:")) {
				//Package, ignoring for now. We're not actually using that anywhere
			} else if (lineParts[0].equals("CL:")) {
				String strFrom=lineParts[1];
				String strTo=lineParts[2];
				MappedClass mapped=mapClasses.get(strFrom);
				if (mapped!=null) {
					if (!mapped.strNewName.equals(strTo)) {
						System.err.println("Inconsistent class mapping");
						return;
					}
				} else {
					mapClasses.put(strFrom, new MappedClass(strTo));
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
				
				MappedClass mappedClass=mapClasses.get(strFromClass);
				if (mappedClass!=null) {
					if (!mappedClass.strNewName.equals(strToClass)) {
						System.err.println("Inconsistent class mapping");
						return;
					}
				} else {
					mapClasses.put(strFromClass,mappedClass=new MappedClass(strToClass));
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
				
				MappedClass mappedClass=mapClasses.get(strFromClass);
				if (mappedClass!=null) {
					if (!mappedClass.strNewName.equals(strToClass)) {
						System.err.println("Inconsistent class mapping");
						return;
					}
				} else {
					mapClasses.put(strFromClass,mappedClass=new MappedClass(strToClass));
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
		Map<String,ClassInfo> mapInfo=new TreeMap<String,ClassInfo>();
		
		ZipFile zipInput=new ZipFile(strInputJar);
		Enumeration<? extends ZipEntry> entries=zipInput.entries();
		while (entries.hasMoreElements()) {
			ZipEntry entry=entries.nextElement();
			if (entry.isDirectory())
				continue;
			if (entry.getName().endsWith(".class")) {
				ClassReader cr=new ClassReader(zipInput.getInputStream(entry));
				ClassInfoCollector cic=new ClassInfoCollector();
				cr.accept(cic,ClassReader.SKIP_CODE);
				mapInfo.put(cic.info.strName, cic.info);
			}
		}
		zipInput.close();
		
		for (String strClass : mapInfo.keySet()) {
			ClassInfo info=mapInfo.get(strClass);
			MappedClass map=mapClasses.get(strClass);
			if (map==null)
				continue;
			Map<String,String> mapFields=new TreeMap<String,String>();
			Map<Method,Method> mapMethods=new TreeMap<Method,Method>();
			for (String strField : info.sFields) {
				String strMappedField=map.mapFields.get(strField);
				if (strMappedField==null)
					strMappedField=strField;
				String strPrev=mapFields.get(strMappedField);
				if (strPrev!=null) {
					System.out.println("FD: "+strClass+"/"+strPrev+" "+map.strNewName+"/"+strMappedField);
					System.out.println("FD: "+strClass+"/"+strField+" "+map.strNewName+"/"+strMappedField);
					System.out.println("");
				} else {
					mapFields.put(strMappedField,strField);
				}
			}
			for (Method mMethod : info.sMethods) {
				Method mMappedMethod=map.mapMethods.get(mMethod);
				if (mMappedMethod==null)
					mMappedMethod=mMethod;
				Method mPrev=mapMethods.get(mMappedMethod);
				if (mPrev!=null) {
					System.out.println("MD: "+strClass+"/"+mPrev.name+" "+mPrev.desc+" "+map.strNewName+"/"+mMappedMethod.name+" "+mMappedMethod.desc);
					System.out.println("MD: "+strClass+"/"+mMethod.name+" "+mMethod.desc+" "+map.strNewName+"/"+mMappedMethod.name+" "+mMappedMethod.desc);
					System.out.println("");
				} else {
					mapMethods.put(mMappedMethod,mMethod);
				}
			}
		}
	}

}
