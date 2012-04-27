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

package nl.hardijzer.fw.jarcompare;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.jar.*;
import java.util.zip.ZipEntry;

import org.objectweb.asm.*;

import java.io.*;

class Field {
	public String owner;
	public String name;
	public Field(String owner, String name) {
		this.owner=owner;
		this.name=name;
	}
	
	public boolean equals(Field b) {
		return this.owner.equals(b.owner) && this.name.equals(b.name);
	}
}

class FieldComparator implements Comparator<Field> {
	public int compare(Field a, Field b) {
		int cmpOwner=a.owner.compareTo(b.owner);
		int cmpName=a.name.compareTo(b.name);
		if (cmpOwner<0 || cmpOwner>0) return cmpOwner;
		return cmpName;
	}
}

class Method {
	public String owner;
	public String name;
	public String arguments;
	public Method(String owner, String name, String arguments) {
		this.owner=owner;
		this.name=name;
		this.arguments=arguments;
	}
	public boolean equals(Method b) {
		return this.owner.equals(b.owner) && this.name.equals(b.name)&& this.arguments.equals(b.arguments); 
	}
}

class MethodComparator implements Comparator<Method> {
	public int compare(Method a, Method b) {
		int cmpOwner=a.owner.compareTo(b.owner);
		int cmpName=a.name.compareTo(b.name);
		int cmpArguments=a.arguments.compareTo(b.arguments);
		if (cmpOwner<0 || cmpOwner>0) return cmpOwner;
		if (cmpName<0 || cmpName>0) return cmpName;
		return cmpArguments;
	}
}


class MyVisitor implements ClassVisitor, MethodVisitor {
	public ArrayList<String> arrClasses=new ArrayList<String>();
	public ArrayList<Field> arrFields=new ArrayList<Field>();
	public ArrayList<Method> arrMethods=new ArrayList<Method>();
	public String strMe="";
	
	@Override
	public void visit(int version, int access, String name, String signature,
			String superName, String[] interfaces) {
		strMe=name;
		arrClasses.add(name);
		arrClasses.add(superName);
		if (interfaces!=null) {
			for (int i=0;i<interfaces.length; i++)
				arrClasses.add(interfaces[i]);
		}
	}

	@Override
	public AnnotationVisitor visitAnnotation(String arg0, boolean arg1) {
		//No need to visit annotations
		return null;
	}

	@Override
	public void visitAttribute(Attribute arg0) {
		
	}

	@Override
	public void visitEnd() {

	}

	@Override
	public FieldVisitor visitField(int access, String name, String desc,
			String signature, Object value) {
		arrFields.add(new Field(strMe,name));
		return null;
	}

	@Override
	public void visitInnerClass(String name, String outerName, String innerName, int access) {
		//TODO?
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
			String signature, String[] exceptions) {
		arrMethods.add(new Method(strMe,name,desc));
		visitTypes(Type.getArgumentTypes(desc));
		visitType(Type.getReturnType(desc));
		return this;
	}

	@Override
	public void visitOuterClass(String owner, String name, String desc) {
	}

	@Override
	public void visitSource(String source, String debug) {
	}

	@Override
	public AnnotationVisitor visitAnnotationDefault() {
		return null;
	}

	@Override
	public void visitCode() {
	}

	@Override
	public void visitFieldInsn(int opcode, String owner, String name, String desc) {
		arrClasses.add(owner);
		arrFields.add(new Field(owner,name));
	}

	@Override
	public void visitFrame(int type, int nLocal, Object[] local, int nStack,
			Object[] stack) {
	}

	@Override
	public void visitIincInsn(int var, int increment) {
	}

	@Override
	public void visitInsn(int opcode) {
	}

	@Override
	public void visitIntInsn(int opcode, int operand) {
	}

	@Override
	public void visitJumpInsn(int opcode, Label label) {
	}

	@Override
	public void visitLabel(Label label) {
	}

	@Override
	public void visitLdcInsn(Object cst) {
		if (cst instanceof Type) {
			visitType((Type)cst);
		}	
	}

	@Override
	public void visitLineNumber(int line, Label start) {
	}

	@Override
	public void visitLocalVariable(String name, String desc, String signature,
			Label start, Label end, int index) {
	}

	@Override
	public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
	}

	@Override
	public void visitMaxs(int maxStack, int maxLocals) {
	}

	@Override
	public void visitMethodInsn(int opcode, String owner, String name, String desc) {
		arrClasses.add(owner);
		arrMethods.add(new Method(owner,name,desc));
		visitTypes(Type.getArgumentTypes(desc));
		visitType(Type.getReturnType(desc));
	}

	@Override
	public void visitMultiANewArrayInsn(String desc, int dims) {
	}

	@Override
	public AnnotationVisitor visitParameterAnnotation(int parameter, String desc,
			boolean visible) {
		return null;
	}

	@Override
	public void visitTableSwitchInsn(int min, int max, Label dflt,
			Label[] labels) {
	}

	@Override
	public void visitTryCatchBlock(Label start, Label end, Label handler,
			String type) {
	}

	@Override
	public void visitTypeInsn(int opcode, String type) {
		arrClasses.add(type);
	}

	@Override
	public void visitVarInsn(int opcode, int var) {
	}
	
	public void visitType(Type t) {
		if (t.getSort()==Type.OBJECT)
			arrClasses.add(t.getInternalName());
		if (t.getSort()==Type.ARRAY)
			visitType(t.getElementType());
	}
	
	public void visitTypes(Type t[]) {
		for (int i=0; i<t.length; i++)
			visitType(t[i]);
	}
}

public class JarCompare {
	
	public static Type mapType(Type t, Map<String,String> mapClasses) {
		switch (t.getSort()) {
		case Type.VOID:
		case Type.BOOLEAN:
		case Type.CHAR:
		case Type.SHORT:
		case Type.INT:
		case Type.FLOAT:
		case Type.LONG:
		case Type.DOUBLE:
			return t;
		case Type.ARRAY:
			Type tElement=t.getElementType();
			Type tElementMapped=mapType(tElement,mapClasses);
			if (!tElement.equals(tElementMapped)) {
				//BEWARE! Hacks!
				String strType=t.getDescriptor();
				String strElement=tElement.getDescriptor();
				String strElementMapped=tElementMapped.getDescriptor();
				String strMapped=strType.replaceAll(strElement,strElementMapped);
				return Type.getType(strMapped);
			}
			return t;
		case Type.OBJECT:
			String strMapped=mapClasses.get(t.getInternalName());
			if (strMapped!=null)
				return Type.getObjectType(strMapped);
			return t;
		default:
			return t;
		}
	}
	
	public static String convertMethodDesc(String desc, Map<String,String> mapClasses) {
		Type[] tArgs=Type.getArgumentTypes(desc);
		Type tReturn=Type.getReturnType(desc);
		for (int i=0; i<tArgs.length; i++)
			tArgs[i]=mapType(tArgs[i],mapClasses);
		tReturn=mapType(tReturn,mapClasses);
		return Type.getMethodDescriptor(tReturn,tArgs);
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		if (args.length<2) {
			System.err.println("Usage: java -jar JarCompare.jar <from.jar> <to.jar>");
			return;
		}
		JarFile jarFirst=new JarFile(args[0]);
		JarFile jarSecond=new JarFile(args[1]);
		Manifest mfFirst=jarFirst.getManifest();
		Manifest mfSecond=jarSecond.getManifest();
		String mainFirst=mfFirst.getMainAttributes().getValue("Main-Class");
		String mainSecond=mfSecond.getMainAttributes().getValue("Main-Class");
		Map<String,String> mapClasses=new HashMap<String,String>();
		Map<Field,Field> mapFields=new TreeMap<Field,Field>(new FieldComparator());
		Map<Method,Method> mapMethods=new TreeMap<Method,Method>(new MethodComparator());
		Set<String> setClasses=new TreeSet<String>();
		mapClasses.put(mainFirst.replace('.','/'), mainSecond.replace('.','/'));
		Queue<String> q=new LinkedList<String>();
		q.add(mainFirst.replace('.','/'));
		while (!q.isEmpty()) {
			String classFirst=q.remove();
			String classSecond=mapClasses.get(classFirst);
			String fileFirst=classFirst+".class";
			String fileSecond=classSecond+".class";
			ZipEntry zeFirst=jarFirst.getEntry(fileFirst);
			ZipEntry zeSecond=jarSecond.getEntry(fileSecond);
			if (zeFirst == null && zeSecond == null) {
				continue;
			}
			if (zeFirst==null || zeSecond==null) {
				System.err.println("Classes "+classFirst+" or "+classSecond+" not found");
				return;
			}
			setClasses.add(classFirst);
			//System.out.println("Comparing "+classFirst+" to "+classSecond);
			ClassReader crFirst=new ClassReader(jarFirst.getInputStream(zeFirst));
			ClassReader crSecond=new ClassReader(jarSecond.getInputStream(zeSecond));
			MyVisitor cvFirst=new MyVisitor();
			MyVisitor cvSecond=new MyVisitor();
			crFirst.accept(cvFirst,0);
			crSecond.accept(cvSecond,0);
			if (
					cvFirst.arrClasses.size()!=cvSecond.arrClasses.size() ||
					cvFirst.arrFields.size()!=cvSecond.arrFields.size() ||
					cvFirst.arrMethods.size()!=cvSecond.arrMethods.size()
					)
			{
				System.err.println("Classes "+classFirst+" or "+classSecond+" do not match");
				return;
			}
			for (int c=0; c<cvFirst.arrClasses.size(); c++) {
				String strFrom=cvFirst.arrClasses.get(c);
				String strTo=cvSecond.arrClasses.get(c);
				if (mapClasses.containsKey(strFrom)) {
					if (!mapClasses.get(strFrom).equals(strTo)) {
						System.err.println("Mismatching mappings from "+strTo);
						return;
					}
				} else {
					mapClasses.put(strFrom, strTo);
					q.add(strFrom);
				}
			}
			for (int f=0; f<cvFirst.arrFields.size(); f++) {
				Field fFrom=cvFirst.arrFields.get(f);
				Field fTo=cvSecond.arrFields.get(f);
				if (mapFields.containsKey(fFrom)) {
					if (!mapFields.get(fFrom).equals(fTo)) {
						Field fPrev=mapFields.get(fFrom);
						System.err.println("Mismatching mappings from field "+fFrom.owner+" "+fFrom.name);
						System.err.println("Previous mapping was "+fPrev.owner+" "+fPrev.name);
						System.err.println("New mapping is "+fTo.owner+" "+fTo.name);
						return;
					}
				} else {
					if (fTo.owner.equals(mapClasses.get(fFrom.owner))) {
						mapFields.put(fFrom,fTo);
					} else {
						System.err.println("Fields moved between classes, mismatch!");
						return;
					}
				}
			}
			for (int m=0; m<cvFirst.arrMethods.size(); m++) {
				Method mFrom=cvFirst.arrMethods.get(m);
				Method mTo=cvSecond.arrMethods.get(m);
				if (mapMethods.containsKey(mFrom)) {
					if (!mapMethods.get(mFrom).equals(mTo)) {
						System.err.println("Mismatching mappings from method "+mFrom.owner+" "+mFrom.name+" "+mFrom.arguments);
						return;
					}
				} else {
					if (mTo.owner.equals(mapClasses.get(mFrom.owner))) {
						String mToDesc=convertMethodDesc(mFrom.arguments,mapClasses);
						if (mTo.arguments.equals(mToDesc)) {
							mapMethods.put(mFrom,mTo);
						} else {
							System.err.println("Arguments do not match between methods, mismatch!");
							return;
						}
					} else {
						System.err.println("Methods moved between classes, mismatch!");
						return;
					}
				}
			}
		}
		int nClasses=0,nFields=0,nMethods=0;
		for (String c : setClasses) {
			String c2=mapClasses.get(c);
			if (!c.equals(c2)) {
				System.out.println("CL: "+c+" "+mapClasses.get(c));
				nClasses++;
			}
		}
		for (Field f : mapFields.keySet()) {
			if (setClasses.contains(f.owner)) {
				Field b=mapFields.get(f);
				if (!f.name.equals(b.name)) {
					System.out.println("FD: "+f.owner+"/"+f.name+" "+b.owner+"/"+b.name);
					nFields++;
				}
			}
		}
		for (Method m : mapMethods.keySet()) {
			if (setClasses.contains(m.owner) && !m.name.equals("<init>")) {
				Method b=mapMethods.get(m);
				if (!m.name.equals(b.name)) {
					System.out.println("MD: "+m.owner+"/"+m.name+" "+m.arguments+" "+b.owner+"/"+b.name+" "+b.arguments);
					nMethods++;
				}
			}
		}
		/*System.err.println("Number of classes: "+nClasses);
		System.err.println("Number of fields: "+nFields);
		System.err.println("Number of methods: "+nMethods);*/
	}

}
