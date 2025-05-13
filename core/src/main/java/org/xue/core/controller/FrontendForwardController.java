package org.xue.core.controller;

import org.springframework.web.bind.annotation.RequestMapping;

public class FrontendForwardController {
    @RequestMapping(value = "/{path:^(?!api|static|assets|js|css).*$}/**")
    public String forward() {
        return "forward:/index.html";
    }
}
