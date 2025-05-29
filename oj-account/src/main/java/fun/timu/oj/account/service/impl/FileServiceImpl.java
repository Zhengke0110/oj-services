package fun.timu.oj.account.service.impl;


import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectResult;

import fun.timu.oj.account.config.OSSConfig;
import fun.timu.oj.account.service.FileService;
import fun.timu.oj.common.utils.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class FileServiceImpl implements FileService {

    @Resource
    private OSSConfig ossConfig;

    /**
     * 用户头像上传方法
     * 该方法负责将用户选择的图片文件上传到阿里云OSS存储，并返回上传后的图片访问URL
     *
     * @param file 用户选择的图片文件，类型为MultipartFile，Spring框架对上传文件的封装
     * @return 返回上传成功后的图片访问URL，如果上传失败则返回null
     */
    @Override
    public String uploadUserImg(MultipartFile file) {
        // 从配置文件中获取OSS存储桶名称
        String bucketName = ossConfig.getBucketname();
        // 从配置文件中获取OSS服务接入点
        String endpoint = ossConfig.getEndpoint();
        // 从配置文件中获取阿里云访问密钥ID
        String accessKeyId = ossConfig.getAccessKeyId();
        // 从配置文件中获取阿里云访问密钥密钥
        String accessKeySecret = ossConfig.getAccessKeySecret();

        //oss客户端构建
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        //获取文件原始名称 xxx.jpg
        String originalFilename = file.getOriginalFilename();

        //jdk8语法日期格式
        LocalDateTime ldt = LocalDateTime.now();
        DateTimeFormatter pattern = DateTimeFormatter.ofPattern("yyyy/MM/dd");

        //user/2022/12/12/sdsdwe/
        String folder = pattern.format(ldt);
        String fileName = CommonUtil.generateUUID();
        String extendsion = originalFilename.substring(originalFilename.lastIndexOf("."));

        //在oss上的bucket创建文件夹
        String newFilename = "user/" + folder + "/" + fileName + extendsion;

        try {
            // 将文件上传到OSS
            PutObjectResult putObjectResult = ossClient.putObject(bucketName, newFilename, file.getInputStream());
            //拼装返回路径
            if (putObjectResult != null) {
                String imgUrl = "https://" + bucketName + "." + endpoint + "/" + newFilename;
                return imgUrl;
            }

        } catch (IOException e) {
            // 记录文件上传失败的日志信息
            log.error("文件上传失败:{}", e.getMessage());
        } finally {
            // 关闭OSS客户端连接
            ossClient.shutdown();
        }

        return null;
    }
}
