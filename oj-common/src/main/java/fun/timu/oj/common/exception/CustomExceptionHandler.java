package fun.timu.oj.common.exception;

import fun.timu.oj.common.utils.JsonData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
//@RestControllerAdvice
public class CustomExceptionHandler {
    private static Logger logger = LoggerFactory.getLogger(CustomExceptionHandler.class);

    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public JsonData handler(Exception e) {

        if (e instanceof BizException) {
            BizException bizException = (BizException) e;
            logger.error("[业务异常]{}", e);
            return JsonData.buildCodeAndMsg(bizException.getCode(), bizException.getMsg());
        } else {
            logger.error("[系统异常]{}", e);
            return JsonData.buildError("系统异常");
        }

    }

}
