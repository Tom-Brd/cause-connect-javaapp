package org.pat.causeconnect;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Component
public class UpdateDownloader {
    private final String baseUrl = "https://api.causeconnect.fr";

    private String currentVersion = "";

    private String hasNewVersion() {
//        RestTemplate restTemplate = new RestTemplate();
//        String versionUrl = baseUrl + "/version";
        currentVersion = CauseconnectApplication.class.getPackage().getImplementationVersion();
//        String latestVersion = restTemplate.getForObject(versionUrl, String.class);
        String latestVersion = "1.0.2";

        if (latestVersion != null && !latestVersion.equals(currentVersion)) {
            return latestVersion;
        }
        return null;
    }

    public void downloadNewVersion() {
        String version = hasNewVersion();
        if (version != null) {
            System.out.println("Downloading new version: " + version);
        } else {
            System.out.println("No new version available");
            return;
        }

        String downloadUrl = baseUrl + "/java-app/" + version;
        RestTemplate restTemplate = new RestTemplate();

        try {
            System.out.println("Downloading new version from: " + downloadUrl);
            Resource resource = restTemplate.getForObject(downloadUrl, Resource.class);
            if (resource != null && resource.isReadable()) {
                System.out.println("New version: " + version);
                Path tempPath = Files.createTempFile("causeconnect", ".jar");
                resource.getInputStream().transferTo(Files.newOutputStream(tempPath));
                replaceOldJar(tempPath);
            } else {
                System.out.println("Error downloading new version: Resource is not readable");
            }
        } catch (Exception e) {
            System.out.println(e.getClass());
            System.out.println("Error downloading new version: " + e.getMessage());
        }
    }

    private void replaceOldJar(Path newJarPath) throws IOException {
        Path oldJarPath = getRunningJarPath();

        Files.move(newJarPath, oldJarPath);
        launchNewVersion(oldJarPath);
    }

    private Path getRunningJarPath() {
        return Paths.get("/Applications/CauseConnect.app/Contents/app/", "causeconnect.jar");
    }

    private void launchNewVersion(Path newJarPath) {
        String javaBin = System.getenv("JAVA_HOME") != null ? System.getenv("JAVA_HOME") + "/bin/java" : "java";
        ProcessBuilder processBuilder = new ProcessBuilder(javaBin, "-jar", newJarPath.toString());
        processBuilder.inheritIO();
        try {
            Process process = processBuilder.start();
            process.waitFor();
        } catch (Exception e) {
            System.out.println("Error launching new version: " + e.getMessage());
        }
        System.exit(0);
    }
}