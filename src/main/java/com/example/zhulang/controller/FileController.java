package com.example.zhulang.controller;

import com.example.zhulang.utils.Result;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;

@RestController
@RequestMapping("/file")
public class FileController {

    @PostMapping("/upload")
    public Result<?> upload(MultipartFile file) throws IOException {
        String docFilePath = "/home/server/zhulang/output.docx";
        InputStream inputStream = file.getInputStream();
        Workbook workbook = new XSSFWorkbook(inputStream);
        OutputStream out = new FileOutputStream(docFilePath);
        XWPFDocument document = new XWPFDocument();
        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();
        Sheet sheet = workbook.getSheetAt(0); // 读取第一个工作表
        int numColumns = sheet.getRow(0).getLastCellNum(); // 获取列数
        DataFormatter dataFormatter = new DataFormatter();
        for(int col = 1; col < numColumns; col++) {
            for (Row row : sheet) {
                run.setText(dataFormatter.formatCellValue(row.getCell(0)) + "：");
                run.setText(dataFormatter.formatCellValue(row.getCell(col)));
                run.addBreak();
                run.addBreak();
            }
        }
        document.write(out);

        String url = "https://101.35.49.27:8080/file/download/";

        return Result.success(url);
    }

    @GetMapping("/download")
    public void download(HttpServletResponse response) {
        String filePath = "/home/server/zhulang/output.docx";
        try {
            File file = new File(filePath);
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.setHeader("Content-Disposition", "attachment; filename=" + file.getName());
            response.setHeader("Content-Length", String.valueOf(file.length()));

            try (InputStream inputStream = new FileInputStream(file);
                 OutputStream outputStream = response.getOutputStream()) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
