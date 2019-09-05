package com.example.demo.controller.admin;

import com.example.demo.bean.Detailed;
import com.example.demo.bean.Language;
import com.example.demo.bean.RestResultModule;
import com.example.demo.dao.DfeedbackDao;
import com.example.demo.dao.LanguageDao;
import com.example.demo.entity.ExcelConstant;
import com.example.demo.entity.ExcelData;
import com.example.demo.service.DetailedService;
import com.example.demo.util.ExcelUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/*
 * 报表Excel
 * paddy 2019/02/28
 * */
@RestController
@RequestMapping(value = "appJson/admin/excel")
@Component("AdminExcelController")
public class ExcelController {
    private  static Logger logger = LoggerFactory.getLogger(ExcelController.class);

    @Resource
    private LanguageDao languageDao;

    @Resource
    private DetailedService detailedService;

    @Resource
    private DfeedbackDao dfeedbackDao;


    /**
     * 获取全部FAQ
     * @param response
     */
    @RequestMapping("/faq")
    public void test3(HttpServletResponse response){
        List<Language> languages = languageDao.findAll();
        try {
            Date now = new Date( );
            SimpleDateFormat ft = new SimpleDateFormat ("yyyy.MM.dd hh:mm:ss");
            // 告诉浏览器用什么软件可以打开此文件
            response.setHeader("content-Type", "application/vnd.ms-excel");
            // 下载文件的默认名称
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode("FAQ全部问题"+ ft.format(now) + ".xls", "utf-8"));
            OutputStream out = response.getOutputStream();
            String[] headers = { "父级", "问题ID" ,"标题","答案"};
            ExcelUtils eeu = new ExcelUtils();
            HSSFWorkbook workbook = new HSSFWorkbook();
            int index = 0;
            for (Language l:languages) {
                List<Detailed> list = detailedService.getAllByLangId(l.getId());
                List<List<Object>> data = new ArrayList<List<Object>>();
                for(int i = 0, length = list.size();i<length;i++){
                    Detailed detailed = list.get(i);
                    List rowData = new ArrayList();
                    rowData.add(detailed.getFlTitle());
                    rowData.add(detailed.getId());
                    rowData.add(detailed.getTitle());
                    rowData.add(detailed.getContentTxt());
                    data.add(rowData);
                }
                eeu.exportExcel1(workbook, index++, l.getTitle(), headers, data, out);
            }
            //原理就是将所有的数据一起写入，然后再关闭输入流。
            workbook.write(out);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * 获取全部点击率
     * @param response
     */
    @RequestMapping(value="/monitor")
    public void test4(HttpServletResponse response, HttpServletRequest request,
                      @RequestParam(name = "langId",required = false,defaultValue = "0")long langId,
                      @RequestParam(name = "startTime",required = false,defaultValue = "")String startTime,
                      @RequestParam(name = "endTime",required = false,defaultValue = "")String endTime){
        request.getSession().removeAttribute("monitor");
        List<Language> languages = new ArrayList<>();
        if(langId > 0 ){
            Language l = languageDao.findById(langId);
            languages.add(l);
        }else{
            languages = languageDao.findAll();
        }
        try {
            // 告诉浏览器用什么软件可以打开此文件
            response.setHeader("content-Type", "application/vnd.ms-excel");
            // 下载文件的默认名称
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode("FAQ点击率.xls", "utf-8"));
            OutputStream out = response.getOutputStream();

            String[] headers = { "父级名", "FAQ标题" ,"发布状态","点击率"};
            ExcelUtils eeu = new ExcelUtils();
            HSSFWorkbook workbook = new HSSFWorkbook();
            int index = 0;
            for (Language l:languages) {
                List<Object[]> list = languageDao.getAllObjects(l.getId(),startTime,endTime);
                List<List<Object>> data = new ArrayList<List<Object>>();
                for(int i = 0, length = list.size();i<length;i++){

                    Object[] os = list.get(i);
                    List rowData = new ArrayList();
                    rowData.add(os[0]);
                    rowData.add(os[1]);
                    rowData.add(os[2]);
                    rowData.add(os[3]);
                    data.add(rowData);
                }
                eeu.exportExcel1(workbook, index++, l.getTitle(), headers, data, out);
            }
            //原理就是将所有的数据一起写入，然后再关闭输入流。
            workbook.write(out);
            out.flush();
            out.close();
            request.getSession().setAttribute("monitor","ok");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 获取全部反馈数
     * @param response
     */
    @RequestMapping("/test5")
    public void test5( @PathVariable("id") long id,HttpServletResponse response){
        List<Language> languages = languageDao.findAll();
        try {
            Date now = new Date( );
            SimpleDateFormat ft = new SimpleDateFormat ("yyyy.MM.dd hh:mm:ss");
            // 告诉浏览器用什么软件可以打开此文件
            response.setHeader("content-Type", "application/vnd.ms-excel");
            // 下载文件的默认名称
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode("FAQ反馈"+id+ ".xls", "utf-8"));
            OutputStream out = response.getOutputStream();
            String[] headers = { "序号", "语言" ,"FAQ标题","FAQ问题内容","Rating","创建时间","评价内容","评价邮件","评价电话"};
            ExcelUtils eeu = new ExcelUtils();
            HSSFWorkbook workbook = new HSSFWorkbook();
            int index = 0;
            String s = "2019-"+id+"-01 00:00:00";
            String e = "2019-"+(id+1)+"-01 00:00:00";
            List<Object[]> list = languageDao.getAllObjects1(s,e);
            List<List<Object>> data = new ArrayList<List<Object>>();
            for(int i = 0, length = list.size();i<length;i++){

                Object[] os = list.get(i);
                List rowData = new ArrayList();
                rowData.add(os[0]);
                rowData.add(os[1]);
                rowData.add(os[2]);
                rowData.add(os[3]);
                rowData.add(os[4]);
                rowData.add(os[5]);
                rowData.add(os[6]);
                rowData.add(os[7]);
                rowData.add(os[8]);
                data.add(rowData);
            }
            eeu.exportExcel1(workbook, index++, "sheet1", headers, data, out);
            //原理就是将所有的数据一起写入，然后再关闭输入流。
            workbook.write(out);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }



    /**
     * 获取全部点击率
     * @param response
     */
    @RequestMapping(value="/feedback")
    public void feedback(HttpServletResponse response, HttpServletRequest request,
                      @RequestParam(name = "langId",required = false,defaultValue = "0")long langId,
                      @RequestParam(name = "comment",required = false,defaultValue = "0")long comment,
                      @RequestParam(name = "df_type",required = false,defaultValue = "0")long df_type,
                      @RequestParam(name = "commentStatu",required = false,defaultValue = "0")long commentStatu,
                      @RequestParam(name = "startTime",required = false,defaultValue = "")String startTime,
                      @RequestParam(name = "endTime",required = false,defaultValue = "")String endTime){

        System.out.println(1);
        request.getSession().removeAttribute("feedback");
        List<Language> languages = new ArrayList<>();
        if(langId > 0 ){
            Language l = languageDao.findById(langId);
            languages.add(l);
        }else{
            languages = languageDao.findAll();
        }
        try {
            // 告诉浏览器用什么软件可以打开此文件
            response.setHeader("content-Type", "application/vnd.ms-excel");
            // 下载文件的默认名称
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode("FAQ-Feedback.xls", "utf-8"));
            OutputStream out = response.getOutputStream();

            String[] headers = { "序号","FAQ标题","FAQ问题内容","Rating","创建时间","评价内容","评价邮件","评价电话"};
            ExcelUtils eeu = new ExcelUtils();
            HSSFWorkbook workbook = new HSSFWorkbook();
            int index = 0;
            for (Language l:languages) {
                List<Object[]> list = dfeedbackDao.getAllByDfTypeExcel(l.getId(),comment,commentStatu,df_type,startTime,endTime);
                List<List<Object>> data = new ArrayList<List<Object>>();
                for(int i = 0, length = list.size();i<length;i++){

                    Object[] os = list.get(i);
                    List rowData = new ArrayList();
                    rowData.add(os[0]);
                    rowData.add(os[1]);
                    rowData.add(os[2]);
                    rowData.add(os[3].equals(1)?"+1":"-1");
                    rowData.add(os[4]);
                    rowData.add(os[5]);
                    rowData.add(os[6]);
                    rowData.add(os[7]);
                    data.add(rowData);
                }
                eeu.exportExcel1(workbook, index++, l.getTitle(), headers, data, out);
            }
            //原理就是将所有的数据一起写入，然后再关闭输入流。
            workbook.write(out);
            out.flush();
            out.close();
            request.getSession().setAttribute("feedback","ok");
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    /**
     * check
     */
    @ResponseBody
    @RequestMapping("/check")
    public RestResultModule test6(HttpServletRequest request,@RequestParam(name = "id",required = false,defaultValue = "")String id){
        RestResultModule module = new RestResultModule();
        String s = "on";
        if(null != id){
            if("ok".equals(request.getSession().getAttribute(id))){
                s = "ok";
            }
        }
        module.putData("s",s);
        return module;
    }

}
