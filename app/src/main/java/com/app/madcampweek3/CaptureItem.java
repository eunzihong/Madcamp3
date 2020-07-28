package com.app.madcampweek3;

import android.net.Uri;

public class CaptureItem {
    Uri imgUri; // 갤러리 앱의 DB 정보
    String imgPath; // 이미지의 절대 경로
    String torchAnswer;

    public CaptureItem(Uri imgUri, String imgPath) {
        this.imgUri = imgUri;
        this.imgPath = imgPath;
    }

    public Uri getImgUri() {
        return imgUri;
    }

    public void setImgUri(Uri imgUri) {
        this.imgUri = imgUri;
    }

    public String getImgPath() {
        return imgPath;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }

    public String getTorchAnswer() {return torchAnswer;}

    public void setTorchAnswer(String torchAnswer)  {this.torchAnswer = torchAnswer;}

}
