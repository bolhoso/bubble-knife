package com.bubbleknife2.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bubbleknife.InjectThis;
import com.bubbleknife.Injector;

public class MainActivity extends Activity {

    @InjectThis(id = R.id.button)
    Button myButton;

    @InjectThis(id = R.id.title)
    TextView title;

    @InjectThis(id = R.id.subtitle)
    TextView subTitle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Injector.inject(this);
        myButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "Hooray! I've been injected!", Toast.LENGTH_LONG).show();
            }
        });


    }

}
