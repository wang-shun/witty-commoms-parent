package com.netease.commons.utils;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.annotation.Excel;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import com.google.common.collect.Lists;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.FileOutputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class StudentEntity implements Serializable {
    /**
     * id
     */
    private String        id;
    /**
     * 学生姓名
     */
    @Excel(name = "学生姓名", height = 20, width = 30, isImportField = "true_st")
    private String        name;
    /**
     * 学生性别
     */
    @Excel(name = "学生性别", replace = { "男_1", "女_2" }, suffix = "生", isImportField = "true_st")
    private int           sex;

    @Excel(name = "出生日期", databaseFormat = "yyyyMMddHHmmss", format = "yyyy-MM-dd", isImportField = "true_st", width = 20)
    private Date birthday;

    @Excel(name = "进校日期", databaseFormat = "yyyyMMddHHmmss", format = "yyyy-MM-dd")
    private Date registrationDate;

    public StudentEntity(String id, String name, int sex, Date birthday, Date registrationDate) {
        this.id = id;
        this.name = name;
        this.sex = sex;
        this.birthday = birthday;
        this.registrationDate = registrationDate;
    }

    public static void main(String[] args)throws Exception {

        List<StudentEntity> list = Lists.newArrayList();
        list.add(new StudentEntity("1","tomcat",1,new Date(),new Date()));
        list.add(new StudentEntity("1","tomcat",1,new Date(),new Date()));
        list.add(new StudentEntity("1","tomcat",1,new Date(),new Date()));

        Workbook workbook = ExcelExportUtil.exportExcel(new ExportParams("计算机一班学生","学生"),
                StudentEntity .class, list);
        FileOutputStream fos = new FileOutputStream("D://ExcelExportHasImgTest.exportCompanyImg.xls");
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }
}
