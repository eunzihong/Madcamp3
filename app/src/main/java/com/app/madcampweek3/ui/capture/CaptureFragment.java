package com.app.madcampweek3.ui.capture;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.loader.content.CursorLoader;

import com.app.madcampweek3.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

import static android.app.Activity.RESULT_OK;

public class CaptureFragment extends Fragment {

    Button btn;
    Button imageBtn;
    EditText result;
    ImageView iv;
    //업로드할 이미지의 절대경로(실제 경로)
    String imgPath;

    private CaptureViewModel homeViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
//        homeViewModel =
//                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_capture, container, false);
        imageBtn = (Button) root.findViewById(R.id.imageBtn);
        btn = (Button) root.findViewById(R.id.btn);
        result = (EditText) root.findViewById(R.id.result);
        iv = (ImageView) root.findViewById(R.id.iv);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ncp();
            }
        });

        imageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //갤러리 or 사진 앱 실행하여 사진을 선택하도록..
                Intent intent= new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent,10);
            }
        });
        return root;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 10:
                if(resultCode==RESULT_OK){
                    //선택한 사진의 경로(Uri)객체 얻어오기
                    Uri uri= data.getData();
                    if(uri!=null){
                        iv.setImageURI(uri);
                        //갤러리앱에서 관리하는 DB정보가 있는데, 그것이 나온다 [실제 파일 경로가 아님!!]
                        //얻어온 Uri는 Gallery앱의 DB번호임. (content://-----/2854)
                        //업로드를 하려면 이미지의 절대경로(실제 경로: file:// -------/aaa.png 이런식)가 필요함
                        //Uri -->절대경로(String)로 변환
                        imgPath= getRealPathFromUri(uri);   //임의로 만든 메소드 (절대경로를 가져오는 메소드)
                        //이미지 경로 uri 확인해보기
                        new AlertDialog.Builder(getActivity()).setMessage(uri.toString()+"\n"+imgPath).create().show();
                    }
                }else
                {
                    Toast.makeText(getActivity(), "이미지 선택을 하지 않았습니다.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }//onActivityResult() ..
    //Uri -- > 절대경로로 바꿔서 리턴시켜주는 메소드
    String getRealPathFromUri(Uri uri){
        String[] proj= {MediaStore.Images.Media.DATA};
        CursorLoader loader= new CursorLoader(getActivity(), uri, proj, null, null, null);
        Cursor cursor= loader.loadInBackground();
        int column_index= cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result= cursor.getString(column_index);
        cursor.close();
        return  result;
    }
    void ncp(){
        new Thread(){
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void run(){
                String apiURL = "https://7fddcdc659404aaf911921f7567947c5.apigw.ntruss.com/custom/v1/2966/851445119e04578b0ad435fdf1f1d6812329d5720dcb40731ba7d698d6b158fa/general";
                String secretKey = "cVdHTUh0eUdPWG9QYXNiQXB4V0FhT1dYZ0RnQktnZnQ=";
                String imageFile = imgPath;
                try {
                    URL url = new URL(apiURL);
                    HttpURLConnection con = (HttpURLConnection)url.openConnection();
                    con.setUseCaches(false);
                    con.setDoInput(true);
                    con.setDoOutput(true);
                    con.setReadTimeout(30000);
                    con.setRequestMethod("POST");
                    String boundary = "----" + UUID.randomUUID().toString().replaceAll("-", "");
                    con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                    con.setRequestProperty("X-OCR-SECRET", secretKey);
                    JSONObject json = new JSONObject();
                    json.put("version", "V2");
                    json.put("requestId", UUID.randomUUID().toString());
                    json.put("timestamp", System.currentTimeMillis());
                    JSONObject image = new JSONObject();
                    image.put("format", "jpg");
                    image.put("name", "demo");
                    JSONArray images = new JSONArray();
                    images.put(image);
                    json.put("images", images);
                    String postParams = json.toString();
                    con.connect();
                    DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                    long start = System.currentTimeMillis();
                    File file = new File(imageFile);
                    writeMultiPart(wr, postParams, file, boundary);
                    wr.close();
                    int responseCode = con.getResponseCode();
                    BufferedReader br;
                    if (responseCode == 200) {
                        br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    } else {
                        br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                    }
                    String inputLine;
                    StringBuffer response = new StringBuffer();
                    while ((inputLine = br.readLine()) != null) {
                        response.append(inputLine);
                    }
                    br.close();
                    Log.d("Result", "zzzzzzzzzzzzzzz"+String.valueOf(response));
                    //System.out.println(response);
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        }.start();
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private static void writeMultiPart(OutputStream out, String jsonMessage, File file, String boundary) throws
            IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("--").append(boundary).append("\r\n");
        sb.append("Content-Disposition:form-data; name=\"message\"\r\n\r\n");
        sb.append(jsonMessage);
        sb.append("\r\n");
        out.write(sb.toString().getBytes("UTF-8"));
        out.flush();
        if (file != null && file.isFile()) {
            out.write(("--" + boundary + "\r\n").getBytes("UTF-8"));
            StringBuilder fileString = new StringBuilder();
            fileString
                    .append("Content-Disposition:form-data; name=\"file\"; filename=");
            fileString.append("\"" + file.getName() + "\"\r\n");
            fileString.append("Content-Type: application/octet-stream\r\n\r\n");
            out.write(fileString.toString().getBytes("UTF-8"));
            out.flush();
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[8192];
                int count;
                while ((count = fis.read(buffer)) != -1) {
                    out.write(buffer, 0, count);
                }
                out.write("\r\n".getBytes());
            }
            out.write(("--" + boundary + "--\r\n").getBytes("UTF-8"));
        }
        out.flush();
    }

}