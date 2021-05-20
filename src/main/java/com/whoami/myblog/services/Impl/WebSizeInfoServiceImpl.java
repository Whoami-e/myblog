package com.whoami.myblog.services.Impl;

import com.whoami.myblog.dao.SettingsDao;
import com.whoami.myblog.entity.Setting;
import com.whoami.myblog.response.ResponseResult;
import com.whoami.myblog.services.WebSizeInfoService;
import com.whoami.myblog.utils.Constants;
import com.whoami.myblog.utils.RedisUtil;
import com.whoami.myblog.utils.SnowFlakeIdWorker;
import com.whoami.myblog.utils.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class WebSizeInfoServiceImpl implements WebSizeInfoService {

    @Autowired
    private SettingsDao settingsDao;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private SnowFlakeIdWorker snowFlakeIdWorker;

    @Override
    public ResponseResult getWebSizeTitle() {
        Setting title = settingsDao.selectByKey(Constants.Settings.WEB_SIZE_TITLE);
        return ResponseResult.SUCCESS("获取网站TITLE成功").setData(title);
    }

    @Override
    public ResponseResult putWebSizeTitle(String title) {
        if (TextUtils.isEmpty(title)) {
            return ResponseResult.FAILED("网站标题不可以为空");
        }
        Setting titleFromDb = settingsDao.selectByKey(Constants.Settings.WEB_SIZE_TITLE);
        if (titleFromDb == null) {
            Setting setting = new Setting();
            setting.setId(snowFlakeIdWorker.nextId() + "");
            setting.setUpdateTime(new Date());
            setting.setCreateTime(new Date());
            setting.setValue(title);
            setting.setKey(Constants.Settings.WEB_SIZE_TITLE);
            settingsDao.insert(setting);
            return ResponseResult.SUCCESS("网站标题添加成功");
        }
        titleFromDb.setValue(title);
        titleFromDb.setUpdateTime(new Date());
        settingsDao.updateByPrimaryKeySelective(titleFromDb);
        return ResponseResult.SUCCESS("网站标题更新成功");
    }

    @Override
    public ResponseResult getSeoInfo() {
        Setting description = settingsDao.selectByKey(Constants.Settings.WEB_SIZE_DESCRIPTION);
        Setting keyWords = settingsDao.selectByKey(Constants.Settings.WEB_SIZE_KEYWORDS);
        Map<String,String> result = new HashMap<>();
        result.put(description.getKey(), description.getValue());
        result.put(keyWords.getKey(), keyWords.getValue());
        return ResponseResult.SUCCESS("获取SEO信息成功").setData(result);
    }

    @Override
    public ResponseResult putSeoInfo(String keywords, String description) {

        if (TextUtils.isEmpty(keywords)) {
            return ResponseResult.FAILED("关键字不可以为空");
        }
        if (TextUtils.isEmpty(description)) {
            return ResponseResult.FAILED("网站描述不可以为空");
        }

        Setting descriptionFromDb = settingsDao.selectByKey(Constants.Settings.WEB_SIZE_DESCRIPTION);
        Setting keyWordsFromDb = settingsDao.selectByKey(Constants.Settings.WEB_SIZE_KEYWORDS);
        int state1 = 0, state2 = 0;
        if (descriptionFromDb == null) {
            Setting newDescription = new Setting();
            newDescription.setId(snowFlakeIdWorker.nextId() + "");
            newDescription.setUpdateTime(new Date());
            newDescription.setCreateTime(new Date());
            newDescription.setKey(Constants.Settings.WEB_SIZE_DESCRIPTION);
            newDescription.setValue(description);
            settingsDao.insert(newDescription);
            state1 = 1;
        }
        if (keyWordsFromDb == null) {
            Setting newKeyWords = new Setting();
            newKeyWords.setId(snowFlakeIdWorker.nextId() + "");
            newKeyWords.setUpdateTime(new Date());
            newKeyWords.setCreateTime(new Date());
            newKeyWords.setKey(Constants.Settings.WEB_SIZE_KEYWORDS);
            newKeyWords.setValue(keywords);
            settingsDao.insert(newKeyWords);
            state2 = 1;
        }
        if(state1 == 1 && state2 == 1){
            return ResponseResult.SUCCESS("网站关键字和描述初始化成功");
        }else if(state1 == 1){
            return ResponseResult.SUCCESS("网站描述初始化成功");
        }else if (state2 == 1 ){
            return ResponseResult.SUCCESS("网站关键字初始化成功");
        }


        descriptionFromDb.setValue(description);
        descriptionFromDb.setUpdateTime(new Date());
        keyWordsFromDb.setValue(keywords);
        keyWordsFromDb.setUpdateTime(new Date());

        settingsDao.updateByPrimaryKeySelective(descriptionFromDb);
        settingsDao.updateByPrimaryKeySelective(keyWordsFromDb);

        return ResponseResult.SUCCESS("更新SEO信息成功");
    }

    @Override
    public ResponseResult getWebSizeViewCount() {

        Object viewCountFromRedis =  redisUtil.get(Constants.Settings.WEB_SIZE_VIEW_COUNT);
        Setting viewCount = settingsDao.selectByKey(Constants.Settings.WEB_SIZE_VIEW_COUNT);
        if (viewCount == null) {
            viewCount = this.initViewItem();
            settingsDao.insert(viewCount);
        }
        if (viewCountFromRedis == null) {
            viewCountFromRedis = Integer.valueOf(viewCount.getValue());
            redisUtil.set(Constants.Settings.WEB_SIZE_VIEW_COUNT,viewCountFromRedis);
        } else {
            //把Redis里的更新到数据库
            viewCount.setValue(String.valueOf(viewCountFromRedis));
            settingsDao.updateByPrimaryKeySelective(viewCount);
        }


        Map<String, Integer> result = new HashMap<>();
        result.put(viewCount.getKey(),Integer.valueOf(viewCount.getValue()));
        return ResponseResult.SUCCESS("获取网站浏览量成功").setData(result);
    }

    private Setting initViewItem() {
        Setting viewCount;
        viewCount = new Setting();
        viewCount.setId(snowFlakeIdWorker.nextId() + "");
        viewCount.setKey(Constants.Settings.WEB_SIZE_VIEW_COUNT);
        viewCount.setUpdateTime(new Date());
        viewCount.setCreateTime(new Date());
        viewCount.setValue("1");
        return viewCount;
    }

    @Override
    public void updateViewCount() {

        Object viewCount = redisUtil.get(Constants.Settings.WEB_SIZE_VIEW_COUNT);
        if(viewCount == null){
            Setting setting = settingsDao.selectByKey(Constants.Settings.WEB_SIZE_VIEW_COUNT);
            if (setting == null) {
                setting = this.initViewItem();
                settingsDao.insert(setting);
            }
            redisUtil.set(Constants.Settings.WEB_SIZE_VIEW_COUNT,Integer.valueOf(setting.getValue()));
        } else {
            redisUtil.incr(Constants.Settings.WEB_SIZE_VIEW_COUNT, 1);
        }
    }
}
