package com.madou.gebase.utils;

import lombok.Data;
import org.springframework.context.annotation.Configuration;

import java.io.File;


/**
 * 文件上传类
 */
@Configuration
@Data
public class FileUtils {

    public static final String path = "D:\\项目开发\\Gebase-bankend\\src\\main\\resources\\uploadAvatarImg\\";

    public File getPath() {
        // 构建上传文件的存放 "文件夹" 路径
        String fileDirPath = path;
        File fileDir = new File(fileDirPath);
        if (!fileDir.exists()) {
            // 递归生成文件夹
            fileDir.mkdirs();
        }
        return fileDir;
    }
    public boolean del(String filename) {
        File file = new File(path + File.separator + filename);
        return file.delete();
    }
    public boolean del(String path, String filename) {
        return new File(path + File.separator + filename).delete();
    }
}
