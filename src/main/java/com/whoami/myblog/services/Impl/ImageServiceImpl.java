package com.whoami.myblog.services.Impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.whoami.myblog.dao.ArticleDao;
import com.whoami.myblog.dao.ImagesDao;
import com.whoami.myblog.entity.Article;
import com.whoami.myblog.entity.Images;
import com.whoami.myblog.entity.Page;
import com.whoami.myblog.entity.User;
import com.whoami.myblog.response.ResponseResult;
import com.whoami.myblog.services.ArticleService;
import com.whoami.myblog.services.ImageService;
import com.whoami.myblog.services.UserService;
import com.whoami.myblog.utils.*;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class ImageServiceImpl implements ImageService {


    @Value("${my.blog.image.save-path}")
    private String imagePath;

    @Value("${my.blog.image.max-size}")
    private long maxSize;

    @Autowired
    private ImagesDao imagesDao;

    @Autowired
    private ArticleDao articleDao;

    @Autowired
    private SnowFlakeIdWorker snowFlakeIdWorker;

    @Autowired
    private UserService userService;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public ResponseResult uploadImage(MultipartFile file, String original) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        HttpServletResponse response = requestAttributes.getResponse();
        //计算图片MD5值
//        String md5 = null;
//        try {
//            md5 = FileMd5Util.getMD5((FileInputStream) file.getInputStream());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        Images images1 = imagesDao.selectByMD5(md5);
//        if (images1 != null) {
//            return ResponseResult.FAILED("请勿重复上传同一个图片");
//        }


        if (file == null) {
            return ResponseResult.FAILED("图片不可以为空");
        }
        String contentType = file.getContentType();
        // System.out.println("contentType ==> " + contentType);
        if (TextUtils.isEmpty(contentType)) {
            return ResponseResult.FAILED("文件格式错误");
        }
        String originalFilename = file.getOriginalFilename();
        // System.out.println("originalFilename ==> " + originalFilename);
        String type = getType(contentType, originalFilename);
        if (type == null) {
            return ResponseResult.FAILED("不支持此类文件上传");
        }

        //限制大小
        long size = file.getSize();
        if (size > maxSize) {
            return ResponseResult.FAILED("图片最大仅支持2Mb");
        }
        long currentMillions = System.currentTimeMillis();
        String currentDay = new SimpleDateFormat("yyyy_MM_dd").format(currentMillions);
        String dayPath =  imagePath + File.separator + currentDay;
        File dayPathFile = new File(dayPath);
        if (!dayPathFile.exists()) {
            dayPathFile.mkdirs();
        }
        String targetId = String.valueOf(snowFlakeIdWorker.nextId());
        String targetPath = dayPath + File.separator + type + File.separator + targetId + "." + type;
        File targetFile = new File(targetPath);
        if (!targetFile.getParentFile().exists()) {
            targetFile.getParentFile().mkdirs();
        }
        try {
            if (!targetFile.exists()) {
                targetFile.createNewFile();
            }
            file.transferTo(targetFile);
            Map<String,String> result = new HashMap<>();
            String resultPath = currentMillions + "_" + targetId + "." + type;
            result.put("path",resultPath);
            result.put("name",originalFilename);
            Images images = new Images();
            images.setContentType(contentType);
            images.setId(targetId);
            images.setCreateTime(new Date());
            images.setUpdateTime(new Date());
            images.setPath(targetFile.getPath());
            images.setName(originalFilename);
            images.setUrl(resultPath);
            images.setOriginal(original);
            images.setState("1");
//            images.setMd5(md5);
            User user = userService.checkUser(request,response);
            images.setUserId(user.getId());
            imagesDao.insert(images);
            // System.out.println(result);
            return ResponseResult.SUCCESS("图片上传成功").setData(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ResponseResult.FAILED("图片上传失败");
    }

    private String getType(String contentType, String originalFilename) {
        String type = null;
        if (Constants.ImageType.TYPE_PNG_WITH_PREFIX.equals(contentType) && originalFilename.endsWith(Constants.ImageType.TYPE_PNG)){
            type = Constants.ImageType.TYPE_PNG;
        }else if (Constants.ImageType.TYPE_GIF_WITH_PREFIX.equals(contentType) && originalFilename.endsWith(Constants.ImageType.TYPE_GIF)){
            type = Constants.ImageType.TYPE_GIF;
        }else if (Constants.ImageType.TYPE_JPG_WITH_PREFIX.equals(contentType) && originalFilename.endsWith(Constants.ImageType.TYPE_JPG)){
            type = Constants.ImageType.TYPE_JPG;
        }
        return type;
    }

    private final Object mLock = new Object();

    @Override
    public void viewImage(HttpServletResponse response, String imageId) throws IOException {

        String[] paths = imageId.split("_");
        String dayValue = paths[0];
        String format;
        format = new SimpleDateFormat("yyyy_MM_dd").format(Long.parseLong(dayValue));

        String name = paths[1];
        // System.out.println("format==>" + format);
        // System.out.println("name==>" + name);

        String type = name.split("\\.")[1];
        String targetPath = imagePath + File.separator + format + File.separator + type + File.separator + name;

        File file = new File(targetPath);
        OutputStream writer = null;
        FileInputStream fos = null;
        try {
            if(type.equals(Constants.ImageType.TYPE_PNG)){
                response.setContentType("image/png");
            }else if(type.equals(Constants.ImageType.TYPE_JPG) || type.equals("jpeg")){
                response.setContentType("image/jpeg");
            }else if(type.equals(Constants.ImageType.TYPE_GIF)){
                response.setContentType("image/gif");
            }

            writer = response.getOutputStream();
            fos = new FileInputStream(file);
            byte[] buff = new byte[1024];
            int len;
            while ((len = fos.read(buff)) != -1){
                writer.write(buff,0,len);
            }
            writer.flush();
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            if (fos != null) {
                fos.close();
            }
            if (writer != null) {
                writer.close();
            }
        }
    }

    @Override
    public ResponseResult listImages(Page page, String state, String original) {

        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        HttpServletResponse response = requestAttributes.getResponse();

        User user = userService.checkUser(request, response);
        if (user == null) {
            return ResponseResult.NOT_LOGIN();
        }

        Integer pageNum = page.getPageNum();
        Integer pageSize = page.getPageSize();
        PageHelper.startPage(pageNum,pageSize);
        List<Images> images = imagesDao.selectAll(user.getId(),state,original);
        // System.out.println(images);
        return ResponseResult.SUCCESS("获取图片列表成功").setData(new PageInfo<>(images));
    }

    @Override
    public ResponseResult deleteById(String imageId) {
        Images images = new Images();
        images.setId(imageId);
        images.setState("0");
        int i = imagesDao.updateByPrimaryKeySelective(images);
        if (i > 0) {
            return ResponseResult.SUCCESS("禁用图片成功");
        }
        return ResponseResult.FAILED("禁用图片失败");
    }

    @Override
    public void createQrCode(String code, HttpServletResponse response, HttpServletRequest request) {

        String loginState = (String) redisUtil.get(Constants.User.KEY_PC_LOGIN_ID + code);
        if (TextUtils.isEmpty(loginState)) {
            return;
        }

        String originalDomain = TextUtils.getDomain(request);

        String content = originalDomain + Constants.APP_DOWNLOAD_PATH + "===" + code;
        byte[] result = QrCodeUtils.encodeQRCode(content);
        response.setContentType(QrCodeUtils.RESPONSE_CONTENT_TYPE);
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            outputStream.write(result);
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public ResponseResult getRightImage(String imageId) {
        Images images = new Images();
        images.setId(imageId);
        images.setState("1");
        int i = imagesDao.updateByPrimaryKeySelective(images);
        if (i > 0) {
            return ResponseResult.SUCCESS("恢复图片正常成功");
        }
        return ResponseResult.FAILED("恢复图片正常失败");
    }

    @Override
    public ResponseResult uploadMd(MultipartFile multipartFile) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        HttpServletResponse response = requestAttributes.getResponse();
        if (multipartFile == null) {
            return ResponseResult.FAILED("请上传文件！");
        }
        try {
            //原文件名
            String originalFilename = multipartFile.getOriginalFilename();
            //获取文件后缀名
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            File file = File.createTempFile(System.currentTimeMillis()+ "",suffix);
            multipartFile.transferTo(file);

            String content = FileUtils.readFileToString(file,"utf-8");

            Article article = new Article();
            article.setId(snowFlakeIdWorker.nextId() + "");
            article.setContent(content);
            article.setTitle(originalFilename);
            article.setState("1");

            User user = userService.checkUser(request, response);

            article.setUserName(user.getUserName());
            article.setUserAvatar(user.getAvatar());
            article.setUpdateTime(new Date());
            article.setUserId(user.getId());
            article.setCreateTime(new Date());
            article.setType("1");

            // System.out.println(article);

            int i = articleDao.insertSelective(article);

            return ResponseResult.SUCCESS("上传成功!");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseResult.FAILED("上传失败！");
    }
}
