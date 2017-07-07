package com.example.zhang.loadbutton;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.loadbutton.LoadButton;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private LoadButton load_btn;
    private Button btn_2;
    private Button btn_1;
    private Button btn_3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initListener();

    }

    private void initView() {
        load_btn = (LoadButton) findViewById(R.id.load_btn);
        btn_1 = (Button) findViewById(R.id.btn_1);
        btn_2 = (Button) findViewById(R.id.btn_2);
        btn_3 = (Button) findViewById(R.id.btn_3);
    }

    private void initListener() {
        btn_1.setOnClickListener(this);
        btn_2.setOnClickListener(this);
        btn_3.setOnClickListener(this);

        load_btn.setLoadListener(new LoadButton.LoadListener() {
            @Override
            public void onClick(boolean isSuccessed) {
                if(isSuccessed){
                    Toast.makeText(MainActivity.this,"加载成功",Toast.LENGTH_LONG).show();
                }else {
                    Toast.makeText(MainActivity.this,"加载失败",Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void needLoading() {
                Toast.makeText(MainActivity.this,"重新下载",Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_1:
                load_btn.loadSuccessed();
                break;
            case R.id.btn_2:
                load_btn.loadFailed();
                break;
            case R.id.btn_3:
                load_btn.reset();
                break;
        }

    }
}
