package com.nowcoder.demo1.controller;

import com.nowcoder.demo1.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@Controller
public class DataController {
    @Autowired
    private DataService dataService;

//    打开统计页面
    @RequestMapping(path = "/data", method = {RequestMethod.GET,RequestMethod.POST})
    public String getDataPage(){
        return "/site/admin/data";
    }

//    处理网站UV（访客）
    @RequestMapping(path = "/data/uv", method = RequestMethod.POST)
    public String getUV(@DateTimeFormat(pattern = "yyyy-MM-dd") Date start,
                        @DateTimeFormat(pattern = "yyyy-MM-dd")Date end, Model model){

        model.addAttribute("uvStartDate",start);
        model.addAttribute("uvEndDate",end);
        if (start.after(end)){
            model.addAttribute("uvResult","开始日期不能大于结束日期");
            return "forward:/data";
        }

        long uv = dataService.calculateUV(start, end);
        model.addAttribute("uvResult",uv);

        return "forward:/data";
    }

//    处理网站DAU
    @RequestMapping(path = "/data/dau", method = RequestMethod.POST)
    public String getDAU(@DateTimeFormat(pattern = "yyyy-MM-dd") Date start,
                         @DateTimeFormat(pattern = "yyyy-MM-dd") Date end, Model model){
        model.addAttribute("dauStartDate", start);
        model.addAttribute("dauEndDate", end);
        if (start.after(end)){
            model.addAttribute("dauResult","开始日期不能大于结束日期");
            return "forward:/data";
        }

        long dau = dataService.calculateDAU(start, end);
        model.addAttribute("dauResult", dau);

        return "forward:/data";
    }
}
