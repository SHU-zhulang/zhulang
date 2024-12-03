package com.example.zhulang.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.zhulang.entity.Route;
import com.example.zhulang.entity.User;
import com.example.zhulang.mapper.PeopleMapper;
import com.example.zhulang.mapper.UserMapper;
import com.example.zhulang.utils.Result;
import com.example.zhulang.entity.People;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/people")
public class PeopleController {
    @Resource
    private PeopleMapper peopleMapper;

    @Resource
    private UserMapper userMapper;

    @PostMapping("/save")
    public Result<?> save(@RequestBody People people) {
        // 检查当前队员是否提交过本条线路的人物贴
        List<People> curMember = peopleMapper.isSaved(people.getRouteId(), people.getMemberId());
        if (!curMember.isEmpty()){
            // 提交过，更新对应记录
            people.setId(curMember.get(0).getId());
            peopleMapper.updateById(people);
            return Result.success();
        }
        else {
            // 没提交过，插入新记录
            System.out.println(people);
            peopleMapper.insert(people);
            return Result.success();
        }
    }

    @PostMapping("/memberHasSubmitted")
    public Result<?> memberHasSubmitted(@RequestBody Route route) {
        return Result.success(peopleMapper.memberHasSubmitted(route.getId()));
    }

    /***
     * 找出当前用户的人物贴
     * @param routeId
     * @param memberId
     * @return
     */
    @GetMapping("/myPeople")
    public Result<?> myPeople(@RequestParam(defaultValue = "") Integer routeId,
                             @RequestParam(defaultValue = "") Integer memberId) {
        return Result.success(peopleMapper.isSaved(routeId, memberId));
    }

    /***
     * 人物贴头图上传
     * @param photo
     * @return
     * @throws IOException
     */
    @PostMapping("/photo")
    public Result<?> photo(MultipartFile photo) throws IOException {
        String lastName = photo.getOriginalFilename();
        String filePath = "/home/resources/people/" + lastName;
//        String filePath = "E:\\zhulang\\people\\" + lastName;
        // 使用 try-with-resources 确保流关闭
        try (InputStream inputStream = photo.getInputStream();
             OutputStream outputStream = new FileOutputStream(filePath)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
        return Result.success("http://www.zhulang.online/resources/people/" + lastName);
    }

    /***
     * 生成人物贴word文档
     * @param route
     * @return
     * @throws IOException
     */
    @PostMapping("/export")
    public Result<?> export(@RequestBody Route route) throws IOException {
        // 找出当前路线所有队员
        List<String> memberUids = StrUtil.split(route.getMember(), ',');
        List<User> allMember = new ArrayList<>();
        for (String uid : memberUids) {
            allMember.add(userMapper.selectById(Integer.parseInt(uid)));
        }

        // 找出当前线路所有人物贴
        List<People> allPeople = new ArrayList<>();
        String[] allMemberUid = route.getMember().split(",");
        for(String uid : allMemberUid){
            List<People> curMember = peopleMapper.isSaved(route.getId(), Integer.parseInt(uid));
            if (!curMember.isEmpty()){
                allPeople.add(curMember.get(0));
            }
        }
        System.out.println(allPeople);

        // 开始制作人物贴word文档
        String docLastName = UUID.randomUUID() + ".docx";
        String docFilePath = "/home/resources/people/" + docLastName; // word文档最终存放地址
//        String docFilePath = "E:\\zhulang\\people\\" + docLastName; // word文档最终存放地址
        try (XWPFDocument document = new XWPFDocument();
             OutputStream out = new FileOutputStream(docFilePath)) {
            // 创建段落和内容
            XWPFParagraph paragraph = document.createParagraph();
            XWPFRun run = paragraph.createRun();

            // 开始书写内容
            // 写个标题
            // 创建一个段落
            XWPFParagraph titleParagraph = document.createParagraph();
            // 设置段落居中
            titleParagraph.setAlignment(ParagraphAlignment.CENTER);
            // 创建一个文本运行
            XWPFRun titleRun = titleParagraph.createRun();
            // 设置标题文本
            titleRun.setText(route.getName() + "人物贴");
            // 设置加粗
            titleRun.setBold(true);
            // 设置字体大小
            titleRun.setFontSize(30);
            // 设置字体
            titleRun.setFontFamily("宋体");

            // 对大家想说的话
            XWPFParagraph minasanParagraph = document.createParagraph();
            XWPFRun minasanRun = minasanParagraph.createRun();
            minasanRun.setText("to 大家/这条线");
            minasanRun.setBold(true);
            minasanRun.setFontSize(15);
            minasanRun.addBreak();
            for (People curPeople : allPeople) {
                XWPFParagraph curParagraph = document.createParagraph();
                XWPFRun curRun = curParagraph.createRun();
                curRun.setText(curPeople.getNickName() + "：");
                curRun.setText(curPeople.self);
                curRun.addBreak();
            }

            // 遍历allPeople，给每个人拼凑人物贴。在每次遍历中，去所有的content当中寻找写给当前用户的人物贴
            for(User user : allMember){
                // 为当前用户的名字新建一个段落
                XWPFParagraph nameUserparagraph = document.createParagraph();
                XWPFRun nameRun = nameUserparagraph.createRun();
                // 段落开始写上他的名字
                nameRun.setText("to " + user.getNickName());
                nameRun.setBold(true);
                nameRun.setFontSize(15);
                nameRun.addBreak();
                // 开始写他的人物贴
                XWPFParagraph curParagraph = document.createParagraph();
                XWPFRun curRun = curParagraph.createRun();
                // 找一找大家给他写的
                for (People curPeople : allPeople) {
                    JSONArray curContent = JSONUtil.parseArray(curPeople.getContent());
                    for(int i = 0; i < curContent.size(); i++){
                        // 将每个元素强制转换为 JSONObject
                        JSONObject item = curContent.getJSONObject(i);
                        if(Objects.equals(item.getInt("uid"), user.getUid()) && !Objects.equals(item.getStr("people"), "")){
                            curRun.setText(curPeople.getNickName() + "：");
                            curRun.setText(item.getStr("people"));
                            curRun.addBreak();
                            curRun.addBreak();
                        }
                    }
                }
            }

            // 写入文件
            document.write(out);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 打压缩包
        // 要压缩的文件路径
        List<String> filesToZip = new ArrayList<>();
        for (People curPeople : allPeople) {
            filesToZip.add(curPeople.getPhoto());
        }
        filesToZip.add("http://www.zhulang.online/resources/people/" + docLastName);

        // 压缩包的最终存储路径
        String zipLastName = UUID.randomUUID() + ".zip";
//        String outputZipFilePath = "E:\\zhulang\\people\\" + zipLastName;
        String outputZipFilePath = "/home/resources/people/" + zipLastName;

        try {
            // 压缩文件并存储到指定路径
            byte[] zipData = createZipInMemory(filesToZip);
            saveZipToFile(zipData, outputZipFilePath);

            System.out.println("压缩完成，文件已保存到: " + outputZipFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Result.success("http://www.zhulang.online/resources/people/" + zipLastName);
    }

    /**
     * 将若干文件压缩到内存中
     *
     * @param files 文件路径数组（本地文件路径或在线资源 URL）
     * @return 压缩包的字节数据
     * @throws IOException
     */
    public static byte[] createZipInMemory(List<String> files) throws IOException {
        // 使用 ByteArrayOutputStream 存储压缩数据
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(byteArrayOutputStream)) {

            for (String filePath : files) {
                File file;

                // 判断是否为在线资源（URL）
                if (filePath.startsWith("http://") || filePath.startsWith("https://")) {
                    file = downloadFile(filePath); // 下载在线资源到临时文件
                } else {
                    file = new File(filePath); // 本地文件
                }

                // 检查文件是否存在
                if (file == null || !file.exists()) {
                    System.out.println("文件不存在: " + filePath);
                    continue;
                }

                // 创建 ZIP 条目并写入数据
                try (FileInputStream fis = new FileInputStream(file)) {
                    ZipEntry zipEntry = new ZipEntry(file.getName());
                    zos.putNextEntry(zipEntry);

                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = fis.read(buffer)) >= 0) {
                        zos.write(buffer, 0, length);
                    }

                    zos.closeEntry();
                }

                // 如果是临时文件，删除它
                if (filePath.startsWith("http://") || filePath.startsWith("https://")) {
                    file.delete();
                }
            }

            // 返回压缩包的字节数据
            zos.finish();
            return byteArrayOutputStream.toByteArray();
        }
    }

    /**
     * 下载在线资源到本地临时文件
     *
     * @param fileUrl 在线资源的 URL
     * @return 下载的临时文件
     * @throws IOException
     */
    public static File downloadFile(String fileUrl) throws IOException {
        System.out.println("正在下载: " + fileUrl);

        // 创建 URL 对象
        URL url = new URL(fileUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        // 检查响应码
        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            // 获取文件名
            String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);

            // 创建临时文件（无 .tmp 后缀）
            File tempFile = new File(System.getProperty("java.io.tmpdir"), fileName);
            tempFile.deleteOnExit();

            // 将文件内容写入本地文件
            try (InputStream inputStream = connection.getInputStream();
                 FileOutputStream outputStream = new FileOutputStream(tempFile)) {

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }

            System.out.println("下载完成: " + tempFile.getAbsolutePath());
            return tempFile;
        } else {
            System.out.println("下载失败: " + fileUrl + "，HTTP 响应码: " + connection.getResponseCode());
            return null;
        }
    }


    /**
     * 将压缩包的字节数据写入文件
     *
     * @param zipData 压缩包的字节数据
     * @param outputFilePath 输出文件路径
     * @throws IOException
     */
    public static void saveZipToFile(byte[] zipData, String outputFilePath) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(outputFilePath)) {
            fos.write(zipData);
        }
    }
}
