package com.github.dapeng.impl.plugins.deploy_agent;

import com.github.dapeng.api.Container;
import com.github.dapeng.api.Plugin;
import com.github.dapeng.impl.container.DapengContainer;
import org.apache.ivy.Ivy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class DeployAgentPlugin implements Plugin{

    private static final Logger LOGGER = LoggerFactory.getLogger(DeployAgentPlugin.class);

//    private final Container container;
//    private final List<ClassLoader> appClassLoaders;

    private static final String JAR_POSTFIX = ".*\\.jar";

    private boolean useivy = true; //fixme 先本地写死
    private String organisation = "com.github.dapeng"; //fixme 先本地写死
    private String serviceVersion = "0.1-SNAPSHOT";   //fixme 先本地写死 (服务版本号)
    private String services = "stable-test_service_2.12"; //fixme 先本地写死     (服务名)
    private String ivysettings; //fixme 先本地写死 (为ivysettings.xml 的文件路径)
    private String targetDir = "E:\\tmp\\";
    private String cache;
    private String local;

    protected String servicesdir;
    protected String thirddir;
    protected int port = -1;
    protected int restport = -1;

    //TODO 目前jmx 暂时没用
    private static String action = "start";
    private static String jmxHost = "127.0.0.1";
    private static int jmxPort = 8060;
    private static String jmxObject = "com.vip.hermes.core.reporter:type=Slf4jReporter";

//    public DeployAgentPlugin(DapengContainer container, List<ClassLoader> appsCls) {
//        this.container = container;
//        this.appClassLoaders = appsCls;
//    }

    @Override
    public void start() {
        Set<URI> serviceUri;
        try {
            serviceUri = loadLibrary();

            Iterator<URI> iterator = serviceUri.iterator();
            while (iterator.hasNext()) {
                URI uri = iterator.next();
                System.out.println("path: " + uri.getPath());
                System.out.println("fileName: " + uri.getPath().substring(uri.getPath().lastIndexOf("/") + 1));
                String filepath = uri.getPath().replaceAll("\\/","\\\\\\\\").substring(2);
                writeFile(filepath,uri.getPath().substring(uri.getPath().lastIndexOf("/") + 1),targetDir );
            }
        } catch (Exception e) {
            LOGGER.error(" failed to load service url........." + e.getMessage());
        }
    }

    @Override
    public void stop() {

    }

    /**
     * 将文件写入本地
     *
     * @param fromUrl  httpPath
     * @param targetFileName targetDirFile
     * @param targetDir targetDir
     */
    private void writeFile(String fromUrl, String targetFileName, String targetDir)  {
        BufferedInputStream inputStream = null;
        BufferedOutputStream outputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(fromUrl));
            outputStream = new BufferedOutputStream(new FileOutputStream(new File(targetDir + "/" + targetFileName)));

            while (inputStream.available() > 0) {
                outputStream.write(inputStream.read());
            }
            //缓冲区的内容写入到文件
            outputStream.flush();

        } catch (MalformedURLException e) {
            LOGGER.error(" failed to get url from: " + fromUrl);
        } catch (IOException e) {
            LOGGER.error(" failed to open url: " +  fromUrl);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (Exception e) {
                LOGGER.error(" failed to close stream......." + e.getMessage());
            }
        }

    }

    /**
     * 初始化OSP三种Classloader的ClassPath
     */
    private Set<URI> loadLibrary() throws Exception {
        Set<URI> plateformUrls = new HashSet<URI>();
        Set<URI> containerUrls = new HashSet<URI>();
        Set<URI> serviceUrls = new HashSet<URI>();

        if (useivy) {
            IvyGetJarUtil ivyGetJarUtil = new IvyGetJarUtil(getIvy());
            plateformUrls.addAll(ivyGetJarUtil.getJars(organisation, services, serviceVersion,
                    new String[] { "master(*)", "compile(*)", "runtime(*)", "provided(*)" }));
//			containerUrls.addAll(ivyGetJarUtil.getJars(organisation, "osp-container", ospVersion,
//					new String[] { "master(*)", "compile(*)", "runtime(*)" }));

            if (services != null) {
                String[] temp = services.split(":");
                serviceUrls.addAll(
                        ivyGetJarUtil.getJars(organisation, temp[0], serviceVersion, new String[] { "master(*)", "compile(*)" }));
            }
        } else {
            String enginePath = URLDecoder.decode(
                    new File(OspEngine.class.getProtectionDomain().getCodeSource().getLocation().getFile()).getParent(),
                    "UTF-8");

            File platformlib = new File(enginePath + "/platformlib");
            if (platformlib.exists()) {
                plateformUrls.addAll(listFilesUrls(platformlib, JAR_POSTFIX));
            } else {
                LOGGER.error("platformlib folder is not exists");
            }

            File containerlib = new File(enginePath + "/containerlib");
            if (containerlib.exists()) {
                containerUrls.addAll(listFilesUrls(containerlib, JAR_POSTFIX));
            } else {
                LOGGER.error("containerlib folder is not exists");
            }

            // 检查servicesdir非空时必须是存在的目录列表
            if (servicesdir != null) {
                for (String temp : servicesdir.split(";")) {
                    File file = new File(temp);
                    if (!file.exists()) {
                        LOGGER.error("the servicesdir is not exists, it's " + temp);
                    }
                    if (!file.isDirectory()) {
                        LOGGER.error("the servicesdir is not directory, it's " + temp);
                    }
                }
            } else {
                servicesdir = enginePath + "/servicesdir";
            }

            String[] servicedirs = servicesdir.split(";");
            for (String dir : servicedirs) {
                File dirFile = new File(dir);
                serviceUrls.addAll(listFilesUrls(dirFile, JAR_POSTFIX));

                // 额外将此目录加为ClassPath, 因为使用Spring Loaded时，此目录直接放.class文件
                serviceUrls.add(dirFile.toURI());
            }

            // thirddir非空时必须是存在的目录或文件列表
            if (thirddir != null) {
                for (String temp : thirddir.split(";")) {
                    File file = new File(temp);
                    if (!file.exists()) {
                        LOGGER.error("the thirddir is not exists, it's " + temp);
                    }
                }
            } else {
                thirddir = enginePath + "/thirddir";
            }

            String[] thirddirs = thirddir.split(";");
            for (String dir : thirddirs) {
                serviceUrls.addAll(listFilesUrls(new File(dir), JAR_POSTFIX));
            }
        }

        return serviceUrls;
    }


    /**
     * list files in dir or file, filter by nameRegex.
     */
    private static Set<URI> listFilesUrls(File file, String nameRegex) throws MalformedURLException {
        Set<URI> urlSet = new HashSet<URI>();

        if (file == null || !file.exists()) {
            return urlSet;
        }

        if (file.isFile() && file.getName().matches(nameRegex)) {
            urlSet.add(file.toURI());
        } else if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    urlSet.addAll(listFilesUrls(files[i], nameRegex));
                }
            }
        }
        return urlSet;
    }

    private Ivy getIvy() throws ParseException, IOException {
        Ivy ivy = Ivy.newInstance();

        if (cache == null) {
            cache = System.getProperty("user.home") + "/.ivy_cache";
        }
        File cacheFile = new File(cache);
        if (!cacheFile.exists()) {
            cacheFile.mkdirs();
        }
        ivy.getSettings().setDefaultCache(cacheFile);

        if (local == null) {
            local = System.getProperty("user.home") + "/.m2/repository";
        }
        File localFile = new File(local);
        if (!localFile.exists()) {
            localFile.mkdirs();
        }
        ivy.getSettings().setVariable("ivy.local.default.root", local);

        URL ivysettingsURL = null;
        if (ivysettings == null) {
            ivysettingsURL = OspEngine.class.getClassLoader().getResource("ivysettings.xml");
        } else {
            ivysettingsURL = new File(ivysettings).toURI().toURL();
        }
        ivy.getSettings().load(ivysettingsURL);

        return ivy;
    }
}