package net.lr.tinyhttp.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import net.lr.tinyhttp.HTTPMethod;
import net.lr.tinyhttp.HTTPStatus;
import net.lr.tinyhttp.Handler;
import net.lr.tinyhttp.HttpRequest;
import net.lr.tinyhttp.HttpResponse;

@ObjectClassDefinition(name = "Tiny HTTP File Handler Configuration")
@interface FileHandlerConfig {
    String alias() default "";
    String fileBase() default "web";
}

@Designate(ocd = FileHandlerConfig.class)
@Component(name = "tinyhttp.handler.file")
public class FileHandler implements Handler {
    private File base;
    private MimeTypes mimeTypes;

    public FileHandler() {
        this.mimeTypes = new MimeTypes();
    }

    public FileHandler(File base) {
        this();
        this.base = base;
    }

    @Activate
    public void activate(FileHandlerConfig config) {
        this.base = new File(config.fileBase());
    }

    @Override
    public void process(String alias, HttpRequest request, HttpResponse response) throws IOException {
        HTTPMethod method = request.getMethod();
        if (method != HTTPMethod.GET) {
            response.methodNotAllowed();
            return;
        }
        String path = request.getPath();
        String relPath = path.substring(alias.length());
        File file = new File(base, relPath);
        if (!file.getCanonicalPath().startsWith(base.getCanonicalPath())) {
            response.forbidden();
            return;
        }
        if (!file.exists()) {
            response.notFound();
            return;
        }
        if (file.isDirectory()) {
            File indexFile = new File(file, "index.html"); 
            if (indexFile.exists()) {
                file = indexFile;
            } else {
                response.forbidden();
                return;
            }
        }
        response.writeStatus(HTTPStatus.OK, "OK");
        
        if (file.isFile()) {
            String mimeType = mimeTypes.get(file);
            response.writeHeader("Content-Type", mimeType);
            response.endHeaders();
            Files.copy(file.toPath(), response.getOutputStream());
        }
    }

}
