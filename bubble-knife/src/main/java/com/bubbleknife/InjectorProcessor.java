package com.bubbleknife;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

@SupportedAnnotationTypes("com.example.bubbleknife.api.InjectThis")
public class InjectorProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        List<String> data = new ArrayList<String>();
        for (Element element : roundEnv.getElementsAnnotatedWith(InjectThis.class)) {
            InjectThis annotation = element.getAnnotation(InjectThis.class);

            data.add(Integer.toString(annotation.id()));
        }

        JavaFileObject file;
        try {
            file = processingEnv.getFiler().createSourceFile("Blerg.java");
            Writer out = file.openWriter();
            out.write("public class Blerg { "
                    + "public void lala () { ");

            for (String i : data) {
                out.write("System.out.println (\"i: \" + " + i + ");");
            }

            out.write("}" +
                    "}");
            out.flush();
            out.close();
        } catch (IOException e) {
            throw new RuntimeException();
        }

        return true;
    }
}