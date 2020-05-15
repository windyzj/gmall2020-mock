package com.atguigu.gmall2020.mock.db.service;

import com.atguigu.gmall2020.mock.db.bean.CommentInfo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 商品评论表 服务类
 * </p>
 *
 * @author zhangchen
 * @since 2020-02-24
 */
public interface CommentInfoService extends IService<CommentInfo> {

    public  void genComments(Boolean ifClear);

}
