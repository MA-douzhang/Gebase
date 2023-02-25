package com.madou.gebase.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.madou.gebase.model.PostComment;
import com.madou.gebase.service.PostCommentService;
import com.madou.gebase.mapper.PostCommentMapper;
import org.springframework.stereotype.Service;

/**
* @author MA_dou
* @description 针对表【post_comment(帖子)】的数据库操作Service实现
* @createDate 2023-02-25 17:17:07
*/
@Service
public class PostCommentServiceImpl extends ServiceImpl<PostCommentMapper, PostComment>
    implements PostCommentService{

}




