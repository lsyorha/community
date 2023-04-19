package com.nowcoder.demo1.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class MyController {
    @RequestMapping(path = "/students",method = RequestMethod.GET)
    @ResponseBody
    public String students(
            @RequestParam(name = "current",required = false,defaultValue = "1") int current,
            @RequestParam(name = "limit",required = false,defaultValue = "15") int limit){
        System.out.println(current + ": " + limit);
        return "hello，word";
    }
//PathVariable!!!!!
    @RequestMapping(path = "/student/{id}",method = RequestMethod.GET)
    @ResponseBody
    public String student(@PathVariable("id") int id){
        System.out.println(id);
        return "hello，student";
    }

    @RequestMapping(value = "/student",method = RequestMethod.POST)
    @ResponseBody
    public String save(String name,int age){
        System.out.println("name：" + name);
        System.out.println("age：" + age);
        return "success";
    }

    @RequestMapping(value = "/teacher",method = RequestMethod.GET)
    public ModelAndView teacher(){
        ModelAndView view = new ModelAndView();
        view.addObject("name","猴子");
        view.addObject("age","11");
        view.setViewName("/view");
        return view;
    }

    @RequestMapping(value = "/view",method = RequestMethod.GET)
    public String view(Model model){
        model.addAttribute("name","猴子");
        model.addAttribute("age","111");
        return "view";
    }

//    JSON字符串存储
    @RequestMapping(value = "employees",method = RequestMethod.GET)
    @ResponseBody
    public List<Map<String,Object>> employees(){
        List<Map<String,Object>> list = new ArrayList();
        Map<String,Object> emp = new HashMap<>();
        emp.put("name","战三");
        emp.put("age","10");
        emp.put("salary","999");
        list.add(emp);

        emp = new HashMap<>();
        emp.put("name","李四");
        emp.put("age","11");
        emp.put("salary","9299");
        list.add(emp);

        emp = new HashMap<>();
        emp.put("name","王五");
        emp.put("age","10");
        emp.put("salary","9939");
        list.add(emp);
        return list;
    }

}
