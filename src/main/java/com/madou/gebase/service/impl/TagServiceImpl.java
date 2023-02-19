package com.madou.gebase.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.madou.gebase.mapper.TagMapper;
import com.madou.gebase.model.Tag;
import com.madou.gebase.service.TagService;
import org.springframework.stereotype.Service;

/**
* @author MA_dou
* @description 针对表【tag(标签)】的数据库操作Service实现
* @createDate 2023-01-12 16:35:54
*/
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag>
    implements TagService {

}




