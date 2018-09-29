package com.netease.commons.utils.io;

import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.FileOutputStream;

public class OkFileUtils extends FileUtils {

    public static void download(Workbook workbook, String path) throws Exception {
        FileOutputStream fos = new FileOutputStream("D://ExcelExportHasImgTest.exportCompanyImg.xls");
        workbook.write(fos);
        fos.close();
    }
}
