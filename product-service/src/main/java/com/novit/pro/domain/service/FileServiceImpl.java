package com.novit.pro.domain.service;

import com.google.common.collect.Lists;
import com.novit.pro.until.FTPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service("iFileService")
public class FileServiceImpl implements IFileService {

    private Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    //要把上传之后的文件名返回回去
    public String upload(MultipartFile file, String path){
        String fileName = file.getOriginalFilename();//拿到文件的原始文件名
        //扩展名
        //abc.jpg
        //abc.abc.abc.jpg所以要取最后一个点的位置
        //获取文件的扩展名，substring从最后开始找到第一个点然后取后面的字符，+1表示不取该点，例如abc.jpg就得到jpg
        String fileExtensionName = fileName.substring(fileName.lastIndexOf(".")+1);

        //上传文件的名字，用UUID拼接的原因是让不同人上传同名文件的时候不覆盖
        //A:abc.jpg     B:abc.jpg
        String uploadFileName = UUID.randomUUID().toString()+"."+fileExtensionName;
        logger.info("开始上传文件,上传文件的文件名:{},上传的路径:{},新文件名:{}",fileName,path,uploadFileName);//打印一下日志，因为该服务会被经常调用，用到了变长参数

        File fileDir = new File(path);//声明目录的file
        if(!fileDir.exists()){//对文件夹进行判断，如果文件夹不存在就要创建它
            fileDir.setWritable(true);//先赋予权限，可写
            fileDir.mkdirs();//创建文件夹
        }
        File targetFile = new File(path,uploadFileName);//目录创建好了，此处创建文件


        try {
            file.transferTo(targetFile);
            //文件已经上传成功了

            //将targetFile上传到FTP服务器上
            FTPUtil.uploadFile(Lists.newArrayList(targetFile));
            //已经上传到ftp服务器上

            //上传完之后，删除upload下面的文件
            targetFile.delete();
        } catch (IOException e) {
            logger.error("上传文件异常",e);
            return null;
        }
        return targetFile.getName();//传回目标文件的文件名
    }
}
