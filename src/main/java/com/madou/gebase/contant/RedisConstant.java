package com.madou.gebase.contant;

public interface RedisConstant {
    /**
     * 用户列表redis的key
     */
    String REDIS_RECOMMEND_KEY = "gebase:user:recommend";

    /**
     * 用户加入队伍的key
     */
    String REDIS_JOIN_TEAM_KEY = "gebase:join_team";

    /**
     * 用户点赞的key
     */
    String REDIS_THUMB_KEY = "gebase:THUMB";

    String REDIS_RECOMMEND_UPDATE_KEY = "gebase:user:recommend:update";
}
