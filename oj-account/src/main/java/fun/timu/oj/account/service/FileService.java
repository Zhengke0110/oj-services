package fun.timu.oj.account.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    /**
     * 上传用户头像
     * @param file
     * @return
     */
    String uploadUserImg(MultipartFile file);
}
