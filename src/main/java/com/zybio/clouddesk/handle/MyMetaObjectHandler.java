//package com.zybio.clouddesk.handle;
//
//import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.ibatis.reflection.MetaObject;
//import org.springframework.stereotype.Component;
//
//import java.time.ZoneId;
//import java.time.ZonedDateTime;
//
//@Slf4j
//@Component
//public class MyMetaObjectHandler implements MetaObjectHandler {
//
//    //插入时候的填充策略
//    @Override
//    public void insertFill(MetaObject metaObject) {
//        log.info("start insert fill ....");  //日志
//        //设置字段的值（String fieldName字段名,Object fieldVal要传递的值,MetaObject metaObject)
//        this.strictInsertFill(metaObject, "created", ZonedDateTime.class, ZonedDateTime.now(ZoneId.of("Asia/Chongqing")));
//    }
//
//    //更新时间的填充策略
//    @Override
//    public void updateFill(MetaObject metaObject) {
//        log.info("start update fill ....");
//        this.strictUpdateFill(metaObject, "updated", ZonedDateTime.class, ZonedDateTime.now(ZoneId.of("Asia/Chongqing")));
//    }
//}
//
