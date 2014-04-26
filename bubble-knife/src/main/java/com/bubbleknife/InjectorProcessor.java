package com.bubbleknife;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import static javax.lang.model.element.ElementKind.CLASS;

@SupportedAnnotationTypes("com.bubbleknife.InjectThis")
public class InjectorProcessor extends AbstractProcessor {
    static final String SUFFIX = "$$ViewInjector";

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Map<TypeElement, Set<InjectionPoint>> injectionsByClass =
                new LinkedHashMap<TypeElement, Set<InjectionPoint>>();
        Set<TypeMirror> injectionTargets = new HashSet<TypeMirror>();

        List<String> data = new ArrayList<String>();
        for (Element element : roundEnv.getElementsAnnotatedWith(InjectThis.class)) {
            InjectThis annotation = element.getAnnotation(InjectThis.class);


            // Get the enclosing class for the annotation
            TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
            if (enclosingElement.getKind() != CLASS) {
                error("Unexpected @InjectView on field in " + element);
                continue;
            }

            String message = "annotation found in " + annotation.id()
                    + " called " + element.getSimpleName() + " kind " + element.getKind() + " on element " + enclosingElement.getQualifiedName();
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, message);


            // Verify field properties
            Set<Modifier> modifiers = element.getModifiers();
            if (modifiers.contains(Modifier.PRIVATE) || modifiers.contains(Modifier.PROTECTED) || modifiers.contains(Modifier.STATIC)) {
                error("@InjectView fields must not be private, protected , or static: " +
                        enclosingElement.getQualifiedName()
                        + "." + element);
                continue;
            }

            // Create all injection points
            Set<InjectionPoint> injections = injectionsByClass.get(enclosingElement);
            if (injections == null) {
                injections = new HashSet<InjectionPoint>();
                injectionsByClass.put(enclosingElement, injections);
            }

            // Assemble information on the injectino piont
            String variableName = element.getSimpleName().toString();
            String type = element.asType().toString();
            int value = annotation.id();
            injections.add(new InjectionPoint(variableName, type, value));

            // Add to the valid injection targets set.
            injectionTargets.add(enclosingElement.asType());

            data.add(Integer.toString(annotation.id()));
        }

        // Now, generate an injection call for each class
        for (Map.Entry<TypeElement, Set<InjectionPoint>> injection : injectionsByClass.entrySet()) {
            TypeElement type = injection.getKey();
            String targetClass = type.getQualifiedName().toString();

            int lastDot = targetClass.lastIndexOf('.');
            String activityType = targetClass.substring(lastDot + 1);
            String className = activityType + SUFFIX;
            String packageName = targetClass.substring(0, lastDot);
            String parentClass = resolveParentType(type, injectionTargets);
            StringBuilder injections = new StringBuilder();
            if (parentClass != null) {
                injections.append("    ")
                        .append(parentClass)
                        .append(SUFFIX)
                        .append(".inject(activity);\n\n");
            }

            for (InjectionPoint injectionPoint : injection.getValue()) {
                injections.append(injectionPoint).append("\n");
            }

            // Write the view injector class
            try {
                JavaFileObject jfo = processingEnv.getFiler().createSourceFile(packageName + "." + className, type);
                Writer writer = jfo.openWriter();

                writer.write(String.format(INJECTOR, packageName, className, activityType, injections));
                writer.flush();
                writer.close();
            } catch (IOException e) {
                error(e.getMessage());
            }
        }

        return true;
    }

    public String resolveParentType(TypeElement typeElement, Set<TypeMirror> parents) {
        TypeMirror type;
        while (true) {
            type = typeElement.getSuperclass();
            if (type.getKind() == TypeKind.NONE) {
                return null;
            }

            if (parents.contains(type)) {
                return type.toString();
            }

            typeElement = (TypeElement) ((DeclaredType) type).asElement();
        }
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    private void error(String message) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, String.format(message, message));
    }

    private static class InjectionPoint {
        private final String variableName;
        private final String type;
        private final int value;

        InjectionPoint(String varName, String type, int val) {
            this.variableName = varName;
            this.type = type;
            this.value = val;
        }

        @Override
        public String toString() {
            return String.format(INJECTION, variableName, type, value);
        }
    }

    /**
     * The injected code
     */
    private static final String INJECTION = "    activity.%s = (%s) activity.findViewById(%s);";

    private static final String INJECTOR = ""
            + "package %s;\n\n"
            + "public class %s {"
            + "  public static void inject(%s activity) {\n"
            + "%s"
            + "  }\n"
            + "}\n";


}