package com.mindcluster.safediary.iam.infrastructure.authorization.sfs.pipeline;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import java.io.IOException;

/** Writes security errors with the same JSON shape as ErrorResource ({"code","message"}). */
public final class SecurityErrorWriter {
    private SecurityErrorWriter() {}

    public static void write(HttpServletResponse response, int status, String code, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"code\":\"%s\",\"message\":\"%s\"}".formatted(code, message.replace("\"", "'")));
    }
}
