package com.github.softwaresale.clientspec;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.softwaresale.clientspec.model.APISpec;
import com.github.softwaresale.clientspec.model.ServiceSpec;
import com.google.auto.service.AutoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.StandardLocation;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;

/**
 * Finds rest controllers and extracts their service definition
 */
@SupportedAnnotationTypes("org.springframework.web.bind.annotation.RestController")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@AutoService(Processor.class)
public class ControllerProcessor extends AbstractProcessor {

    private static final Logger logger = LoggerFactory.getLogger(ControllerProcessor.class);

    private final ControllerVisitor controllerVisitor;
    private final ObjectMapper objectMapper;

    public ControllerProcessor() {
        this(new ControllerVisitor(), new ObjectMapper());
    }

    public ControllerProcessor(ControllerVisitor controllerVisitor, ObjectMapper objectMapper) {
        this.controllerVisitor = controllerVisitor;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {
        logger.info("running client-spec annotation processor");

        // this is our API specification. We will visit all controllers and entities and place them in here
        APISpec apiSpec = new APISpec("service");

        for (TypeElement annotation : annotations) {
            Set<? extends Element> annotatedElements = roundEnvironment.getElementsAnnotatedWith(annotation);
            for (Element element : annotatedElements) {

                logger.debug("found element {} of kind {}", element.getSimpleName(), element.getKind());

                // pull out our services
                if (this.controllerVisitor.accept(element)) {
                    logger.info("processing class {}", element.getSimpleName());
                    ServiceSpec service = this.controllerVisitor.visitController(element);
                    apiSpec.addService(service);
                }
            }
        }

        // output our API document
        try {
            FileObject resourceFile = processingEnv.getFiler().createResource(StandardLocation.SOURCE_OUTPUT, "", "api-spec.json");
            Writer outputWriter = resourceFile.openWriter();
            objectMapper.writeValue(outputWriter, apiSpec);
            outputWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return true;
    }
}
