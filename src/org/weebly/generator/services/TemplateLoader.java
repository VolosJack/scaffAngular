package org.weebly.generator.services;

import org.weebly.generator.model.Component;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

/**
 * Service to load file templates.
 */
public class TemplateLoader {

    private String templatePath = "/templates/";
    private String[] componentTypes = {"Controller", "Directive", "Service", "Factory", "Filter"};

    private HashMap<String, Component> Components = new HashMap<>();

    private boolean isLoaded = false;

    /**
     * Lazy loads and gets the map of initialized Components
     *
     * @return map
     */
    public HashMap<String, Component> getComponents() {
        if(!isLoaded) {
            initComponent();
        }
        return Components;
    }

    public final void initComponent() {
        try {
            for (String obj : componentTypes) {
                Component tmp = new Component();
                tmp.code = FileHandler.readFile(templatePath + obj + ".js");
                tmp.spec = FileHandler.readFile(templatePath + obj + "Spec.js");

                Components.put(obj, tmp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        isLoaded = true;
    }

    /**
     * Gets the file name with the required type based on if its a controller or a directive or a service
     *
     * @param fileName the filename
     * @param type     the type of the file
     * @return the file name with file type
     */
    public String getFilenameWithSuffix(String fileName, String type) {
        if (type.equals("Controller")) {
            return fileName + "Ctrl";
        } else {
            return fileName + type;
        }
    }

    public String getSrcFilename(String baseName, String fileType) {
        return getFilenameWithSuffix(baseName, fileType) + ".js";
    }

    public String getTestFilename(String baseName, String fileType) {
        return getFilenameWithSuffix(baseName, fileType) + "Spec.js";
    }

    public String formatContent(String content, String componentName, String moduleName) {
        return content.replaceAll("#COMPONENTNAME#", componentName).replaceAll("#MODULENAME#", moduleName);
    }

    public String[] getComponentTypes() {return componentTypes;}
}
