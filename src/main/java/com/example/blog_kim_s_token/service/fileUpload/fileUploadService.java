package com.example.blog_kim_s_token.service.fileUpload;

import java.io.File;
import java.util.List;
import java.util.UUID;

import com.example.blog_kim_s_token.service.fileUpload.aws.awsService;
import com.nimbusds.jose.shaded.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class fileUploadService {
    private final String windowLocal="C:/Users/Administrator/Desktop/blog/blog_kim_s_shop/src/main/resources/static/image/";
    private final String serverImageUploadUrl="http://localhost:8080/static/image/";
    private final String awsS3Url="https://s3.ap-northeast-2.amazonaws.com/kimsshop/images/";
    @Value("${cloud.aws.credentials.imageBucktet}")
    private  String  imageBucktetName;
    @Value("${cloud.aws.credentials.fileBucktetName}")
    private  String  fileBucktetName;
   

    JSONObject respone = new JSONObject();

    @Autowired
    private awsService awsService;

    public JSONObject awsS3ImageUpload(MultipartFile multipartFile) {
        String saveName=awsService.uploadAws(multipartFile,imageBucktetName);
        //respone.put("uploaded",true ); //ckeditor5
        respone.put("bool",true );// summernote
        respone.put("url",awsS3Url+saveName);
        return respone;
    }
    public JSONObject localImageUpload(MultipartFile multipartFile) {
        System.out.println("localImageUpload");
        try {
            String savename = null;
            String filename=multipartFile.getOriginalFilename();
            savename=UUID.randomUUID()+filename;
            String localLocation=windowLocal+savename;
            System.out.println(localLocation+" locallocation");
            System.out.println(savename+"sa");
            multipartFile.transferTo(new File(localLocation));
            //respone.put("uploaded",true ); //ckeditor5
            respone.put("bool",true );// summernote
            respone.put("url",serverImageUploadUrl+savename);
            return respone;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            throw new RuntimeException("사진업로드에 실패했습니다");
        }
    }
    public void deleteImage(String fileName) {
        awsService.deleteFile(imageBucktetName, fileName);
    }
    public void CompareImgs(List<String>originImage,List<String>newImages) {
        System.out.println("CompareImgs");
        if(newImages.isEmpty()&&originImage.isEmpty()){
            System.out.println("이미지가 원래/수정 없습니다");
        }else if(newImages.isEmpty()&&!originImage.isEmpty()){
            System.out.println("이미지가 모두 삭제되었습니다 ");
            deleteImages(originImage);
        }else{
            System.out.println("이미지가 일부 변경 되었습니다");
            int originSize=originImage.size();
            int newSize=newImages.size();
            for(int i=0;i<originSize;i++){
                String ori=originImage.get(i);
                for(int ii=0;ii<newSize;ii++){
                    String newImg=newImages.get(ii);
                    System.out.println(i+" "+ii);
                    if(ori.equals(newImg)){
                        System.out.println("이전사진 존재");
                        break;
                    }else if(ii==newSize-1&&!ori.equals(newImg)){
                        System.out.println("삭제된 사진 발견");
                        reuqestDeleteImage(ori);
                    }
                }
            }
        }    
    }
    private void reuqestDeleteImage(String imgPath) {
        System.out.println("reuqestDeleteImage");
        String [] split=imgPath.split("/");
        System.out.println(split[5]);
        deleteImage(split[5]);
    }
    public void deleteImages(List<String>articleImages) {
        System.out.println("deleteImages");
        if(articleImages.isEmpty()){
            System.out.println("이미지가 없는 댓글/게시글 삭제");
            return;
        }
        for(String s: articleImages){
            reuqestDeleteImage(s);
        }
    }
}
