package fun.timu.oj.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import fun.timu.oj.mapper.QuestionMapper;
import fun.timu.oj.modal.entity.Question;
import fun.timu.oj.service.QuestionService;
import org.springframework.stereotype.Service;

/**
* @author zhengke
* @description 针对表【question(题目)】的数据库操作Service实现
* @createDate 2025-04-10 11:45:52
*/
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question>
    implements QuestionService {

}




