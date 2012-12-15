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

package nl.hardijzer.fw.checkabstract;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.*;

class ClassInfo {
	public String strName;
	public String strZipFilename;
	public Set<String> setImplements;
	public Set<String> setCreates;
	public Set<Method> setMethods;
	public Set<Method> setAbstractMethods;
	public Map<Method,String> mapMethodOwner;
	public boolean bIsAbstract;
	
	public ClassInfo() {
		strName="";
		setImplements=new HashSet<String>();
		setCreates=new HashSet<String>();
		setMethods=new HashSet<Method>();
		setAbstractMethods=new HashSet<Method>();
		mapMethodOwner=new HashMap<Method,String>();
	}
}

class ClassInfoGatherer extends EmptyVisitor {
	ClassInfo info;
	
	public ClassInfoGatherer() {
		info=new ClassInfo();
	}
	
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		info.bIsAbstract=((access & Opcodes.ACC_ABSTRACT)!=0);
		info.strName=name;
		info.setImplements.add(superName);
		for (String i : interfaces)
			info.setImplements.add(i);
		super.visit(version,access,name,signature,superName,interfaces);
	}
	
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		Method m=new Method(name,desc);
		boolean bAbstract=((access & Opcodes.ACC_ABSTRACT)!=0);
		if (bAbstract)
			info.setAbstractMethods.add(m);
		else
			info.setMethods.add(m);
		info.mapMethodOwner.put(m,info.strName);
		return super.visitMethod(access,name,desc,signature,exceptions);
	}
}

public class CheckAbstract {
	
	private static String repeatString(String s, int n) {
		StringBuilder sb=new StringBuilder();
		while (n-->0)
			sb.append(s);
		return sb.toString();
	}
	
	private static String typeToString(Type t) {
		switch (t.getSort()) {
			case Type.ARRAY:
				return typeToString(t.getElementType())+repeatString("[]",t.getDimensions());
			case Type.BOOLEAN: return "boolean";
			case Type.BYTE: return "byte";
			case Type.CHAR: return "char";
			case Type.DOUBLE: return "double";
			case Type.FLOAT: return "float";
			case Type.INT: return "int";
			case Type.LONG: return "long";
			case Type.OBJECT: return t.getClassName().replace('/', '.');
			case Type.SHORT: return "short";
			case Type.VOID: return "void";
			default:
				return "Unknown";
		}
	}
	
	private static String methodToString(Method m) {
		StringBuilder sb=new StringBuilder();
		sb.append(typeToString(m.getReturnType()));
		sb.append(" ");
		sb.append(m.getName());
		sb.append("(");
		boolean bFirst=true;
		for (Type t : m.getArgumentTypes()) {
			if (!bFirst)
				sb.append(", ");
			bFirst=false;
			sb.append(typeToString(t));
		}
		sb.append(")");
		return sb.toString();
	}
	
	public static void main(String[] args) throws IOException {
		List<String> listInput=new LinkedList<String>();
		Set<String> setOutput=new TreeSet<String>();
		for (int i=0; i<args.length; i++)
			if (args[i].equals("--parse")) {
				listInput.add(args[++i]);
			} else if (args[i].equals("--check")) {
				String a=args[++i];
				listInput.add(a);
				setOutput.add(a);
		}
		System.out.println("Generating class map");
		Map<String,ClassInfo> mapClassInfo=new TreeMap<String,ClassInfo>();
		for (String strInput : listInput) {
			System.out.println("Reading in "+strInput);
			ZipFile zipInput=new ZipFile(strInput);
			Enumeration<? extends ZipEntry> entries=zipInput.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry=entries.nextElement();
				if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
					ClassReader reader=new ClassReader(zipInput.getInputStream(entry));
					ClassInfoGatherer gatherer=new ClassInfoGatherer();
					reader.accept(gatherer,0);
					gatherer.info.strZipFilename=strInput;
					mapClassInfo.put(gatherer.info.strName, gatherer.info);
				}
			}
			zipInput.close();
		}
		System.out.println("Classes found: "+mapClassInfo.size());
		Set<String> setDone=new TreeSet<String>();
		LinkedList<String> qToDo=new LinkedList<String>();
		qToDo.addAll(mapClassInfo.keySet());
		while (qToDo.size() > 0) {
			String strCurrent=qToDo.peek();
			if (setDone.contains(strCurrent)) {
				qToDo.remove();
				continue;
			}
			ClassInfo info=mapClassInfo.get(strCurrent);
			if (info==null) {
				if (!strCurrent.startsWith("java/") && !strCurrent.startsWith("javax/"))
					System.err.println("Class not found: "+strCurrent);
				qToDo.remove();
				setDone.add(strCurrent);
				//Add a simple one for later
				info=new ClassInfo();
				info.strName=strCurrent;
				mapClassInfo.put(info.strName,info);
				continue;
			}
			int nSizeBefore=qToDo.size();
			for (String i : info.setImplements) {
				if (!setDone.contains(i))
					qToDo.addFirst(i);
			}
			//Added? do the other classes first, we'll get back here later
			if (nSizeBefore!=qToDo.size())
				continue;
			//Remove us from the list, and mark as done
			qToDo.remove();
			setDone.add(strCurrent);
			//Copy methods
			for (String i : info.setImplements) {
				ClassInfo parentinfo=mapClassInfo.get(i);
				for (Method m : parentinfo.setAbstractMethods) {
					info.setAbstractMethods.add(m);
					info.mapMethodOwner.put(m, parentinfo.mapMethodOwner.get(m));
				}
				for (Method m : parentinfo.setMethods) {
					info.setMethods.add(m);
					info.mapMethodOwner.put(m, parentinfo.mapMethodOwner.get(m));
				}
			}
			if (setOutput.contains(info.strZipFilename)) {
				//Check abstract not implemented methods
				if (!info.bIsAbstract) {
					//System.out.println(strCurrent+" checking");
					for (Method m : info.setAbstractMethods) {
						if (!info.setMethods.contains(m)) {
							System.out.println("Error in "+info.strName.replace('/','.')+" ("+info.strZipFilename+")");
							System.out.println("\tNo implementation found for abstract method");
							System.out.println("\tOwner: "+info.mapMethodOwner.get(m).replace('/','.'));
							System.out.println("\tMethod: "+methodToString(m));
							System.out.println("\tDescriptor: "+m.getDescriptor());
						}
					}
				}
			}
		}
	}

}
