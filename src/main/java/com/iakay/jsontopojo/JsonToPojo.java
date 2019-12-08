package com.iakay.jsontopojo;

/**
 * Created by iakay on 2019-12-07.
 */

import com.google.gson.*;
import com.sun.codemodel.*;
import lombok.Data;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public final class JsonToPojo {
    
    private static final String DEFAULT_FILE_PATH = "./src/main/java";
    private static final JCodeModel codeModel = new JCodeModel();

    public static void fromFile(String jsonFile, String classPackage, boolean lombok) {

        try {
            Reader reader = new FileReader(jsonFile);
            JsonElement root = new JsonParser().parse(reader);

            generateCode(root, classPackage, lombok);
        } catch (Exception e) {

            throw new IllegalStateException(e);
        }
    }

    public static void fromJson(String json, String classPackage, boolean lombok) {

        JsonElement root = new JsonParser().parse(json);
        generateCode(root, classPackage, lombok);
    }

    static void generateCode(JsonElement root, String classPackage, boolean lombok) {

        int lastIndexDot = classPackage.lastIndexOf(".");
        String packageName = classPackage.substring(0, lastIndexDot);
        String className = classPackage.substring(lastIndexDot + 1, classPackage.length());

        generateClass(packageName, className, root, lombok);

        try {
            codeModel.build(new File(DEFAULT_FILE_PATH));

        } catch (Exception e) {
            throw new IllegalStateException("Couldn't generate Pojo", e);
        }
    }

    private static JClass generateClass(String packageName, String className, JsonElement jsonElement, boolean lombok) {

        JClass elementClass = null;

        if (jsonElement.isJsonNull()) {
            elementClass = codeModel.ref(Object.class);

        } else if (jsonElement.isJsonPrimitive()) {

            JsonPrimitive jsonPrimitive = jsonElement.getAsJsonPrimitive();
            elementClass = getClassForPrimitive(jsonPrimitive);

        } else if (jsonElement.isJsonArray()) {

            JsonArray array = jsonElement.getAsJsonArray();
            elementClass = getClassForArray(packageName, className, array, lombok);

        } else if (jsonElement.isJsonObject()) {

            JsonObject jsonObj = jsonElement.getAsJsonObject();
            elementClass = getClassForObject(packageName, className, jsonObj, lombok);
        }

        if (elementClass != null) {
            return elementClass;
        }

        throw new IllegalStateException("jsonElement type not supported");
    }

    private static JClass getClassForObject(String packageName, String className, JsonObject jsonObj, boolean lombok) {

        Map<String, JClass> fields = new LinkedHashMap<String, JClass>();

        for (Entry<String, JsonElement> element : jsonObj.entrySet()) {

            String fieldName = element.getKey();
            String fieldUppercase = getFirstUppercase(fieldName);

            JClass elementClass = generateClass(packageName, fieldUppercase, element.getValue(), lombok);
            fields.put(fieldName, elementClass);
        }

        String classPackage = packageName + "." + className;
        generatePojo(classPackage, fields, lombok);

        JClass jclass = codeModel.ref(classPackage);
        return jclass;
    }

    private static JClass getClassForArray(String packageName, String className, JsonArray array, boolean lombok) {

        JClass narrowClass = codeModel.ref(Object.class);
        if (array.size() > 0) {
            String elementName = className;
            if (className.endsWith("ies")) {
                elementName = elementName.substring(0, elementName.length() - 3) + "y";
            } else if (elementName.endsWith("s")) {
                elementName = elementName.substring(0, elementName.length() - 1);
            }

            narrowClass = generateClass(packageName, elementName, array.get(0), lombok);
        }

        String narrowName = narrowClass.name();
        Class<?> boxedClass = null;
        if (narrowName.equals("int")) {
            boxedClass = Integer.class;
        } else if (narrowName.equals("long")) {
            boxedClass = Long.class;
        } else if (narrowName.equals("double")) {
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

    public static void generatePojo(String className, Map<String, JClass> fields, boolean lombok) {

        try {
            JDefinedClass definedClass = codeModel._class(className);
            if (lombok) {
                definedClass.annotate(Data.class);
            }
            for (Map.Entry<String, JClass> field : fields.entrySet()) {
                createFieldAndAddGetterSetter(definedClass, field.getKey(), field.getValue(), lombok);
            }

        } catch (Exception e) {
            throw new IllegalStateException("Couldn't generate Pojo", e);
        }
    }

    private static void createFieldAndAddGetterSetter(JDefinedClass definedClass, String fieldName, JClass fieldType, boolean lombok) {

        String fieldNameWithFirstLetterToUpperCase = getFirstUppercase(fieldName);

        JFieldVar field = definedClass.field(JMod.PRIVATE, fieldType, fieldName);

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
