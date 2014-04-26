package com.bubbleknife;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

import static javax.lang.model.element.ElementKind.CLASS;

@SupportedAnnotationTypes("com.bubbleknife.InjectThis")
public class InjectorProcessor extends AbstractProcessor {
    static int i = 0;
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        List<String> data = new ArrayList<String>();
        for (Element element : roundEnv.getElementsAnnotatedWith(InjectThis.class)) {
            InjectThis annotation = element.getAnnotation(InjectThis.class);

            // Get the enclosing class for the annotation
            TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
            if (enclosingElement.getKind() != CLASS) {
                error ("Unexpected @InjectView on field in " + element);
                continue;
            }

            String message = "annotation found in " + annotation.id()
                    + " called " + element.getSimpleName() + " kind " + element.getKind() + " on element " + enclosingElement.getQualifiedName();
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, message);




            data.add(Integer.toString(annotation.id()));
        }

//        JavaFileObject file;
//        try {
//            file = processingEnv.getFiler().createSourceFile("Blerg2.java");
//            Writer out = file.openWriter();
//            out.write("public class Blerg { "
//                    + "public void lala () { ");
//
//            for (String i : data) {
//                out.write("System.out.println (\"i: \" + " + i + ");");
//            }
//
//            out.write("}" +
//                    "}");
//            out.flush();
//            out.close();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }

        return true;
    }

    private void error (String message) {

    }
}