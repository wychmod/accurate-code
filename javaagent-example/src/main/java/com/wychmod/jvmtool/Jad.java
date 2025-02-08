package com.wychmod.jvmtool;

import org.jd.core.v1.ClassFileToJavaSourceDecompiler;
import org.jd.core.v1.api.loader.Loader;
import org.jd.core.v1.api.loader.LoaderException;
import org.jd.core.v1.api.printer.Printer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @description: 自定义类加载器和代码打印器以实现Java源码反编译
 * @author: wychmod
 * @date: 2025-02-09
 */
public class Jad {
    // 自定义类加载器，用于加载类字节码
    static Loader loader = new Loader() {
        /**
         * 加载指定内部名称的类字节码
         * @param internalName 类的内部名称
         * @return 字节码数组，如果无法加载则返回null
         * @throws LoaderException 如果加载过程中发生IO错误
         */
        @Override
        public byte[] load(String internalName) throws LoaderException {
            InputStream is = loadClass(internalName);
            if (is == null) {
                return null;
            } else {
                try (InputStream in = is; ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                    byte[] buffer = new byte[1024];
                    int read = in.read(buffer);

                    while (read > 0) {
                        out.write(buffer, 0, read);
                        read = in.read(buffer);
                    }

                    return out.toByteArray();
                } catch (IOException e) {
                    throw new LoaderException(e);
                }
            }
        }

        /**
         * 检查是否可以加载指定内部名称的类
         * @param internalName 类的内部名称
         * @return 如果可以加载则返回true，否则返回false
         */
        @Override
        public boolean canLoad(String internalName) {
            return loadClass(internalName) != null;
        }

        /**
         * 加载类的字节码流
         * @param internalName 类的内部名称
         * @return 类的InputStream对象，如果找不到则返回null
         */
        private InputStream loadClass(String internalName) {
            InputStream is = this.getClass().getResourceAsStream("/" + internalName.replaceAll("\\.", "/") + ".class");
            if (is == null && Agent.instrumentation != null) {
                for (Class allLoadedClass : Agent.instrumentation.getAllLoadedClasses()) {
                    if (allLoadedClass.getName().equals(internalName)) {
                        is = allLoadedClass.getResourceAsStream("/" + internalName.replaceAll("\\.", "/") + ".class");
                        break;
                    }
                }
            }
            return is;
        }
    };

    // 自定义代码打印器，用于输出反编译的Java源码
    static Printer printer = new Printer() {
        protected static final String TAB = "  ";
        protected static final String NEWLINE = "\n";

        protected int indentationCount = 0;
        protected StringBuilder sb = new StringBuilder();

        @Override
        public String toString() {
            return sb.toString();
        }

        // 以下方法定义了打印Java源码的逻辑，但具体实现留空以简化注释
        @Override public void start(int maxLineNumber, int majorVersion, int minorVersion) {}
        @Override public void end() {}
        @Override public void printText(String text) { sb.append(text); }
        @Override public void printNumericConstant(String constant) { sb.append(constant); }
        @Override public void printStringConstant(String constant, String ownerInternalName) { sb.append(constant); }
        @Override public void printKeyword(String keyword) { sb.append(keyword); }
        @Override public void printDeclaration(int type, String internalTypeName, String name, String descriptor) { sb.append(name); }
        @Override public void printReference(int type, String internalTypeName, String name, String descriptor, String ownerInternalName) { sb.append(name); }
        @Override public void indent() { this.indentationCount++; }
        @Override public void unindent() { this.indentationCount--; }
        @Override public void startLine(int lineNumber) { for (int i = 0; i < indentationCount; i++) sb.append(TAB); }
        @Override public void endLine() { sb.append(NEWLINE); }
        @Override public void extraLine(int count) { while (count-- > 0) sb.append(NEWLINE); }
        @Override public void startMarker(int type) {}
        @Override public void endMarker(int type) {}
    };

    /**
     * 反编译指定类名的Java源码
     * @param className 要反编译的类名
     * @return 反编译后的Java源码字符串
     * @throws Exception 如果反编译过程中发生错误
     */
    public static String decompiler(String className) throws Exception {
        ClassFileToJavaSourceDecompiler decompiler = new ClassFileToJavaSourceDecompiler();
        decompiler.decompile(loader, printer, className);
        String source = printer.toString();
        return source;
    }
}
