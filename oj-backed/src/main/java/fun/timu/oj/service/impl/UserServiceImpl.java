package fun.timu.oj.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import fun.timu.oj.mapper.UserMapper;
import fun.timu.oj.modal.entity.User;
import fun.timu.oj.service.UserService;
import org.springframework.stereotype.Service;

/**
 * @author zhengke
 * @description 针对表【user(用户)】的数据库操作Service实现
 * @createDate 2025-04-10 11:45:52
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

}




