/*
 * Copyright Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the authors tag. All rights reserved.
 *
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License version 2.
 * 
 * This particular file is subject to the "Classpath" exception as provided in the 
 * LICENSE file that accompanied this code.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.eclipse.ceylon.compiler.java.test.fordebug;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Javac {

    private List<String> classPath = new ArrayList<String>();
    private List<String> sourcePath = new ArrayList<String>();
    private List<String> sourceFiles = new ArrayList<String>();
    private boolean verbose;
    private String encoding;
    
    public Javac() {
    }
    
    public Javac verbose(boolean verbose) {
        this.verbose = verbose;
        return this;
    }
    
    public Javac encoding(String encoding) {
        this.encoding = encoding;
        return this;
    }
    
    
    public Javac appendClassPath(String path) {
        classPath.add(path);
        return this;
    }
    

    public void appendClassPath(FileCollector classPath) {
        this.classPath.addAll(classPath.getFiles());
    }
    
    public Javac appendSourcePath(String path) {
        sourcePath.add(path);
        return this;
    }
    
    public Javac sourceFiles(List<String> sourceFiles) {
        this.sourceFiles = new ArrayList<String>(sourceFiles);
        return this;
    }
    
    public Javac addSourceFile(String sourceFile) {
        this.sourceFiles.add(sourceFile);
        return this;
    }
    
    public Javac addSourceFiles(FileCollector sources) {
        this.sourceFiles.addAll(sources.getFiles());
        return this;
    }
    
    
    private ProcessBuilder build() {
        List<String> args = buildArgs();
        ProcessBuilder pb = new ProcessBuilder(args);
        return pb;
    }
    
    public String toString() {
        return buildArgs().toString();
    }

    private List<String> buildArgs() {
        List<String> args = new ArrayList<String>();
        String home = System.getenv("JAVA_HOME");
        if (home == null) {
            home = System.getProperty("java.home");
        }
        if (home != null) {
            File javac = new File(home, "bin/javac");
            if (!javac.exists()) {// maybe we found the JRE home
                javac = new File(home, "../bin/javac");
            }
            args.add(javac.getPath());
        } else {
            args.add("javac");
        }
        if (verbose) {
            args.add("-verbose");
        }
        if (classPath != null) {
            args.add("-classpath");
            args.add(Path.path(this.classPath));
        }
        if (sourcePath != null) {
            args.add("-sourcepath");
            args.add(Path.path(this.sourcePath));
        }
        if (encoding != null) {
            args.add("-encoding");
            args.add(encoding); 
        }
        args.addAll(sourceFiles);
        return args;
    }
    
    public int exec() throws Exception {
        return ProcessRunner.exec(build());
    }
}
