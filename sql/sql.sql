

create table notice
(
    id          bigint auto_increment comment '消息id'
        primary key,
    senderId    bigint                              not null comment '发送者id',
    receiverId  bigint                              not null comment '接收者id',
    content     varchar(256)                        not null comment '信息体(最大200字)',
    contentType int                                 not null comment '信息类型，1为评论，2为回复评论，3为点赞，4系统通知',
    targetId    bigint                              null comment '信息体对象，帖子id等',
    noticeState tinyint   default 0                 not null comment '状态 0未读，1已读',
    createTime  datetime  default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime  timestamp default CURRENT_TIMESTAMP not null comment '更新时间',
    isDelete    tinyint   default 0                 not null comment '是否删除'
)
    comment '通知';

create table post
(
    id         bigint auto_increment comment '帖子id'
        primary key,
    content    text                                not null comment '内容',
    userId     bigint                              not null comment '用户id',
    postState  int       default 0                 not null comment '状态 0 正常',
    thumbNum   int       default 0                 not null comment '点赞数',
    createTime datetime  default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime timestamp default CURRENT_TIMESTAMP not null comment '更新时间',
    isDelete   tinyint   default 0                 not null comment '是否删除'
)
    comment '帖子';

create table post_comment
(
    id           bigint auto_increment comment '评论id'
        primary key,
    userId       bigint                              not null comment '评论用户id',
    postId       bigint                              not null comment '评论帖子id',
    content      varchar(256)                        not null comment '评论内容(最大200字)',
    pid          bigint                              not null comment '父id',
    commentState int       default 0                 not null comment '状态 0 正常',
    createTime   datetime  default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime   timestamp default CURRENT_TIMESTAMP not null comment '更新时间',
    isDelete     tinyint   default 0                 not null comment '是否删除'
)
    comment '帖子';

create table post_thumb
(
    id         bigint auto_increment comment 'id'
        primary key,
    postId     bigint                             not null comment '帖子 id',
    userId     bigint                             not null comment '创建用户 id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '帖子点赞';

create index idx_postId
    on post_thumb (postId);

create index idx_userId
    on post_thumb (userId);

create table team
(
    id           bigint auto_increment comment '队伍id'
        primary key,
    userId       bigint                              not null comment '用户id',
    teamName     varchar(256)                        not null comment '队伍名称',
    description  varchar(1024)                       null comment '队伍描述',
    maxNum       int       default 1                 not null comment '最大人数',
    teamPassword varchar(512)                        null comment '队伍密码',
    teamState    int       default 0                 not null comment '状态 0-正常 1-私有  2-加密',
    expireTime   datetime                            null comment '过期时间',
    createTime   datetime  default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime   timestamp default CURRENT_TIMESTAMP not null comment '更新时间',
    isDelete     tinyint   default 0                 not null comment '是否删除'
)
    comment '队伍';

create table user
(
    id           bigint auto_increment comment '用户id'
        primary key,
    username     varchar(256)                        null comment '用户昵称',
    userAccount  varchar(256)                        null comment '用户账号',
    userProfile  varchar(512)                        null comment '用户自我介绍',
    gender       tinyint                             null comment '性别',
    userPassword varchar(512)                        not null comment '密码',
    phone        varchar(128)                        null comment '电话',
    email        varchar(512)                        null comment '信箱',
    userState    int       default 0                 not null comment '状态 0 正常',
    createTime   datetime  default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime   timestamp default CURRENT_TIMESTAMP not null comment '更新时间',
    isDelete     tinyint   default 0                 not null comment '是否删除',
    userRole     int       default 0                 not null comment '0 - 普通用户 1 - 管理员
',
    tags         varchar(1024)                       null comment '标签列表',
    avatarUrl    varchar(1024)                       null comment '头像'
)
    comment '用户';

create table user_team
(
    id         bigint auto_increment comment 'id'
        primary key,
    userId     bigint                              not null comment '用户id',
    teamId     bigint                              not null comment '队伍id',
    joinTime   datetime                            null comment '加入时间',
    createTime datetime  default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime timestamp default CURRENT_TIMESTAMP not null comment '更新时间',
    isDelete   tinyint   default 0                 not null comment '是否删除'
)
    comment '用户队伍关系';

