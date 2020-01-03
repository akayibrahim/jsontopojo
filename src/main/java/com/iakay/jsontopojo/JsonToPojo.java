package com.iakay.jsontopojo;

/**
 * Created by iakay on 2019-12-07.
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.sun.codemodel.*;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public final class JsonToPojo {
    
    private static final String DEFAULT_FILE_PATH = "./src/main/java";
    private static final JCodeModel codeModel = new JCodeModel();
    private static final List<String> classList = new ArrayList<String>();

    public static void fromFile(String jsonFile, String classPackage, boolean lombok, String prefix, String postfix, boolean throwForDuplicateObjectName) {

        try {
            Reader reader = new FileReader(jsonFile);
            JsonElement root = new JsonParser().parse(reader);
            generateCode(root, classPackage, lombok, prefix, postfix, throwForDuplicateObjectName);
        } catch (Exception e) {

            throw new IllegalStateException(e);
        }
    }

    public static void fromJson(String json, String classPackage, boolean lombok, String prefix, String postfix, boolean throwForDuplicateObjectName) {
        JsonReader reader = new JsonReader(new StringReader(json));
        JsonElement root = new JsonParser().parse(reader);
        generateCode(root, classPackage, lombok, prefix, postfix, throwForDuplicateObjectName);
    }

    static void generateCode(JsonElement root, String classPackage, boolean lombok, String prefix, String postfix, boolean throwForDuplicateObjectName) {

        int lastIndexDot = classPackage.lastIndexOf(".");
        String packageName = classPackage.substring(0, lastIndexDot);
        String className = classPackage.substring(lastIndexDot + 1, classPackage.length());

        generateClass(packageName, className, root, lombok, prefix, postfix, throwForDuplicateObjectName);

        try {
            codeModel.build(new File(DEFAULT_FILE_PATH));

        } catch (Exception e) {
            throw new IllegalStateException("Couldn't generate Pojo", e);
        }
    }

    private static JClass generateClass(String packageName, String className, JsonElement jsonElement, boolean lombok, String prefix, String postfix, boolean throwForDuplicateObjectName) {

        JClass elementClass = null;

        if (jsonElement.isJsonNull()) {
            elementClass = codeModel.ref(Object.class);


        } else if (jsonElement.isJsonPrimitive()) {

            JsonPrimitive jsonPrimitive = jsonElement.getAsJsonPrimitive();
            elementClass = getClassForPrimitive(jsonPrimitive);

        } else if (jsonElement.isJsonArray()) {

            JsonArray array = jsonElement.getAsJsonArray();
            elementClass = getClassForArray(packageName, className, array, lombok, prefix, postfix, throwForDuplicateObjectName);

        } else if (jsonElement.isJsonObject()) {
            JsonObject jsonObj = jsonElement.getAsJsonObject();
            elementClass = getClassForObject(packageName, className, jsonObj, lombok, prefix, postfix, throwForDuplicateObjectName);
        }

        if (elementClass != null) {
            return elementClass;
        }

        throw new IllegalStateException("jsonElement type not supported");
    }

    private static JClass getClassForObject(String packageName, String className, JsonObject jsonObj, boolean lombok, String prefix, String postfix, boolean throwForDuplicateObjectName) {

        Map<String, JClass> fields = new LinkedHashMap<String, JClass>();

        for (Entry<String, JsonElement> element : jsonObj.entrySet()) {

            String fieldName = element.getKey();
            String fieldUppercase = getFirstUppercase(fieldName);

            JClass elementClass = generateClass(packageName, fieldUppercase, element.getValue(), lombok, prefix, postfix, throwForDuplicateObjectName);
            fields.put(fieldName, elementClass);
        }
        className = prepareClassName(className, prefix, postfix);
        boolean nameChanged = false;
        String nameBeforeAfter = "Original: " + className;
        if (classList.contains(className)) {
            if (throwForDuplicateObjectName) {
                throw new IllegalStateException("There are multiple objects of the same type, please rename these objects and try again. Object name: " + className);
            } else {
                className = className + "_$";
                nameChanged = true;
                nameBeforeAfter += ", Changed to: " + className;
            }
        }
        classList.add(className);
        String classPackage = packageName + "." + className;
        generatePojo(classPackage, fields, lombok, prefix, postfix, nameChanged, nameBeforeAfter);

        JClass jclass = codeModel.ref(classPackage);
        return jclass;
    }

    private static String prepareClassName(String className, String prefix, String postfix) {
        if (!className.endsWith("_$")) {
            if (className.contains("_") || className.chars().allMatch(c -> Character.isUpperCase(c))) {
                className = className.replace("_", " ");
                className = WordUtils.capitalizeFully(className);
                className = className.replace(" ", "");
            }
        }

        if (className.chars().allMatch(c -> Character.isLowerCase(c))) {
            className = WordUtils.capitalizeFully(className);
        }

        if (!StringUtils.isBlank(prefix) && !className.startsWith(prefix)) {
            className = prefix + className;
        }
        if (!StringUtils.isBlank(postfix) && !className.endsWith(postfix)) {
            className = className + postfix;
        }
        return className;
    }

    private static String prepareVariableName(String variable) {
        if (isContainUnderlineOrStartWithUppercase(variable)) {
            variable = variable.replace("_", " ");
            variable = WordUtils.capitalizeFully(variable);
            variable = variable.replace(" ", "");
            variable = StringUtils.uncapitalize(variable);
        }
        return variable;
    }

    private static boolean isContainUnderlineOrStartWithUppercase(String variable) {
        return variable.contains("_") || Character.isUpperCase(variable.charAt(0));
    }

    private static JClass getClassForArray(String packageName, String className, JsonArray array, boolean lombok, String prefix, String postfix, boolean throwForDuplicateObjectName) {

        JClass narrowClass = codeModel.ref(Object.class);
        if (array.size() > 0) {
            String elementName = className;
            if (className.endsWith("ies")) {
                elementName = elementName.substring(0, elementName.length() - 3) + "y";
            } else if (elementName.endsWith("s")) {
                elementName = elementName.substring(0, elementName.length() - 1);
            }

            narrowClass = generateClass(packageName, elementName, array.get(0), lombok, prefix, postfix, throwForDuplicateObjectName);
        }

        String narrowName = narrowClass.name();
        Class<?> boxedClass = null;
        if (narrowName.equals("Integer")) {
            boxedClass = Integer.class;
        } else if (narrowName.equals("Long")) {
            boxedClass = Long.class;
        } else if (narrowName.equals("Double")) {
            boxedClass = Double.class;
        }
        if (boxedClass != null) {
            narrowClass = codeModel.ref(boxedClass);
        }

        JClass listClass = codeModel.ref(List.class).narrow(narrowClass);
        return listClass;
    }

    private static JClass getClassForPrimitive(JsonPrimitive jsonPrimitive) {

        JClass jClass = null;

        if (jsonPrimitive.isNumber()) {
            Number number = jsonPrimitive.getAsNumber();
            double doubleValue = number.doubleValue();

            if (doubleValue != Math.round(doubleValue)) {
                jClass = codeModel.ref("Double");
            } else {
                long longValue = number.longValue();
                if (longValue >= Integer.MAX_VALUE) {
                    jClass = codeModel.ref("Long");
                } else {
                    jClass = codeModel.ref("Integer");
                }
            }
        } else if (jsonPrimitive.isBoolean()) {
            jClass = codeModel.ref("Boolean");
        } else {
            jClass = codeModel.ref(String.class);
        }
        return jClass;
    }

    public static void generatePojo(String className, Map<String, JClass> fields, boolean lombok, String prefix, String postfix, boolean nameChanged, String nameBeforeAfter) {
        try {
            JDefinedClass definedClass = codeModel._class(className);
            if (lombok) {
                definedClass.annotate(Data.class);
            }
            definedClass.javadoc().add(String.format("Auto Generated BY JSON TO POJO \n" +
                    "https://github.com/akayibrahim/jsontopojo" +
                    (nameChanged ? "\n TODO - Class name changed because of there is another class with same name: " +
                            nameBeforeAfter : "")));
            for (Map.Entry<String, JClass> field : fields.entrySet()) {
                createFieldAndAddGetterSetter(definedClass, field.getKey(), field.getValue(), lombok);
            }

        } catch (Exception e) {
            throw new IllegalStateException("Couldn't generate Pojo", e);
        }
    }

    private static void createFieldAndAddGetterSetter(JDefinedClass definedClass, String fieldName, JClass fieldType, boolean lombok) {
        String fieldNameTemp = fieldName;
        boolean isContainUnderlineOrStartUppercase = isContainUnderlineOrStartWithUppercase(fieldName);
        fieldName = prepareVariableName(fieldName);

        String fieldNameWithFirstLetterToUpperCase = getFirstUppercase(fieldName);

        JFieldVar field = definedClass.field(JMod.PRIVATE, fieldType, fieldName);
        if (fieldType == codeModel.ref(Object.class)) {
            field.javadoc().add(String.format("TODO (Auto Generated Code): '%s' variable type is Object. \n" +
                    "Please check it's data in json. If there is a problem in data, fix it and regenerate. \n" +
                    "If there is not problem, just remove this comment. \n" +
                    "For more detail: https://github.com/akayibrahim/jsontopojo", fieldName));
        }
        if (isContainUnderlineOrStartUppercase) {
            field.annotate(JsonProperty.class).param("value", fieldNameTemp);
        }

        if (!lombok) {
            String getterPrefix = "get";
            String fieldTypeName = fieldType.fullName();
            if (fieldTypeName.equals("boolean") || fieldTypeName.equals("java.lang.Boolean")) {
                getterPrefix = "is";
            }
            String getterMethodName = getterPrefix + fieldNameWithFirstLetterToUpperCase;
            JMethod getterMethod = definedClass.method(JMod.PUBLIC, fieldType, getterMethodName);
            JBlock block = getterMethod.body();
            block._return(field);

            String setterMethodName = "set" + fieldNameWithFirstLetterToUpperCase;
            JMethod setterMethod = definedClass.method(JMod.PUBLIC, Void.TYPE, setterMethodName);
            String setterParameter = fieldName;
            setterMethod.param(fieldType, setterParameter);
            setterMethod.body().assign(JExpr._this().ref(fieldName), JExpr.ref(setterParameter));
        }
    }

    private static String getFirstUppercase(String word) {

        String firstLetterToUpperCase = word.substring(0, 1).toUpperCase();
        if (word.length() > 1) {
            firstLetterToUpperCase += word.substring(1, word.length());
        }
        return firstLetterToUpperCase;
    }
}
