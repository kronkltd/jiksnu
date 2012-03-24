/*
 * Tigase Jabber/XMPP Utils
 * Copyright (C) 2004-2007 "Artur Hefczyc" <artur.hefczyc@tigase.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://www.gnu.org/licenses/.
 *
 * $Rev: 630 $
 * Last modified by $Author: kobit $
 * $Date: 2011-05-19 12:12:30 -0400 (Thu, 19 May 2011) $
 */

package tigase.util;

import java.io.File;
import java.io.IOException;

import java.lang.reflect.Modifier;

import java.util.Date;
import java.util.Enumeration;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


/**
 * <code>ClassUtil</code> file contains code used for loading all
 * implementations of specified <em>interface</em> or <em>abstract class</em>
 * found in classpath. As a result of calling some functions you can have
 * <code>Set</code> containing all required classes.
 * 
 * <p>
 * Created: Wed Oct 6 08:25:52 2004
 * </p>
 * 
 * @author <a href="mailto:artur.hefczyc@tigase.org">Artur Hefczyc</a>
 * @version $Rev: 630 $
 */
public class ClassUtil {
    private static final String[] SKIP_CONTAINS = { ".ui.", ".swing", ".awt", ".sql.",
                                                    ".xml.", ".terracotta." };
    private static final String[] SKIP_STARTS = { 

        "aleph",
        "arq",

        "biz",

        "ciste",
        "clojure",
        "clj_factory",
        "clj_http",
        "clj_gravatar",
        "clj_stacktrace",
        "clj_tigase",
        "closure",
        "clout",
        "com.cliqset",
        "com.ctc",
        "com.drew",
        "com.esotericsoftware",
        "com.gargoylesoftware",
        "com.google",
        "com.hp",
        "com.ibm",
        "com.jcraft",
        "com.kenai",
        "com.martiansoftware",
        "com.mongodb",
        "com.mysql",
        "com.newrelic",
        "com.ocpsoft",
        "com.opera",
        "com.petebevin",
        "com.rabbitmq",
        "com.steadystate",
        "com.sun",
        "com.thoughtworks",
        "com.tonicsystems",
        "compojure",
        "cucumber",
        "cuke4duke",
        "cybervillains",

        "difflib",

        "gherkin",
        "gloss",
        "gnu",
        "groovy",

        "hiccup",

        "inflections",
        "info",

        "java_cup",
        "javax",
        "jay",
        "JDOMAbout",
        "jena",
        "jline",
        "jiksnu.abdera",
        "jiksnu.actions",
        "jiksnu.helpers",
        "jiksnu.model",
        "jiksnu.sections",
        "jiksnu.templates",
        "jiksnu.triggers",
        "jiksnu.views",
        "jnr",
        "junit",

        "karras",

        "lazytest",
        "lamina",
        "leiningen",

        "marginalia",
        "mx4j",

        "net.cgrand",
        "net.jcip",
        "net.rootdev",
        "net.sf",
        "net.sourceforge",
        "net.xeoh",
        "nl.bitwalker",
        "nu",

        "org.aopalliance",
        "org.apache",
        "org.bouncycastle",
        "org.bson",
        "org.ccil",
        "org.clojure",
        "org.codehaus",
        "org.cyberneko",
        "org.deri",
        "org.dom4j",
        "org.easymock",
        "org.eclipse",
        "org.gjt",
        "org.hamcrest",
        "org.htmlparser",
        "org.ini4j",
        "org.jaxen",
        "org.jboss",
        "org.jcodings",
        "org.jdom",
        "org.joda",
        "org.joni",
        "org.json",
        "org.jruby",
        "org.junit",
        "org.kohsuke",
        "org.mockito",
        "org.mortbay",
        "org.mozilla",
        "org.netbeans",
        "org.objectweb",
        "org.objenesis",
        "org.openqa",
        "org.openjena",
        "org.openrdf",
        "org.openxmlformats",
        "org.openxrd",
        "org.picocontainer",
        "org.postgresql",
        "org.python",
        "org.relaxng",
        "org.slf4j",
        "org.w3c",
        "org.yaml",
        "org.yecht",

        "plaza",
        "potemkin",

        "slingshot",
        "sun",

        "rdfa",
        "redis",
        "repackage",
        "ring",
        "riotcmd",

        "saturnine",
        "schemaorg_apache_xmlbeans",
        "schemasMicrosoft",
        "schemasMicrosoftComVml",
        "swank",

        "tigase.pubsub.Utils",

        "xml_picker_seq",

        "YechtService",
};

    /**
     * Method description
     * 
     * 
     * @param fileName
     * 
     * @return
     */
    public static String getClassNameFromFileName(String fileName) {
        String class_name = null;

        if (fileName.endsWith(".class")) {

            // class_name = fileName.substring(0,
            // fileName.length()-6).replace(File.separatorChar, '.');
            // Above code does not works on MS Windows if we load
            // files from jar file. Jar manipulation code always returns
            // file names with unix style separators
            String tmp_class_name =
                fileName.substring(0, fileName.length() - 6).replace('\\', '.');

            class_name = tmp_class_name.replace('/', '.');
        } // end of if (entry_name.endsWith(".class"))

        return class_name;
    }

    /**
     * Method description
     * 
     * 
     * @param dir
     * 
     * @return
     */
    public static Set<String> getClassNamesFromDir(File dir) {
        Set<String> tmp_set = getFileListDeep(dir);
        Set<String> result = new TreeSet<String>();

        for (String elem : tmp_set) {
            String class_name = getClassNameFromFileName(elem);

            if (class_name != null) {
                result.add(class_name);

                // System.out.println("class name: "+class_name);
            } // end of if (class_name != null)
        } // end of for ()

        return result;
    }

    /**
     * Method description
     * 
     * 
     * @param jarFile
     * 
     * @return
     * 
     * @throws IOException
     */
    public static Set<String> getClassNamesFromJar(File jarFile) throws IOException {
        Set<String> result = new TreeSet<String>();
        JarFile jar = new JarFile(jarFile);
        Enumeration<JarEntry> jar_entries = jar.entries();

        while (jar_entries.hasMoreElements()) {
            JarEntry jar_entry = jar_entries.nextElement();
            String class_name = getClassNameFromFileName(jar_entry.getName());

            if (class_name != null) {
                result.add(class_name);

                // System.out.println("class name: "+class_name);
            } // end of if (entry_name.endsWith(".class"))
        } // end of while (jar_entries.hasMoreElements())

        return result;
    }

    /**
     * Method description
     * 
     * 
     * @return
     * 
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public static Set<Class> getClassesFromClassPath() throws IOException,
                                                              ClassNotFoundException {
        Set<Class> classes_set = new TreeSet<Class>(new ClassComparator());
        String classpath = System.getProperty("java.class.path");

        // System.out.println("classpath: "+classpath);
        StringTokenizer stok = new StringTokenizer(classpath, File.pathSeparator, false);

        while (stok.hasMoreTokens()) {
            String path = stok.nextToken();
            File file = new File(path);

            if (file.exists()) {
                if (file.isDirectory()) {

                    // System.out.println("directory: "+path);
                    Set<String> class_names = getClassNamesFromDir(file);

                    classes_set.addAll(getClassesFromNames(class_names));
                } // end of if (file.isDirectory())

                if (file.isFile()) {

                    // System.out.println("jar file: "+path);
                    Set<String> class_names = getClassNamesFromJar(file);

                    classes_set.addAll(getClassesFromNames(class_names));

                    // System.out.println("Loaded jar file: "+path);
                } // end of if (file.isFile())
            } // end of if (file.exists())
        } // end of while (stok.hasMoreTokens())

        return classes_set;
    }

    /**
     * Method description
     * 
     * 
     * @param names
     * 
     * @return
     * 
     * @throws ClassNotFoundException
     */
    public static Set<Class> getClassesFromNames(Set<String> names)
        throws ClassNotFoundException {
        Set<Class> classes = new TreeSet<Class>(new ClassComparator());

        for (String name : names) {
            try {
                boolean skip_class = false;

                for (String test_str : SKIP_CONTAINS) {
                    skip_class = name.contains(test_str);

                    if (skip_class) {
                        break;
                    }
                }

                if (!skip_class) {
                    for (String test_str : SKIP_STARTS) {
                        skip_class = name.startsWith(test_str);

                        if (skip_class) {
                            break;
                        }
                    }
                }

                if (!skip_class) {

                    // System.out.println(new Date() + " - Class name: " + name);
                    Class cls = Class.forName(name, false, ClassLoader.getSystemClassLoader());

                    classes.add(cls);
                }
            } catch (NoClassDefFoundError e) {
            } catch (UnsatisfiedLinkError e) {
            } catch (Throwable e) {
                Throwable cause = e.getCause();

                // System.out.println("Class name: " + name);
                e.printStackTrace();

                if (cause != null) {
                    cause.printStackTrace();
                }
            }
        } // end of for ()

        return classes;
    }

    /**
     * Method description
     * 
     * 
     * @param classes
     * @param cls
     * @param <T>
     * 
     * @return
     */
    @SuppressWarnings({ "unchecked" })
	public static <T extends Class> Set<T>
                                 getClassesImplementing(Set<Class> classes, T cls) {
        Set<T> classes_set = new TreeSet<T>(new ClassComparator());

        for (Class c : classes) {

            // System.out.println("Checking class: " + c.getName());
            if (cls.isAssignableFrom(c)) {
                int mod = c.getModifiers();

                if (!Modifier.isAbstract(mod) && !Modifier.isInterface(mod)) {
                    classes_set.add((T) c);
                } // end of if (!Modifier.isAbstract(mod) && !Modifier.isInterface(mod))
            } // end of if (cls.isAssignableFrom(c))
        } // end of for ()

        return classes_set;
    }

    /**
     * Method description
     * 
     * 
     * @param cls
     * @param <T>
     * 
     * @return
     * 
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public static <T extends Class> Set<T> getClassesImplementing(T cls)
        throws IOException, ClassNotFoundException {
        return getClassesImplementing(getClassesFromClassPath(), cls);
    }

    /**
     * Method description
     * 
     * 
     * @param path
     * 
     * @return
     */
    public static Set<String> getFileListDeep(File path) {
        Set<String> set = new TreeSet<String>();

        if (path.isDirectory()) {
            String[] files = path.list();

            for (String file : files) {
                walkInDirForFiles(path, file, set);
            } // end of for ()
        } else {
            set.add(path.toString());
        } // end of if (file.isDirectory()) else

        return set;
    }

    /**
     * Method description
     * 
     * 
     * @param obj
     * @param <T>
     * 
     * @return
     * 
     * @throws ClassNotFoundException
     * @throws IOException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    @SuppressWarnings("unchecked")
	public static <T> Set<T> getImplementations(Class<T> obj) throws IOException,
                                                                         ClassNotFoundException, InstantiationException, IllegalAccessException {
        Set<T> result = new TreeSet<T>(new ObjectComparator());

        for (Class cls : getClassesImplementing(obj)) {
            result.add((T) cls.newInstance());
        } // end of for ()

        return result;
    }

    /**
     * Method description
     * 
     * 
     * @param base_dir
     * @param path
     * @param set
     */
    public static void walkInDirForFiles(File base_dir, String path, Set<String> set) {
        File tmp_file = new File(base_dir, path);

        if (tmp_file.isDirectory()) {
            String[] files = tmp_file.list();

            for (String file : files) {
                walkInDirForFiles(base_dir, new File(path, file).toString(), set);
            } // end of for ()
        } else {

            // System.out.println("File: " + path.toString());
            set.add(path);
        } // end of if (file.isDirectory()) else
    }
} // ClassUtil
