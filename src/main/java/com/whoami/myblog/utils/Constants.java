package com.whoami.myblog.utils;

public interface Constants {

    //PC端
    String FROM_PC = "p_";
    //mobile端
    String FROM_MOBILE = "m_";

    //app下载地址
    String APP_DOWNLOAD_PATH = "/portal/app/";


    interface User{
        String ROLE_ADMIN = "admin";
        String ROLE_NORMAL = "normal";
        String DEFAULT_AVATAR = "https://cdn.sunofbeaches.com/images/default_avatar.png";
        String DEFAULT_STATE = "1";
        String KEY_CAPTCHA_CONTENT = "key_captcha_content_"; //图灵验证码
        String KEY_EMAIL_CODE_CONTENT = "key_email_code_content_"; //邮箱验证码
        String KEY_EMAIL_SEND_IP = "key_email_send_ip_";
        String KEY_EMAIL_SEND_ADDRESS = "key_email_send_address_";
        String KEY_TOKEN = "key_token_";
        String COOKIE_KEY = "my_blog_token";
        String KEY_COMMIT_TOKEN_RECORD = "key_commit_token_record_";
        String KEY_PC_LOGIN_ID = "key_pc_login_id_";
        String KEY_PC_LOGIN_STATE_FALSE = "false";
        String KEY_PC_LOGIN_STATE_TRUE = "true";
        String LAST_REQUEST_LOGIN_ID = "l_r_l_i";
    }

    interface Settings{
        String MANAGER_ACCOUNT_INIT_STATE = "manager_account_init_state";
        String WEB_SIZE_TITLE = "web_size_title";
        String WEB_SIZE_DESCRIPTION = "web_size_description";
        String WEB_SIZE_KEYWORDS = "web_size_keywords";
        String WEB_SIZE_VIEW_COUNT = "web_size_view_count";
    }

    interface TimeValue{
        int MIN = 60;
        int HALF_MIN = 30;
        int MIN_5 = 60 * 5;
        int MIN_15 = 60 * 15;
        int SECOND_10 = 10;
        int HOUR = 60 * MIN;
        int HOUR_2 = 60 * MIN * 2;
        int DAY = 24 * HOUR;
        int WEEK = 7 * DAY;
        int MONTH = 30 * DAY;
    }

    interface TimeValueMillions{
        long MIN = 60 * 1000;
        long HOUR = 60 * MIN;
        long HOUR_2 = 60 * MIN * 2;
        long DAY = 24 * HOUR;
        long WEEK = 7 * DAY;
        long MONTH = 30 * DAY;
    }

    interface Page{
        int DEFAULT_PAGE = 1;
        int MIN_SIZE = 5;
    }

    interface ImageType{
        String PREFIX = "image/";
        String TYPE_PNG = "png";
        String TYPE_JPG = "jpg";
        String TYPE_GIF = "gif";
        String TYPE_PNG_WITH_PREFIX = PREFIX + "png";
        String TYPE_JPG_WITH_PREFIX = PREFIX + "jpeg";
        String TYPE_GIF_WITH_PREFIX = PREFIX + "gif";
    }

    interface Article{
        int TITLE_MAX_LENGTH = 128;
        int SUMMARY_MAX_LENGTH = 256;
        //状态（0表示已发布，1表示草稿，2表示删除，3表示置顶）
        String STATE_PUBLISH = "0";
        String STATE_DRAFT = "1";
        String STATE_DELETE = "2";
        String STATE_TOP = "3";
        String TYPE_MARKDOWN = "1";
        String TYPE_RICH_TEXT = "0";
        //文章缓存
        String KEY_ARTICLE_CACHE = "key_article_cache_";
        String KEY_ARTICLE_VIEW_COUNT = "key_article_view_count_";
        String KEY_ARTICLE_LIST_FIRST_PAGE = "key_article_list_first_page";
    }

    interface Comment{
        //状态（0表示删除，1表示正常，2表示置顶）
        String STATE_PUBLISH = "1";
        String STATE_TOP = "2";
        String KEY_COMMENT_FIRST_PAGE_CACHE = "key_comment_first_page_cache_";
    }
}
