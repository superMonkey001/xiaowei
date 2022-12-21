package cn.hncj.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author FanJian
 * @Date 2022-12-20 15:38
 */
@RestController
public class HelloController {
    @GetMapping("/hello")
    public String hello() {
        return "hello xiaowei";
    }
}
