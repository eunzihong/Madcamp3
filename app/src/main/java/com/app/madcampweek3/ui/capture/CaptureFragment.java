package com.app.madcampweek3.ui.capture;

import android.Manifest;
import android.content.ClipData;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.loader.content.CursorLoader;

import com.app.madcampweek3.CaptureAdapter;
import com.app.madcampweek3.CaptureItem;
import com.app.madcampweek3.R;
import com.yongbeam.y_photopicker.util.photopicker.PhotoPagerActivity;
import com.yongbeam.y_photopicker.util.photopicker.PhotoPickerActivity;
import com.yongbeam.y_photopicker.util.photopicker.utils.YPhotoPickerIntent;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static android.app.Activity.RESULT_OK;

public class CaptureFragment extends Fragment {

    /*
    서버에 보낼 정보: 학년, 년도, 월, 과목
     */
    String grade, year, month, subject;

    CaptureAdapter adapter;
    GridView gridView;

    Button btn;
    Button imageBtn;
    EditText result;

    Spinner spinner_year, spinner_month, spinner_subject;
    RadioButton first, second, third;
    RadioGroup radioGroup;

    ArrayList<String> question = new ArrayList<String>();
    ArrayList<String> answer = new ArrayList<String>();

    private CaptureViewModel homeViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_capture, container, false);
        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1000);
        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1000);

        /*
        RADIO GROUP
         */
        radioGroup = (RadioGroup) root.findViewById(R.id.radioGroup);
        first = (RadioButton) root.findViewById(R.id.first);
        second = (RadioButton) root.findViewById(R.id.second);
        third = (RadioButton) root.findViewById(R.id.third);

        radioGroup.setOnCheckedChangeListener(radioGroupButtonChangeListener);

        /*
        SPINNER
         */
        spinner_year = (Spinner) root.findViewById(R.id.year);
        spinner_month = (Spinner) root.findViewById(R.id.month);
        spinner_subject = (Spinner) root.findViewById(R.id.subject);
        spinner_year.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                year = (String) adapterView.getItemAtPosition(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        spinner_month.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                month = (String) adapterView.getItemAtPosition(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        spinner_subject.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                subject = (String) adapterView.getItemAtPosition(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        gridView = root.findViewById(R.id.gridView);
        adapter = new CaptureAdapter();

        imageBtn = (Button) root.findViewById(R.id.imageBtn);
        btn = (Button) root.findViewById(R.id.btn);
        result = (EditText) root.findViewById(R.id.result);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                for (int i = 0; i < adapter.getCount(); i++) {
                    String imgPath = adapter.getItem(i).getImgPath();
                    NCP ncpThread = new NCP(imgPath);
                    ncpThread.start();
                    try {
                        ncpThread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                for(int i=0; i<question.size(); i++){
                    System.out.println("aaaaaaaaa"+question.get(i));
                }
                System.out.println(grade+year+month+subject);
            }
        });

        imageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //  EXTRA_ALLOW_MULTIPLE: 갤러리에서 다중 선택이 가능하도록 함
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
                //intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 10);
            }

        });
        return root;
    }

    RadioGroup.OnCheckedChangeListener radioGroupButtonChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int i) {
            if (i == R.id.first) {
                grade = "1";
            } else if (i == R.id.second) {
                grade = "2";
            } else {
                grade = "3";
            }
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10 && resultCode == RESULT_OK) {
            Uri imgUri = data.getData();
            String imgPath = getRealPathFromUri(imgUri);

            adapter.addItem(new CaptureItem(imgUri, imgPath));
            gridView.setAdapter(adapter);
        } else {
            Toast.makeText(getContext(), "ERROR", Toast.LENGTH_SHORT).show();
        }
    }


    // Uri를 절대경로로 바꿔줌
    String getRealPathFromUri(Uri uri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        CursorLoader loader = new CursorLoader(getActivity(), uri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;
    }


    public class NCP extends Thread {
        private String imgPath;

        public NCP(String imgPath) {
            this.imgPath = imgPath;
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        public void run() {
            String apiURL = "https://7fddcdc659404aaf911921f7567947c5.apigw.ntruss.com/custom/v1/2966/851445119e04578b0ad435fdf1f1d6812329d5720dcb40731ba7d698d6b158fa/general";
            String secretKey = "cVdHTUh0eUdPWG9QYXNiQXB4V0FhT1dYZ0RnQktnZnQ=";
            String imageFile = imgPath;
            try {
                URL url = new URL(apiURL);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
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
                String inputLine = br.readLine();
                try {
                    JSONObject jsonObject = new JSONObject(inputLine);
                    JSONArray jsonArray = jsonObject.getJSONArray("images");

                    for (int i = 0; i < jsonArray.length(); i++) {

                        JSONArray jsonArray_fields = jsonArray.getJSONObject(i).getJSONArray("fields");

                        boolean questionFlag = true;
                        int k = 0;

                        while (questionFlag) {
                            String inferText = jsonArray_fields.getJSONObject(k++).getString("inferText").replace(".", "");
                            if (isNumeric(inferText)) {
                                question.add(inferText);
                                questionFlag = false;
                            }
                        }
                    }
                } catch (Exception e) {
                }

                br.close();

            } catch (Exception e) {
                System.out.println(e);
            }
        } // END: run()
    } // END: ncp()

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

    // 문자열이 숫자로 되었는지 판단
    public boolean isNumeric(String input) {
        try {
            Double.parseDouble(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}