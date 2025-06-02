package com.cursee.behind_you.util;

import com.cursee.behind_you.Constants;
import com.google.common.collect.Lists;
import net.minecraft.util.RandomSource;
import net.neoforged.fml.loading.FMLPaths;

import javax.annotation.Nullable;
import java.io.*;
import java.util.*;

public class TextManager {

    public static TextManager instance;

    private final RandomSource RANDOM = RandomSource.create();
    private List<String> texts = Lists.<String>newArrayList();

    public TextManager() {
        texts = this.prepare();
        if (instance == null) instance = this;
    }

    protected List<String> prepare() {

        texts = Lists.<String>newArrayList();

        String configDirPath = FMLPaths.GAMEDIR.get().toString() + File.separator + "config";
        File configDirectory = new File(configDirPath);
        if (!configDirectory.isDirectory()) configDirectory.mkdirs();

        String behindYouFilePath = configDirPath + File.separator + "behind_you.txt";
        File behindYouFile = new File(behindYouFilePath);

        if (!behindYouFile.isFile()) {

            try (InputStream input = TextManager.class.getResourceAsStream("/assets/behind_you/texts/behind_you.txt")) {
                if (input == null) {
                    Constants.LOG.info("Resource not found: /assets/behind_you/texts/behind_you.txt");
                    return texts;
                }

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
                    String s;
                    while ((s = reader.readLine()) != null) {
                        texts.add(s);
                    }
                }
            }
            catch (Exception e) {
                System.out.println(e.getMessage());
                e.fillInStackTrace();
            }

            copyResourceToFile("/assets/behind_you/texts/behind_you.txt", behindYouFile);
        }
        else {
            try (BufferedReader reader = new BufferedReader(new FileReader(behindYouFile))) {
                String s;
                while ((s = reader.readLine()) != null) {
                    texts.add(s);
                }
            }
            catch (Exception e) {
                System.out.println(e.getMessage());
                e.fillInStackTrace();
            }
        }

        return texts;
    }

    @Nullable
    public String getText() {
        int size = this.texts.size();
        int nextInt = RANDOM.nextInt(size);
        return this.texts.get(nextInt);
    }

    public static void copyResourceToFile(String resourcePath, File destinationFile) {
        try (InputStream input = TextManager.class.getResourceAsStream(resourcePath)) {
            if (input == null) {
                Constants.LOG.info("Resource not found: {}", resourcePath);
                return;
            }

            // Ensure the destination directory exists
            destinationFile.getParentFile().mkdirs();

            try (FileOutputStream output = new FileOutputStream(destinationFile)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = input.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                }
                Constants.LOG.info("Copied resource to: {}", destinationFile.getAbsolutePath());
            }
        } catch (IOException e) {
            Constants.LOG.info("Failed to write resource to file: {}", destinationFile.getPath());
            Constants.LOG.info(e.getMessage());
            e.fillInStackTrace();
        }
    }
}
