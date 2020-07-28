package com.app.madcampweek3.ui.note;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.madcampweek3.NoteAdapter;
import com.app.madcampweek3.NoteItem;
import com.app.madcampweek3.R;
import com.app.madcampweek3.User;
import com.app.madcampweek3.ui.capture.CaptureFragment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class NoteFragment extends Fragment {

    private NoteViewModel dashboardViewModel;

    RecyclerView noteView;
    ArrayList<NoteItem> noteList;
    NoteAdapter noteAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel =
                ViewModelProviders.of(this).get(NoteViewModel.class);
        View root = inflater.inflate(R.layout.fragment_note, container, false);

        noteView = root.findViewById(R.id.noteView);
        noteView.setHasFixedSize(true);
        noteView.setLayoutManager(new LinearLayoutManager(getContext()));

        noteList = new ArrayList<>();

        noteAdapter = new NoteAdapter(noteList);
        noteView.setAdapter(noteAdapter);

        GetNotes getNotes = new GetNotes();
        getNotes.start();
        try {
            getNotes.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return root;
    }

    public class GetNotes extends Thread {

        @Override
        public void run() {
            String serverUri = "http://ec2-13-125-208-213.ap-northeast-2.compute.amazonaws.com/getNotes.php";
            String parameters = "user=" + User.email;
            try {
                URL url = new URL(serverUri);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoInput(true);
                connection.setUseCaches(false);

                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(parameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();

                InputStream is = connection.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader reader = new BufferedReader(isr);

                final StringBuffer buffer = new StringBuffer();
                String line = reader.readLine();

                while (line != null) {
                    buffer.append(line + "\n");
                    line = reader.readLine();
                }

                //읽어온 문자열에서 row(레코드)별로 분리하여 배열로 리턴하기
                String[] rows=buffer.toString().split(";");

                for(String row : rows){
                    //한줄 데이터에서 한 칸씩 분리
                    String[] datas=row.split("&");
                    if(datas.length!=5) continue;
                    String mock_id = datas[0];
                    String mock_name="";
                    String mock_subject=datas[4];
                    if(!datas[3].equals("수능")) {
                        mock_name = "고" + datas[1] + " " + datas[2] + "년도 " + datas[3] + "월 " + datas[4] + " 모의고사";
                    }else{
                        mock_name = "고" + datas[1] + " " + datas[2] + "년도 " + datas[3] + " " + datas[4] + " 모의고사";
                    }
                    NoteItem noteItem = new NoteItem(mock_id, mock_name, mock_subject);
                    noteList.add(noteItem);
                    noteAdapter.notifyDataSetChanged();
                }
                //noteView.setAdapter(noteAdapter);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}