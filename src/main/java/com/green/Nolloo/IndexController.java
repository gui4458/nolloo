package com.green.Nolloo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
@Controller
public class IndexController {
    @GetMapping("/")
    public String maim(){
        return "redirect:/item/list";
    }
}
