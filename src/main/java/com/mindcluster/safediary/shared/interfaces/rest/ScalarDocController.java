package com.mindcluster.safediary.shared.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller serving the Scalar API Reference interactive documentation.
 * Accessible at /scalar and /docs alongside Swagger UI.
 */
@Controller
public class ScalarDocController {

    @Value("${springdoc.api-docs.path:/v3/api-docs}")
    private String openApiDocsPath;

    @Value("${spring.application.name:SafeDiary Platform API}")
    private String applicationTitle;

    @GetMapping(value = {"/scalar", "/docs"}, produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    @Operation(hidden = true)
    public String scalarApiReference() {
        return """
                <!doctype html>
                <html>
                  <head>
                    <title>%s - Scalar API Reference</title>
                    <meta charset="utf-8" />
                    <meta name="viewport" content="width=device-width, initial-scale=1" />
                    <link rel="icon" type="image/svg+xml" href="https://scalar.com/favicon.svg" />
                    <style>
                      body {
                        margin: 0;
                        padding: 0;
                      }
                    </style>
                  </head>
                  <body>
                    <script
                      id="api-reference"
                      data-url="%s"
                      data-proxy-url=""
                      data-theme="purple"
                      src="https://cdn.jsdelivr.net/npm/@scalar/api-reference">
                    </script>
                  </body>
                </html>
                """.formatted(applicationTitle, openApiDocsPath);
    }
}
